package org.astropeci.omwagent;

import javassist.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Arrays;

/**
 * A <a href="https://docs.oracle.com/javase/7/docs/api/java/lang/instrument/package-summary.html">Java Agent</a> which
 * transforms the Paper server class files in order to customise aspects of the game that would be otherwise very
 * difficult to change with a plugin.
 *
 * In particular this agent is used to:
 * <ul>
 *     <li>
 *         Modify the blast durability of moving piston blocks to match the blast durability of they block they are
 *         carrying.
 *     </li>
 *     <li>
 *         Prevent fireballs from being pushed by pistons or moving slime blocks.
 *     </li>
 * </ul>
 */
public class Agent {

    // Modification of class files at runtime is difficult, so this class uses the Javassist library in order to compile
    // pseudo-Java source code strings at runtime and inject them into Paper class files. Note that these snippets are
    // not really Java, so some features such as generics are not supported.

    /**
     * The injection string which modifies the blast resistance of moving piston blocks.
     *
     * This string is injected into the ExplosionDamageCalculator NMS class at the top of the method which returns an
     * instance of {@link java.util.Optional}. The snippet detects whether the block is a moving piston, and if it is
     * applies the modified blast resistance algorithm. If it isn't, the existing Paper code in the method is used.
     */
    private static final String EXPLOSION_DAMAGE_CALCULATOR_INJECTION =
            "{\n" +
                    // Gets the block being exploded
                    "$NMS.Block block = $4.getBlock();\n" +
                    // If it is a moving piston block
                    "if (block instanceof $NMS.BlockPistonMoving) {\n" +
                    //   Then get the tile entity associated with it
                    "    $NMS.TileEntity tileEntity = $1.source.getWorld().getTileEntity($3);\n" +
                    //   If the tile entity is a piston tile entity (which it should be)
                    "    if (tileEntity instanceof $NMS.TileEntityPiston) {\n" +
                    "        $NMS.TileEntityPiston tileEntityPiston = ($NMS.TileEntityPiston) tileEntity;\n" +
                    //       Then calculate the durability, taking into account any fluids which may be present
                    "        float durability = Math.max($5.i(), tileEntityPiston.k().getBlock().getDurability());\n" +
                    "        return java.util.Optional.of(Float.valueOf(durability));\n" +
                    "    }\n" +
                    "}\n" +
                    "}";

    /**
     * The injection string which disallows the pushing of fireballs.
     *
     * This string is injected into the EntityFireball NMS class under a new method which overrides a one in a
     * superclass.
     */
    private static final String FIREBALL_PUSH_INJECTION =
            "public $NMS.EnumPistonReaction getPushReaction() {\n" +
                    // Set the piston reaction to IGNORE so fireballs can't be pushed
                    "return $NMS.EnumPistonReaction.IGNORE;\n" +
                    "}";


    /**
     * Stores classes as they are being loaded by the JVM.
     *
     * This is needed so injected snippets can reference other loaded classes.
     */
    private static ClassPool classPool;

    /**
     * The entry point for the agent which is called before the Java main method.
     */
    public static void premain(String args, Instrumentation instrumentation) {
        classPool = new ClassPool();
        classPool.appendSystemPath();

        // Add a transformer which can edit class bytecode as the JVM loads it
        instrumentation.addTransformer(new ClassFileTransformer() {

            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
                try {
                    // Register the class with the class pool
                    classPool.makeClass(new ByteArrayInputStream(classfileBuffer));

                    // If the class is ExplosionDamageCalculator, return the modified bytecode
                    if (className.matches("net/minecraft/server/v[_a-zA-Z0-9]+/ExplosionDamageCalculator")) {
                        return redefineExplosionDamageCalculator(className);
                    }

                    // If the class is EntityFireball, return the modified bytecode
                    if (className.matches("net/minecraft/server/v[_a-zA-Z0-9]+/EntityFireball")) {
                        return redefineFireball(className);
                    }
                } catch (Exception e) {
                    Exception wrapper = new Exception("Unhandled exception in agent", e);
                    wrapper.printStackTrace();
                }

                return null;
            }
        });
    }

    private static byte[] redefineExplosionDamageCalculator(String internalClassName) throws IOException, CannotCompileException {
        // JVMs use the / character instead of the . character as a package separator internally
        String className = internalClassName.replace('/', '.');
        // Will never be null
        CtClass cls = classPool.getOrNull(className);

        try {
            // A behaviour is essentially a method; we'll cycle through them to find the method which calculates blast
            // resistance
            for (CtBehavior behavior : cls.getDeclaredBehaviors()) {
                // Obfuscated methods don't have a stable name, so we'll identify it by its Optional return type
                if (!behavior.getSignature().endsWith("Ljava/util/Optional;")) {
                    continue;
                }

                // Use the full name of the class to find the NMS package name and then use it to replace the $NMS
                // shortcuts in the Javassist snippets
                String nmsPackage = className.substring(0, className.indexOf("ExplosionDamageCalculator") - 1);
                String payload = EXPLOSION_DAMAGE_CALCULATOR_INJECTION.replaceAll("\\$NMS", nmsPackage);

                // Finally, inject the Javassist snippet at the top of the method
                behavior.insertBefore(payload);
            }

            return cls.toBytecode();
        } finally {
            cls.detach();
        }
    }

    // Some patterns used in this method are replicated above, see them for documentation
    private static byte[] redefineFireball(String internalClassName) throws IOException, CannotCompileException {
        String className = internalClassName.replace('/', '.');
        CtClass cls = classPool.getOrNull(className);

        // Check that there is actually a method there to be overridden. If the method name changes in the future this
        // will avoid a silent failure
        boolean hasAppropriateMethod = Arrays.stream(cls.getMethods())
                .anyMatch(method -> method.getName().equals("getPushReaction"));

        if (!hasAppropriateMethod) {
            throw new IllegalStateException("EntityFireball did not have a getPushReaction method");
        }

        try {
            String nmsPackage = className.substring(0, className.indexOf("EntityFireball") - 1);
            CtMethod method = CtNewMethod.make(FIREBALL_PUSH_INJECTION.replaceAll("\\$NMS", nmsPackage), cls);

            // Adds the snippet into the class which overrides a method
            cls.addMethod(method);
            return cls.toBytecode();
        } finally {
            cls.detach();
        }
    }
}
