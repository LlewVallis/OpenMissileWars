package org.astropeci.omwagent;

import javassist.*;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Date;

public class Agent {

    private static final String EXPLOSION_DAMAGE_CALCULATOR_INJECTION =
            "{\n" +
                    "$NMS.Block block = $4.getBlock();\n" +
                    "if (block instanceof $NMS.BlockPistonMoving) {\n" +
                    "    $NMS.TileEntity tileEntity = $1.source.getWorld().getTileEntity($3);\n" +
                    "    if (tileEntity instanceof $NMS.TileEntityPiston) {\n" +
                    "        $NMS.TileEntityPiston tileEntityPiston = ($NMS.TileEntityPiston) tileEntity;\n" +
                    "        float durability = Math.max($5.i(), tileEntityPiston.k().getBlock().getDurability());\n" +
                    "        return java.util.Optional.of(Float.valueOf(durability));\n" +
                    "    }\n" +
                    "}\n" +
                    "}";

    private static final String FIREBALL_PUSH_INJECTION =
            "public $NMS.EnumPistonReaction getPushReaction() {\n" +
                    "return $NMS.EnumPistonReaction.IGNORE;\n" +
                    "}";


    private static ClassPool classPool;

    public static void premain(String args, Instrumentation instrumentation) throws FileNotFoundException {
        classPool = new ClassPool();
        classPool.appendSystemPath();

        instrumentation.addTransformer(new ClassFileTransformer() {

            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
                try {
                    classPool.makeClass(new ByteArrayInputStream(classfileBuffer));

                    if (className.matches("net/minecraft/server/v[_a-zA-Z0-9]+/ExplosionDamageCalculatorBlock")) {
                        return redefineExplosionDamageCalculator(className);
                    }

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
        String className = internalClassName.replace('/', '.');

        CtClass cls = classPool.getOrNull(className);

        try {
            for (CtBehavior behavior : cls.getDeclaredBehaviors()) {
                if (!behavior.getSignature().endsWith("Ljava/util/Optional;")) {
                    continue;
                }

                String nmsPackage = className.substring(0, className.indexOf("ExplosionDamageCalculatorBlock") - 1);
                String payload = EXPLOSION_DAMAGE_CALCULATOR_INJECTION.replaceAll("\\$NMS", nmsPackage);

                behavior.insertBefore(payload);
            }

            return cls.toBytecode();
        } finally {
            cls.detach();
        }
    }

    private static byte[] redefineFireball(String internalClassName) throws IOException, CannotCompileException {
        String className = internalClassName.replace('/', '.');

        CtClass cls = classPool.getOrNull(className);

        try {
            String nmsPackage = className.substring(0, className.indexOf("EntityFireball") - 1);
            CtMethod method = CtNewMethod.make(FIREBALL_PUSH_INJECTION.replaceAll("\\$NMS", nmsPackage), cls);

            cls.addMethod(method);
            return cls.toBytecode();
        } finally {
            cls.detach();
        }
    }
}
