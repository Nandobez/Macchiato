package dev.nandobez.macc.cmd;

import picocli.CommandLine.*;
import java.util.concurrent.Callable;

import static dev.nandobez.macc.cmd.Tui.*;

@Command(name = "deps", description = "Shortcut for `jdp list`.")
public class DepsCmd implements Callable<Integer> {

    public Integer call() throws Exception {
        String jdpJar = DoctorCmd.locateJdp();
        if (jdpJar == null) {
            error("jdp not installed. Install from https://github.com/Nandobez/jdp");
            return 2;
        }
        return new ProcessBuilder("java", "-jar", jdpJar, "list").inheritIO().start().waitFor();
    }
}
