package xiaofei.library.zlang;

import java.util.HashMap;

/**
 * Created by Xiaofei on 2017/9/21.
 */

class InternalFunctions {

    private static final HashMap<String, HashMap<Integer, Function>> FUNCTION_MAP
            = new HashMap<String, HashMap<Integer, Function>>() {
        {
            HashMap<Integer, Function> functions;

            functions = new HashMap<>();
            functions.put(2, new Test.Add2());
            functions.put(3, new Test.Add3());
            put("_test_add", functions);

            functions = new HashMap<>();
            functions.put(2, new ObjectMethods.Equal());
            put("_equal", functions);

            functions = new HashMap<>();
            functions.put(2, new ObjectMethods.Compare());
            put("_compare", functions);

            functions = new HashMap<>();
            functions.put(1, new ObjectMethods.HashCode());
            put("_hashcode", functions);

            functions = new HashMap<>();
            functions.put(1, new ObjectMethods.GetClass());
            put("_get_class", functions);

            functions = new HashMap<>();
            functions.put(3, new Array.Set1());
            functions.put(4, new Array.Set2());
            put("_array_set", functions);

            functions = new HashMap<>();
            functions.put(2, new Array.Get1());
            functions.put(3, new Array.Get2());
            put("_array_get", functions);

            functions = new HashMap<>();
            functions.put(2, new Array.NewInstance1());
            functions.put(3, new Array.NewInstance2());
            put("_new_array", functions);

            functions = new HashMap<>();
            functions.put(2, new Array.NewInstanceX());
            put("_new_array_x", functions);

            functions = new HashMap<>();
            functions.put(1, new Array.GetLength());
            put("_array_length", functions);
        }
    };

    static Object call(String functionName, Object[] input) {
        HashMap<Integer, Function> functions = FUNCTION_MAP.get(functionName);
        if (functions == null) {
            throw new IllegalArgumentException("Function " + functionName + " does not exist.");
        }
        Function function = functions.get(input.length);
        if (function == null) {
            throw new IllegalArgumentException("Function " + functionName + " does not have " + input.length + " parameter(s).");
        }
        return function.call(input);
    }

    private interface Function {
        Object call(Object[] input);
    }

    private static class Test {
        private static class Add2 implements Function {
            @Override
            public Object call(Object[] input) {
                return (int) input[0] + (int) input[1];
            }
        }

        private static class Add3 implements Function {
            @Override
            public Object call(Object[] input) {
                return (int) input[0] + (int) input[1] + (int) input[2];
            }
        }
    }

    private static class ObjectMethods {
        private static class Equal implements Function {
            @Override
            public Object call(Object[] input) {
                return input[0].equals(input[1]);
            }
        }
        private static class Compare implements Function {
            @Override
            public Object call(Object[] input) {
                if (input[0] instanceof Comparable) {
                    return ((Comparable) input[0]).compareTo(input[1]);
                } else {
                    throw new IllegalArgumentException(input[0] + " is not a comparable.");
                }
            }
        }
        private static class HashCode implements Function {
            @Override
            public Object call(Object[] input) {
                return input[0].hashCode();
            }
        }
        private static class GetClass implements Function {
            @Override
            public Object call(Object[] input) {
                return input[0].getClass();
            }
        }
    }

    private static class Array {
        private static class Set1 implements Function {
            @Override
            public Object call(Object[] input) {
                java.lang.reflect.Array.set(input[0], (int) input[1], input[2]);
                return null;
            }
        }

        private static class Set2 implements Function {
            @Override
            public Object call(Object[] input) {
                Object array = java.lang.reflect.Array.get(input[0], (int) input[1]);
                java.lang.reflect.Array.set(array, (int) input[2], input[3]);
                return null;
            }
        }

        private static class Get1 implements Function {
            @Override
            public Object call(Object[] input) {
                return java.lang.reflect.Array.get(input[0], (int) input[1]);
            }
        }

        private static class Get2 implements Function {
            @Override
            public Object call(Object[] input) {
                Object array = java.lang.reflect.Array.get(input[0], (int) input[1]);
                return java.lang.reflect.Array.get(array, (int) input[2]);
            }
        }

        private static class GetLength implements Function {
            @Override
            public Object call(Object[] input) {
                return java.lang.reflect.Array.getLength(input[0]);
            }
        }

        private static class NewInstance1 implements Function {
            @Override
            public Object call(Object[] input) {
                return java.lang.reflect.Array.newInstance((Class<?>) input[0], (int) input[1]);
            }
        }

        private static class NewInstance2 implements Function {
            @Override
            public Object call(Object[] input) {
                return java.lang.reflect.Array.newInstance((Class<?>) input[0], (int) input[1], (int) input[2]);
            }
        }

        private static class NewInstanceX implements Function {
            @Override
            public Object call(Object[] input) {
                Class<?> clazz = (Class<?>) input[0];
                int length = input.length - 1;
                int[] dimensions = new int[length];
                for (int i = 0; i < length; ++i) {
                    dimensions[i] = (int) input[i + 1];
                }
                return java.lang.reflect.Array.newInstance(clazz, dimensions);
            }
        }
    }
}
