package dev.nandobez.macc.dsl;

import java.util.*;

/** Render of another component: <ComponentName ...props/>. */
public final class ComponentRef implements Element {
    public final String name;
    public final String importPath;            // null = same dir
    public final Map<String, Object> props = new LinkedHashMap<>();
    public final List<Element> children = new ArrayList<>();

    public ComponentRef(String name, String importPath) {
        this.name = name;
        this.importPath = importPath;
    }

    public ComponentRef prop(String name, Object value) {
        props.put(name, value);
        return this;
    }

    public ComponentRef children(Element... kids) {
        for (Element k : kids) if (k != null) children.add(k);
        return this;
    }
}
