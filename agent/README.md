# OpenMissileWars Agent

This module produces the `agent.jar` file which acts as a [Java agent](https://docs.oracle.com/javase/7/docs/api/java/lang/instrument/package-summary.html).
The agent modifies Paper behavior in ways that a plugin normally couldn't by rewritting class files as they are loaded by the JVM.