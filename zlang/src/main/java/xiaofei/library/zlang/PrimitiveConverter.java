/**
 *
 * Copyright 2011-2017 Xiaofei
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package xiaofei.library.zlang;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Xiaofei on 2017/9/21.
 */

class PrimitiveConverter {
    ////boolean, byte, char, short, int, long, float, and double
    private static final ConcurrentHashMap<Class<?>, Integer> PRIMITIVE_INT = new ConcurrentHashMap<Class<?>, Integer>() {
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

    static Class<?> get(Object o1, Object o2) {
        Class<?> c1 = o1.getClass();
        Class<?> c2 = o2.getClass();
        Integer i1 = PRIMITIVE_INT.get(c1);
        Integer i2 = PRIMITIVE_INT.get(c2);
        if (i1 == null || i2 == null) {
            throw new IllegalArgumentException("Current operation does not support " + o1 + " or " + o2);
        }
        if (i1 < i2) {
            return c2;
        } else if (i1 > i2) {
            return c1;
        } else {
            return c1;
        }
    }
}
