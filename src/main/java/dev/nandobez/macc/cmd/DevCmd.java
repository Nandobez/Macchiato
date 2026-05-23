package dev.nandobez.macc.cmd;

import picocli.CommandLine.*;
import java.nio.file.*;
import java.util.concurrent.Callable;

import static dev.nandobez.macc.cmd.Tui.*;

@Command(name = "dev",
    description = "Runs vite dev server (frontend hot reload). Backend should run separately with `mvn spring-boot:run`.")
public class DevCmd implements Callable<Integer> {

    @Option(names = {"-d", "--dir"}, defaultValue = ".",
        description = "Project root.")
    Path projectRoot;

    @Option(names = "--frontend", defaultValue = "src/main/frontend")
    Path frontend;

    public Integer call() throws Exception {
        Path root = projectRoot.toAbsolutePath();
        Path feDir = root.resolve(frontend);
        banner("macc dev", "vite + watch");

        // ensure codegen has run at least once
        info("running codegen first…");
        var ccmd = new java.util.ArrayList<String>();
        ccmd.add("java"); ccmd.add("-jar"); ccmd.add(jarPath());
        ccmd.add("codegen"); ccmd.add("--dir"); ccmd.add(root.toString());
        new ProcessBuilder(ccmd).directory(root.toFile()).inheritIO().start().waitFor();

        info("starting vite at http://localhost:5173  (proxies /api to :8080)");
        return new ProcessBuilder("npm", "run", "dev")
            .directory(feDir.toFile()).inheritIO().start().waitFor();
    }

    private static String jarPath() {
        return DevCmd.class.getProtectionDomain().getCodeSource().getLocation().getPath();
    }
}
