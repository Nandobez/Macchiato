package dev.nandobez.macc.dsl;

import java.util.*;

/** <>...</> — multiple siblings without a wrapper. */
public final class Fragment implements Element {
    public final List<Element> children;
    private Fragment(List<Element> children) { this.children = children; }

    public static Fragment of(Element... kids) {
        List<Element> list = new ArrayList<>();
        for (var k : kids) if (k != null) list.add(k);
        return new Fragment(list);
    }
}
