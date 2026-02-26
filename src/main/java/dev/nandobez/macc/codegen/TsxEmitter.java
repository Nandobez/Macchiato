package dev.nandobez.macc.codegen;

import dev.nandobez.macc.dsl.*;

/** Serializes a Macc Element tree to JSX source code. */
public class TsxEmitter {

    private final StringBuilder out = new StringBuilder();
    private int indent = 0;

    public String emit(Element root) {
        write(root);
        return out.toString();
    }

    private void write(Element e) {
        if (e == null) return;
        if (e instanceof Tag t)         writeTag(t);
        else if (e instanceof Fragment f) writeFragment(f);
        else if (e instanceof ExprNode x) out.append("{").append(x.code).append("}");
        else if (e instanceof EachNode n) writeEach(n);
        else if (e instanceof PickNode p) writePick(p);
        else if (e instanceof ComponentRef c) writeComponent(c);
        else                              out.append("/* ? ").append(e.getClass().getSimpleName()).append(" */");
    }

    private void writeTag(Tag t) {
        indent();
        out.append("<").append(t.name);
        if (t.keyValue != null)            attrInline("key", t.keyValue);
        for (var e : t.attrs.entrySet())   attrInline(e.getKey(), e.getValue());

        boolean hasChildren = !t.children.isEmpty() || t.textContent != null;
        if (!hasChildren) {
            out.append(" />");
            return;
        }
        out.append(">");

        if (t.textContent != null && t.children.isEmpty()) {
            writeText(t.textContent);
            out.append("</").append(t.name).append(">");
            return;
        }
        out.append("\n");
        indent++;
        if (t.textContent != null) {
            indent(); writeText(t.textContent); out.append("\n");
        }
        for (var c : t.children) {
            write(c);
            out.append("\n");
        }
        indent--;
        indent();
        out.append("</").append(t.name).append(">");
    }

    private void writeFragment(Fragment f) {
        indent(); out.append("<>\n");
        indent++;
        for (var c : f.children) { write(c); out.append("\n"); }
        indent--;
        indent(); out.append("</>");
    }

    private void writeEach(EachNode n) {
        indent();
        out.append("{").append(n.listExpr).append(".map((").append(n.itemName).append(") => (\n");
        indent++;
        write(n.body);
        out.append("\n");
        indent--;
        indent();
        out.append("))}");
    }

    private void writePick(PickNode p) {
        // emits inline expression (no surrounding braces; caller handles context).
        String cond = renderCond(p.cond);
        out.append(cond).append(" ? ").append(quote(p.ifTrue)).append(" : ").append(quote(p.ifFalse));
    }

    private void writeComponent(ComponentRef c) {
        indent();
        out.append("<").append(c.name);
        for (var e : c.props.entrySet()) attrInline(e.getKey(), e.getValue());
        if (c.children.isEmpty()) { out.append(" />"); return; }
        out.append(">\n");
        indent++;
        for (var k : c.children) { write(k); out.append("\n"); }
        indent--;
        indent();
        out.append("</").append(c.name).append(">");
    }

    private void attrInline(String name, Object value) {
        out.append(" ").append(name).append("=");
        if (value instanceof String s)             out.append("\"").append(escape(s)).append("\"");
        else if (value instanceof ExprNode x)      out.append("{").append(x.code).append("}");
        else if (value instanceof PickNode p)      { out.append("{"); writePick(p); out.append("}"); }
        else if (value instanceof ClassesNode cl)  { out.append("{"); writeClasses(cl); out.append("}"); }
        else if (value instanceof Boolean b)       out.append("{").append(b).append("}");
        else if (value instanceof Number n)        out.append("{").append(n).append("}");
        else                                       out.append("\"").append(escape(String.valueOf(value))).append("\"");
    }

    private void writeClasses(ClassesNode cl) {
        out.append("clsx(");
        boolean first = true;
        for (Object part : cl.parts) {
            if (part == null) continue;
            if (!first) out.append(", ");
            first = false;
            if (part instanceof WhenPart w) {
                if (w.cond instanceof Boolean b)  out.append(b ? "\"" + w.value + "\"" : "false");
                else if (w.cond instanceof ExprNode x) out.append("(").append(x.code).append(") && \"").append(w.value).append("\"");
                else out.append("\"").append(w.value).append("\"");
            } else if (part instanceof ExprNode x) {
                out.append(x.code);
            } else {
                out.append("\"").append(escape(String.valueOf(part))).append("\"");
            }
        }
        out.append(")");
    }

    private void writeText(Object t) {
        if (t instanceof ExprNode x) out.append("{").append(x.code).append("}");
        else if (t instanceof Number || t instanceof Boolean) out.append("{").append(t).append("}");
        else out.append(escape(String.valueOf(t)));
    }

    private static String renderCond(Object cond) {
        if (cond instanceof ExprNode x)         return x.code;
        if (cond instanceof Boolean b)          return b.toString();
        return String.valueOf(cond);
    }

    private static String quote(Object v) {
        if (v instanceof ExprNode x) return x.code;
        return "\"" + escape(String.valueOf(v)) + "\"";
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private void indent() { for (int i = 0; i < indent; i++) out.append("  "); }
}
