package dev.crestwatch.trapper;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.spi.ToolProvider;

/** Checks the artifact users install, including the regression that broke 1.3.0. */
public final class ProductionJarTest {
    public static void main(String[] args) throws Exception {
        Path jar = Path.of(args[0]);
        Path reports = Files.createDirectories(Path.of(args[1]));
        try (JarFile archive = new JarFile(jar.toFile())) {
            for (var entry : archive.stream().toList()) {
                String name = entry.getName();
                require(!name.startsWith("net/fabricmc/") && !name.startsWith("net/minecraft/"),
                    "Bundled runtime/stub class: " + name);
                require(!name.startsWith("dev/trevorroll/test/") && !name.contains("GameTest"),
                    "Test code shipped in production: " + name);
            }
            for (String name : new String[]{"dev/trevorroll/TrevorRollClient.class",
                    "dev/crestwatch/trapper/TrapperCrestHudClient.class",
                    "assets/trevorroll/textures/gui/circle.png",
                    "assets/trevorroll/textures/gui/glow.png"}) {
                require(archive.getJarEntry(name) != null, "Missing production entry: " + name);
            }
        }

        var javap = ToolProvider.findFirst("javap").orElseThrow();
        for (String type : new String[]{"dev.crestwatch.trapper.CrestHudConfig", "dev.trevorroll.RollConfig"}) {
            StringWriter output = new StringWriter();
            int result = javap.run(new PrintWriter(output), new PrintWriter(output),
                "-p", "-v", "-classpath", jar.toString(), type);
            String bytecode = output.toString();
            Files.writeString(reports.resolve(type.substring(type.lastIndexOf('.') + 1) + "-bytecode.txt"), bytecode);
            require(result == 0, "javap failed: " + type);
            require(bytecode.contains("major version: 69"), "Expected Java 25 class file: " + type);
            for (String method : new String[]{"getInstance", "getConfigDir"}) {
                require(Pattern.compile("(?m)^.*= InterfaceMethodref.*FabricLoader\\." + method + ":.*$")
                    .matcher(bytecode).find(), "Missing InterfaceMethodref: " + type + "." + method);
            }
            require(!Pattern.compile("(?m)^.*= Methodref.*FabricLoader\\..*$").matcher(bytecode).find(),
                "Invalid FabricLoader Methodref: " + type);
        }
        System.out.println("PASS: production JAR has Java 25 interface bytecode, original assets, and no runtime stubs");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
