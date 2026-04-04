package dev.nandobez.macc.codegen;

import dev.nandobez.macc.dsl.annotations.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.regex.*;

/** Builds TS handler functions from @Action-annotated Java methods. */
public class ActionEmitter {

    private static final Pattern PATH_VAR = Pattern.compile("\\{(\\w+)\\}");

    public record Generated(String functionSource, String importsLine) {}

    public Generated emit(Method m) {
        Action a = m.getAnnotation(Action.class);
        String name = m.getName();
        Parameter[] params = m.getParameters();

        // Map path vars to params by name
        Map<String, Parameter> byName = new LinkedHashMap<>();
        for (Parameter p : params) byName.put(p.getName(), p);

        // Build the URL template: `${param.id}` style
        Matcher mat = PATH_VAR.matcher(a.url());
        StringBuilder urlTs = new StringBuilder();
        boolean hasInterp = false;
        int lastEnd = 0;
        while (mat.find()) {
            urlTs.append(a.url(), lastEnd, mat.start());
            String var = mat.group(1);
            urlTs.append("${").append(resolveRef(var, byName)).append("}");
            hasInterp = true;
            lastEnd = mat.end();
        }
        urlTs.append(a.url(), lastEnd, a.url().length());
        String url = (hasInterp ? "`" + urlTs + "`" : "\"" + urlTs + "\"");

        StringBuilder src = new StringBuilder();
        boolean isFormSubmit = params.length == 0;

        // Function signature
        src.append("async function ").append(name).append("(");
        if (isFormSubmit) {
            src.append("e: React.FormEvent<HTMLFormElement>");
        } else {
            boolean first = true;
            for (Parameter p : params) {
                if (!first) src.append(", ");
                first = false;
                src.append(p.getName()).append(": ").append(tsType(p.getType()));
            }
        }
        src.append(") {\n");

        // Body assembly
        if (isFormSubmit) {
            if (a.preventDefault()) src.append("  e.preventDefault();\n");
            src.append("  const __form = e.currentTarget;\n");
            src.append("  const __data = Object.fromEntries(new FormData(__form));\n");
        }

        String bodyExpr = computeBody(a, params, isFormSubmit);

        boolean hasBody = bodyExpr != null;

        src.append("  await fetch(").append(url);
        if (!a.method().equalsIgnoreCase("GET")) {
            src.append(", {\n");
            src.append("    method: \"").append(a.method().toUpperCase()).append("\",\n");
            if (hasBody) {
                src.append("    headers: { \"Content-Type\": \"application/json\" },\n");
                src.append("    body: JSON.stringify(").append(bodyExpr).append("),\n");
            }
            src.append("  }");
        }
        src.append(");\n");

        if (isFormSubmit) src.append("  __form.reset();\n");
        if (a.reloadAfter()) src.append("  location.reload();\n");
        src.append("}\n");

        return new Generated(src.toString(), null);
    }

    private String computeBody(Action a, Parameter[] params, boolean isFormSubmit) {
        if (!a.body().isEmpty()) return a.body();
        if (isFormSubmit) return "__data";
        // pick the first non-path-only parameter (heuristic: prefer object types)
        for (Parameter p : params) {
            if (!isPrimitiveOrBoxed(p.getType())) return p.getName();
        }
        return null;
    }

    private String resolveRef(String var, Map<String, Parameter> byName) {
        // First try direct param name.
        if (byName.containsKey(var)) return var;
        // Fall back to <firstParam>.<var>  (e.g. id from a Task t)
        var iter = byName.entrySet().iterator();
        if (iter.hasNext()) {
            var first = iter.next();
            if (!isPrimitiveOrBoxed(first.getValue().getType())) {
                return first.getKey() + "." + var;
            }
        }
        return var;
    }

    private static boolean isPrimitiveOrBoxed(Class<?> c) {
        return c.isPrimitive() || c == String.class || c == Long.class || c == Integer.class
            || c == Boolean.class || c == Double.class || c == Float.class || c == Number.class;
    }

    private static String tsType(Class<?> c) {
        if (c == String.class) return "string";
        if (c == Boolean.class || c == boolean.class) return "boolean";
        if (c == Long.class || c == long.class || c == Integer.class || c == int.class
            || c == Double.class || c == double.class || c == Float.class || c == float.class
            || c == Number.class) return "number";
        return c.getSimpleName();
    }
}
