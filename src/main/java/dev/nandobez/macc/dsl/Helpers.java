package dev.nandobez.macc.dsl;

import java.util.function.Function;

public final class Helpers {

    /** Raw TS expression — escape hatch. Used inside attrs / values. */
    public static ExprNode $(String code)   { return new ExprNode(code); }
    public static ExprNode expr(String code){ return new ExprNode(code); }

    /** {listExpr.map(item => body)} — string-based form. */
    public static EachNode each(String listExpr, String itemName, Element body) {
        return new EachNode(listExpr, itemName, body);
    }

    /** Typed form: each(users, user -> ...). Uses introspection on the @Model proxy. */
    public static <T> EachNode each(java.util.List<T> source, Function<T, Element> body) {
        // The codegen detects this call shape and replaces with a proxy invocation.
        // At runtime (for codegen execution) we use a Proxy<T> to capture field accesses.
        var proxy = MaccRuntime.proxyFor(source);
        Element rendered = body.apply(proxy.instance());
        String listExpr = MaccRuntime.lastFieldRef();
        if (listExpr == null) listExpr = "items";
        return new EachNode(listExpr, proxy.itemName(), rendered);
    }

    /** pick(cond, ifTrue, ifFalse) — inline ternary. */
    public static PickNode pick(boolean cond, String ifTrue, String ifFalse) {
        return new PickNode(cond, ifTrue, ifFalse, true);
    }
    public static PickNode pick(Object cond, String ifTrue, String ifFalse) {
        return new PickNode(cond, ifTrue, ifFalse, false);
    }

    /** classes() helper for inline conditional class. when(cond, "css"). */
    public static WhenPart when(boolean cond, String value) {
        return new WhenPart(cond, value);
    }
    public static WhenPart when(Object exprCond, String value) {
        return new WhenPart(exprCond, value);
    }

    /** Fragment factory shortcut. */
    public static Fragment group(Element... kids) { return Fragment.of(kids); }

    /** Render another component as a JSX child. */
    public static ComponentRef render(Class<?> componentClass) {
        return new ComponentRef(componentClass.getSimpleName(), null);
    }

    private Helpers() {}
}
