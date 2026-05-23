package dev.nandobez.macc.cmd;

import picocli.CommandLine.*;
import java.nio.file.*;
import java.util.concurrent.Callable;

import static dev.nandobez.macc.cmd.Tui.*;

@Command(name = "generate", aliases = {"g"},
    description = "Generate a page or component skeleton.")
public class GenerateCmd implements Callable<Integer> {

    @Parameters(index = "0", description = "Kind: page | component | model")
    String kind;

    @Parameters(index = "1", description = "Name (e.g. UsersPage, Card)")
    String name;

    @Option(names = "--path", description = "Route path for @Page (e.g. /users)")
    String routePath;

    @Option(names = "--package", description = "Java package", defaultValue = "com.example.ui")
    String pkg;

    public Integer call() throws Exception {
        banner("macc g " + kind, name);
        Path srcRoot = Path.of("src/main/java/" + pkg.replace('.', '/'));
        Files.createDirectories(srcRoot);
        String file = switch (kind.toLowerCase()) {
            case "page"      -> pageTemplate(pkg, name, routePath == null ? "/" + name.toLowerCase() : routePath);
            case "component" -> componentTemplate(pkg, name);
            case "model"     -> modelTemplate(pkg, name);
            default -> { error("unknown kind: " + kind); yield null; }
        };
        if (file == null) return 2;
        Path out = srcRoot.resolve(name + ".java");
        Files.writeString(out, file);
        wrote(out.toString());
        return 0;
    }

    private static String pageTemplate(String pkg, String name, String path) {
        return """
            package %s;

            import dev.nandobez.macc.dsl.*;
            import dev.nandobez.macc.dsl.annotations.*;
            import static dev.nandobez.macc.dsl.Tags.*;
            import static dev.nandobez.macc.dsl.Helpers.*;

            @Page("%s")
            public class %s extends Component {

                @Override
                public Element render() {
                    return div().className("p-6").children(
                        h1("Hello from %s")
                    );
                }
            }
            """.formatted(pkg, path, name, name);
    }

    private static String componentTemplate(String pkg, String name) {
        return """
            package %s;

            import dev.nandobez.macc.dsl.*;
            import dev.nandobez.macc.dsl.annotations.*;
            import static dev.nandobez.macc.dsl.Tags.*;

            public class %s extends Component {

                @Prop String title = "untitled";

                @Override
                public Element render() {
                    return div().className("p-4 border rounded").children(
                        h2(title)
                    );
                }
            }
            """.formatted(pkg, name);
    }

    private static String modelTemplate(String pkg, String name) {
        return """
            package %s;

            import dev.nandobez.macc.dsl.annotations.Model;

            @Model
            public record %s(Long id, String name) {}
            """.formatted(pkg, name);
    }
}
