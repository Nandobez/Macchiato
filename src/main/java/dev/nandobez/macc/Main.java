package dev.nandobez.macc;

import dev.nandobez.macc.cmd.*;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import static dev.nandobez.macc.cmd.Tui.*;

@Command(
    name = "macc",
    mixinStandardHelpOptions = true,
    version = "macc 0.1.0",
    description = "Macc — write React in Java. DSL + codegen.",
    subcommands = { NewCmd.class, GenerateCmd.class, CodegenCmd.class,
        InstallCmd.class, DevCmd.class, ServeCmd.class, WatchCmd.class,
        UiAddCmd.class, DoctorCmd.class, DepsCmd.class }
)
public class Main implements Runnable {

    public void run() { printHelp(); }

    public static void main(String[] args) {
        if (args.length == 0) { printHelp(); System.exit(0); }
        System.out.println();
        int rc = new CommandLine(new Main()).execute(args);
        System.out.println();
        System.exit(rc);
    }

    private static void printHelp() {
        System.out.println();
        System.out.println(BLD + "macc " + R + DIM + "0.1.0" + R + " — write React in Java");
        System.out.println();
        System.out.println("  " + DIM + "PROJECT" + R);
        System.out.println();
        System.out.println("    macc " + BLD + "new <dir>" + R + "                  scaffold Vite + React + Tailwind");
        System.out.println("    macc " + BLD + "install" + R + "                     full build (compile + codegen + npm + vite build)");
        System.out.println("    macc " + BLD + "dev" + R + "                         vite hot reload");
        System.out.println("    macc " + BLD + "serve" + R + "                       xpresso s + vite together");
        System.out.println("    macc " + BLD + "watch" + R + "                       file-watcher · re-codegen on .java change");
        System.out.println();
        System.out.println("  " + DIM + "GENERATE" + R);
        System.out.println();
        System.out.println("    macc " + BLD + "g page <Name>" + R + "               @Page class with @Fetch/@State/@Action");
        System.out.println("    macc " + BLD + "g component <Name>" + R + "          custom Java component (reusable)");
        System.out.println("    macc " + BLD + "g template <Name>" + R + "           alias of g component — your own DSL template");
        System.out.println("    macc " + BLD + "g model <Name>" + R + "              @Model record (shared with backend)");
        System.out.println("    macc " + BLD + "codegen" + R + "                     scan + emit .tsx + types + routes");
        System.out.println();
        System.out.println("  " + DIM + "UI LIBRARY" + R);
        System.out.println();
        System.out.println("    macc " + BLD + "add <comp>…" + R + "                 download shadcn-style components + generate Java wrappers");
        System.out.println("                                 ex: " + DIM + "macc add button card dialog" + R);
        System.out.println();
        System.out.println("  " + DIM + "INTEGRATIONS" + R);
        System.out.println();
        System.out.println("    macc " + BLD + "doctor [--fix]" + R + "             delegate to " + DIM + "jdp doctor" + R);
        System.out.println("    macc " + BLD + "deps" + R + "                       delegate to " + DIM + "jdp list" + R);
        System.out.println();
    }
}
