package dev.nandobez.macc.dsl;

import java.util.*;

/** Reference to an external React component (e.g. shadcn or any npm-installed component). */
public final class ExternalComponent implements Element {
    public final String importPath;
    public final String exportName;
    public final Map<String, Object> props = new LinkedHashMap<>();
    public final List<Element> children = new ArrayList<>();

    ExternalComponent(String importPath, String exportName) {
        this.importPath = importPath;
        this.exportName = exportName;
    }

    /** Create a fresh instance for use in a render(). */
    public ExternalComponent of() { return new ExternalComponent(importPath, exportName); }

    public ExternalComponent prop(String name, Object value) {
        ExternalComponent c = of();
        c.props.putAll(this.props);
        c.props.put(name, value);
        return c;
    }

    public ExternalComponent className(String v)            { return prop("className", v); }
    public ExternalComponent className(Object exprOrValue)  { return prop("className", exprOrValue); }

    public ExternalComponent children(Element... kids) {
        ExternalComponent c = of();
        c.props.putAll(this.props);
        c.children.addAll(this.children);
        for (var k : kids) if (k != null) c.children.add(k);
        return c;
    }
}
