package dev.nandobez.macc.cmd;

import picocli.CommandLine.*;
import java.nio.file.*;
import java.util.concurrent.Callable;

import static dev.nandobez.macc.cmd.Tui.*;

@Command(name = "new",
    description = "Scaffold the Vite/React frontend with the Macc DSL dep wired in.")
public class NewCmd implements Callable<Integer> {

    @Parameters(arity = "1", description = "Frontend directory (e.g. src/main/frontend).")
    Path dir;

    public Integer call() throws Exception {
        banner("macc new", dir.toString());
        Files.createDirectories(dir);
        Files.createDirectories(dir.resolve("pages"));
        Files.createDirectories(dir.resolve("components"));
        Files.createDirectories(dir.resolve("types"));

        write(dir.resolve("package.json"), packageJson());
        write(dir.resolve("vite.config.ts"), viteConfig());
        write(dir.resolve("tsconfig.json"), tsconfig());
        write(dir.resolve("tailwind.config.js"), tailwindConfig());
        write(dir.resolve("postcss.config.js"), postcssConfig());
        write(dir.resolve("index.html"), indexHtml());
        write(dir.resolve("src/main.tsx"), mainTsx());
        write(dir.resolve("src/styles.css"), stylesCss());
        info("now run: " + Tui.BLD + "cd " + dir + " && npm install" + Tui.R);
        return 0;
    }

    private static void write(Path p, String c) throws Exception {
        Files.createDirectories(p.getParent());
        Files.writeString(p, c);
        wrote(p.toString());
    }

    private static String packageJson() { return """
        {
          "name": "macc-frontend",
          "private": true,
          "type": "module",
          "scripts": {
            "dev": "vite",
            "build": "tsc -b && vite build",
            "preview": "vite preview"
          },
          "dependencies": {
            "react": "^18.3.1",
            "react-dom": "^18.3.1",
            "react-router-dom": "^6.26.2",
            "clsx": "^2.1.1"
          },
          "devDependencies": {
            "@types/react": "^18.3.10",
            "@types/react-dom": "^18.3.0",
            "@vitejs/plugin-react": "^4.3.3",
            "autoprefixer": "^10.4.20",
            "postcss": "^8.4.47",
            "tailwindcss": "^3.4.13",
            "typescript": "^5.6.2",
            "vite": "^5.4.8"
          }
        }
        """; }

    private static String viteConfig() { return """
        import { defineConfig } from "vite";
        import react from "@vitejs/plugin-react";

        export default defineConfig({
          plugins: [react()],
          server: {
            port: 5173,
            proxy: {
              "/api": "http://localhost:8080"
            }
          },
          build: {
            outDir: "../resources/static",
            emptyOutDir: true
          }
        });
        """; }

    private static String tsconfig() { return """
        {
          "compilerOptions": {
            "target": "ES2022",
            "lib": ["DOM", "DOM.Iterable", "ES2022"],
            "jsx": "react-jsx",
            "module": "ESNext",
            "moduleResolution": "bundler",
            "strict": true,
            "noEmit": true,
            "esModuleInterop": true,
            "skipLibCheck": true,
            "allowImportingTsExtensions": false,
            "resolveJsonModule": true,
            "isolatedModules": true
          },
          "include": ["src", "pages", "components", "types", "routes.tsx", "lib"]
        }
        """; }

    private static String tailwindConfig() { return """
        export default {
          content: ["./index.html", "./src/**/*.{ts,tsx}", "./pages/**/*.{ts,tsx}", "./components/**/*.{ts,tsx}"],
          theme: { extend: {} },
          plugins: [],
        };
        """; }

    private static String postcssConfig() { return """
        export default { plugins: { tailwindcss: {}, autoprefixer: {} } };
        """; }

    private static String indexHtml() { return """
        <!doctype html>
        <html lang="en">
          <head>
            <meta charset="UTF-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0" />
            <title>Macc App</title>
          </head>
          <body>
            <div id="root"></div>
            <script type="module" src="/src/main.tsx"></script>
          </body>
        </html>
        """; }

    private static String mainTsx() { return """
        import React from "react";
        import ReactDOM from "react-dom/client";
        import "./styles.css";
        import { AppRouter } from "../routes";

        ReactDOM.createRoot(document.getElementById("root")!).render(
          <React.StrictMode>
            <AppRouter />
          </React.StrictMode>
        );
        """; }

    private static String stylesCss() { return """
        @tailwind base;
        @tailwind components;
        @tailwind utilities;
        """; }
}
