package ex1.siv.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ListUtil {
    private ListUtil() {
    }

    public static <E> boolean isNullOrEmpty(Collection<E> list) {
        return list == null || list.isEmpty();
    }

    public static <E> boolean isNullOrEmpty(E[] list) {
        return list == null || list.length == 0;
    }

    public static <E> Set<E> asSet(E[] list) {
        HashSet<E> ret = new HashSet<>();
        Collections.addAll(ret, list);
        return ret;
    }
}
