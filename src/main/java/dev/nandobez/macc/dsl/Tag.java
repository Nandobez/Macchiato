package dev.nandobez.macc.dsl;

import java.util.*;

/** A single HTML element node (div, h1, button, …). */
public class Tag implements Element {

    public final String name;
    public final Map<String, Object> attrs = new LinkedHashMap<>();
    public final List<Element> children = new ArrayList<>();
    public Object textContent;     // String or ExprNode
    public Object keyValue;        // Object (primitive) or ExprNode

    public Tag(String name) { this.name = name; }

    public Tag className(String value)        { attrs.put("className", value); return this; }
    public Tag className(Object exprOrValue)  { attrs.put("className", exprOrValue); return this; }

    public Tag classIf(boolean cond, String ifTrue, String ifFalse) {
        attrs.put("className", new PickNode(cond, ifTrue, ifFalse, /*isStaticCond=*/true));
        return this;
    }
    public Tag classIf(Object exprCond, String ifTrue, String ifFalse) {
        attrs.put("className", new PickNode(exprCond, ifTrue, ifFalse, /*isStaticCond=*/false));
        return this;
    }

    public Tag classes(Object... parts) {
        attrs.put("className", new ClassesNode(parts));
        return this;
    }

    public Tag text(String value)             { this.textContent = value; return this; }
    public Tag text(Object exprOrValue)       { this.textContent = exprOrValue; return this; }

    public Tag key(Object value)              { this.keyValue = value; return this; }

    public Tag attr(String name, Object val)  { attrs.put(name, val); return this; }
    public Tag type(String t)                 { return attr("type", t); }
    public Tag value(Object v)                { return attr("value", v); }
    public Tag placeholder(String p)          { return attr("placeholder", p); }
    public Tag src(String s)                  { return attr("src", s); }
    public Tag href(String h)                 { return attr("href", h); }
    public Tag disabled(Object cond)          { return attr("disabled", cond); }
    public Tag checked(Object cond)           { return attr("checked", cond); }

    public Tag onClick(String jsHandler)      { return attr("onClick", new ExprNode(jsHandler)); }
    public Tag onClick(ExprNode expr)         { return attr("onClick", expr); }
    public Tag onChange(String jsHandler)     { return attr("onChange", new ExprNode(jsHandler)); }
    public Tag onChange(ExprNode expr)        { return attr("onChange", expr); }
    public Tag onSubmit(String jsHandler)     { return attr("onSubmit", new ExprNode(jsHandler)); }
    public Tag onSubmit(ExprNode expr)        { return attr("onSubmit", expr); }

    public Tag children(Element... kids) {
        for (Element k : kids) if (k != null) this.children.add(k);
        return this;
    }
    public Tag children(List<? extends Element> kids) {
        for (Element k : kids) if (k != null) this.children.add(k);
        return this;
    }
}
