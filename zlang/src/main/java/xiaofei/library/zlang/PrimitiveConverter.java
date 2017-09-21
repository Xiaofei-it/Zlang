package xiaofei.library.zlang;

import java.util.HashMap;

/**
 * Created by zhaolifei on 2017/9/21.
 */

public class PrimitiveConverter {
    ////boolean, byte, char, short, int, long, float, and double
    private static final HashMap<Class<?>, Integer> PRIMITIVE_INT = new HashMap<Class<?>, Integer>() {
        {
            put(Byte.class, 1);
            put(Character.class, 2);
            put(Short.class, 3);
            put(Integer.class, 4);
            put(Long.class, 5);
            put(Float.class, 6);
            put(Double.class, 7);
        }
    };

    public static Object[] convert(Object o1, Object o2) {
        // TODO null????
        Class<?> c1 = o1.getClass();
        Class<?> c2 = o2.getClass();
        int i1 = PRIMITIVE_INT.get(c1);
        int i2 = PRIMITIVE_INT.get(c2);
        if (i1 < i2) {
            return new Object[]{c2.cast(o1), o2};
        } else if (i1 > i2) {
            return new Object[]{o1, c1.cast(o2)};
        } else {
            return new Object[]{o1, o2};
        }
    }

    static Class<?> get(Object o1, Object o2) {
        Class<?> c1 = o1.getClass();
        Class<?> c2 = o2.getClass();
        int i1 = PRIMITIVE_INT.get(c1);
        int i2 = PRIMITIVE_INT.get(c2);
        if (i1 < i2) {
            return c2;
        } else if (i1 > i2) {
            return c1;
        } else {
            return c1;
        }
    }
}
