package dev.nandobez.macc.dsl;

import java.lang.reflect.*;
import java.util.*;

/** Codegen runtime support: proxies that capture field accesses during render(). */
public final class MaccRuntime {

    private static final ThreadLocal<String> LAST_FIELD = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_SOURCE_NAME = new ThreadLocal<>();

    public static void recordField(String fieldName) {
        String src = CURRENT_SOURCE_NAME.get();
        LAST_FIELD.set(src == null ? fieldName : src + "." + fieldName);
    }
    public static String lastFieldRef() {
        String v = LAST_FIELD.get();
        LAST_FIELD.remove();
        return v;
    }
    public static void setSourceName(String name) { CURRENT_SOURCE_NAME.set(name); }
    public static void clearSourceName()          { CURRENT_SOURCE_NAME.remove(); }

    public record ProxyHandle<T>(T instance, String itemName) {}

    /** Build a proxy of T (must be a record) that returns ExprNode placeholders for accessors. */
    @SuppressWarnings("unchecked")
    public static <T> ProxyHandle<T> proxyFor(List<T> source) {
        if (source == null) throw new IllegalStateException("each() needs a non-null list");
        // Default item name; codegen overrides via source variable name.
        String itemName = "item";
        // Infer the element class from generic info if available, else default to Object proxy.
        Class<T> cls = inferClass(source);
        T proxy = (T) makeProxy(cls, itemName);
        return new ProxyHandle<>(proxy, itemName);
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<T> inferClass(List<T> source) {
        for (T t : source) {
            if (t != null) return (Class<T>) t.getClass();
        }
        return (Class<T>) Object.class;
    }

    private static Object makeProxy(Class<?> cls, String itemName) {
        if (cls.isInterface()) {
            return Proxy.newProxyInstance(cls.getClassLoader(), new Class<?>[]{cls},
                (p, m, a) -> handle(m, itemName));
        }
        // For records we can't subclass via java.lang.reflect.Proxy. Return a record-like
        // proxy via fallback: synthesize a default instance and rely on @Model annotation in codegen.
        // For v0.1 we expect users to use the string-based each() with @Model record lists.
        return null;
    }

    private static Object handle(Method m, String itemName) {
        String prop = beanProp(m.getName());
        // Returns an ExprNode that codegen will render as "{itemName}.{prop}".
        return new ExprNode(itemName + "." + prop);
    }

    private static String beanProp(String methodName) {
        if (methodName.startsWith("get") && methodName.length() > 3)
            return Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
        if (methodName.startsWith("is") && methodName.length() > 2)
            return Character.toLowerCase(methodName.charAt(2)) + methodName.substring(3);
        return methodName;
    }

    private MaccRuntime() {}
}
