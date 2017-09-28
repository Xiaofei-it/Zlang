package xiaofei.library.zlang;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by Xiaofei on 2017/9/21.
 */

class InternalFunctions {
    private static final HashMap<String, HashMap<Integer, Function>> FIXED_ARGS_FUNCTIONS
            = new HashMap<String, HashMap<Integer, Function>>();

    private static final HashMap<String, LinkedList<Function>> VAR_ARGS_FUNCTIONS
            = new HashMap<String, LinkedList<Function>>();

    static {
        Function[] functions = new Function[]{
                new Test.Add2(),
                new Test.Add3(),

                new Array.Get(),
                new Array.GetLength(),
                new Array.Set(),
                new Array.NewInstance(),

                new ObjectMethods.HashCode(),
                new ObjectMethods.Compare(),
                new ObjectMethods.Equal(),
                new ObjectMethods.GetClass(),

                new Clazz.ClassCast(),
                new Clazz.ForName(),
                new Clazz.GetCanonicalName(),
                new Clazz.GetSimpleName(),
                new Clazz.GetName(),
                new Clazz.GetConstructor(),
                new Clazz.GetDeclaredConstructor(),
                new Clazz.GetMethod(),
                new Clazz.GetDeclaredMethod(),
                new Clazz.GetField(),
                new Clazz.GetDeclaredField(),
                new Clazz.GetSuperclass(),
                new Clazz.InstanceOf(),
                new Clazz.IsAnonymousClass(),
                new Clazz.IsArray(),
                new Clazz.IsAssignableFrom(),
                new Clazz.IsEnum(),
                new Clazz.IsLocalClass(),
                new Clazz.IsPrimitive(),
                new Clazz.IsMemberClass(),
                new Clazz.IsInterface(),
                new Clazz.NewInstance(),
        };
        for (Function function : functions) {
            String name = function.getFunctionName();
            if (function.isVarArgs()) {
                LinkedList<Function> list = VAR_ARGS_FUNCTIONS.get(name);
                if (list == null) {
                    list = new LinkedList<>();
                    VAR_ARGS_FUNCTIONS.put(name, list);
                }
                list.add(function);
            } else {
                HashMap<Integer, Function> map = FIXED_ARGS_FUNCTIONS.get(name);
                if (map == null) {
                    map = new HashMap<>();
                    FIXED_ARGS_FUNCTIONS.put(name, map);
                }
                map.put(function.getParameterNumber(), function);
            }
        }
    }

    static Object call(String functionName, Object[] input) {
        HashMap<Integer, Function> functionMap = FIXED_ARGS_FUNCTIONS.get(functionName);
        if (functionMap != null) {
            Function function = functionMap.get(input.length);
            if (function != null) {
                return function.call(input);
            }
        }
        LinkedList<Function> functionList = VAR_ARGS_FUNCTIONS.get(functionName);
        if (functionList == null) {
            throw new IllegalArgumentException("Function " + functionName + " does not exist.");
        }
        for (Function function : functionList) {
            if (input.length >= function.getParameterNumber()) {
                return function.call(input);
            }
        }
        throw new IllegalArgumentException("Function " + functionName + " does not have " + input.length + " parameter(s).");
    }

    private interface Function {
        boolean isVarArgs();
        int getParameterNumber();
        String getFunctionName();
        Object call(Object[] input);
    }

    private static class Test {
        private static class Add2 implements Function {
            @Override
            public boolean isVarArgs() {
                return false;
            }

            @Override
            public int getParameterNumber() {
                return 2;
            }

            @Override
            public String getFunctionName() {
                return "_test_add";
            }

            @Override
            public Object call(Object[] input) {
                return (int) input[0] + (int) input[1];
            }
        }

        private static class Add3 implements Function {
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public int getParameterNumber() {
                return 3;
            }

            @Override
            public String getFunctionName() {
                return "_test_add";
            }

            @Override
            public Object call(Object[] input) {
                return (int) input[0] + (int) input[1] + (int) input[2];
            }
        }
    }

    private static class ObjectMethods {
        private static class Equal implements Function {
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public int getParameterNumber() {
                return 2;
            }

            @Override
            public String getFunctionName() {
                return "_equal";
            }

            @Override
            public Object call(Object[] input) {
                return input[0].equals(input[1]);
            }
        }
        private static class Compare implements Function {
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public int getParameterNumber() {
                return 2;
            }

            @Override
            public String getFunctionName() {
                return "_compare";
            }

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
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public int getParameterNumber() {
                return 1;
            }

            @Override
            public String getFunctionName() {
                return "_hashcode";
            }

            @Override
            public Object call(Object[] input) {
                return input[0].hashCode();
            }
        }
        private static class GetClass implements Function {
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public int getParameterNumber() {
                return 1;
            }

            @Override
            public String getFunctionName() {
                return "_get_class";
            }

            @Override
            public Object call(Object[] input) {
                return input[0].getClass();
            }
        }
    }

    private static class Array {
        private static class Get implements Function {
            @Override
            public boolean isVarArgs() {
                return true;
            }
            @Override
            public int getParameterNumber() {
                return 2;
            }

            @Override
            public String getFunctionName() {
                return "_array_set";
            }

            @Override
            public Object call(Object[] input) {
                Object array = input[0];
                int length = input.length;
                for (int i = 1; i <= length - 2; ++i) {
                    array = java.lang.reflect.Array.get(array, (int) input[i]);
                }
                return java.lang.reflect.Array.get(array, (int) input[length - 1]);
            }
        }

        private static class Set implements Function {
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public int getParameterNumber() {
                return 3;
            }

            @Override
            public String getFunctionName() {
                return "_array_set";
            }

            @Override
            public Object call(Object[] input) {
                // array, i, o
                Object array = input[0];
                int length = input.length;
                for (int i = 1; i <= length - 3; ++i) {
                    array = java.lang.reflect.Array.get(array, (int) input[i]);
                }
                java.lang.reflect.Array.set(array, (int) input[length - 2], input[length - 1]);
                return null;
            }
        }

        private static class GetLength implements Function {
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public int getParameterNumber() {
                return 1;
            }

            @Override
            public String getFunctionName() {
                return "_array_length";
            }

            @Override
            public Object call(Object[] input) {
                return java.lang.reflect.Array.getLength(input[0]);
            }
        }

        private static class NewInstance implements Function {
            @Override
            public boolean isVarArgs() {
                return true;
            }
            @Override
            public int getParameterNumber() {
                return 2;
            }

            @Override
            public String getFunctionName() {
                return "_new_array";
            }

            @Override
            public Object call(Object[] input) {
                Class<?> clazz = (Class<?>) input[0];
                int length = input.length - 1;
                if (length == 0) {
                    throw new IllegalArgumentException();
                }
                int[] dimensions = new int[length];
                for (int i = 0; i < length; ++i) {
                    dimensions[i] = (int) input[i + 1];
                }
                return java.lang.reflect.Array.newInstance(clazz, dimensions);
            }
        }
    }

    private static class Clazz {
        private static class ClassCast implements Function {
            @Override
            public int getParameterNumber() {
                return 2;
            }

            @Override
            public String getFunctionName() {
                return "_class_cast";
            }

            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public Object call(Object[] input) {
                return ((Class<?>) input[0]).cast(input[1]);
            }
        }

        private static class ForName implements Function {
            @Override
            public boolean isVarArgs() {
                return false;
            }

            @Override
            public int getParameterNumber() {
                return 1;
            }

            @Override
            public String getFunctionName() {
                return "_for_name";
            }

            @Override
            public Object call(Object[] input) {
                try {
                    return Class.forName((String) input[0]);
                } catch (ClassNotFoundException e) {
                    throw new IllegalArgumentException("Class not found: " + input[0], e);
                }
            }
        }

        private static class GetName implements Function {
            @Override
            public boolean isVarArgs() {
                return false;
            }

            @Override
            public int getParameterNumber() {
                return 1;
            }

            @Override
            public String getFunctionName() {
                return "_class_get_name";
            }

            @Override
            public Object call(Object[] input) {
                return ((Class<?>) input[0]).getName();
            }
        }

        private static class GetSimpleName implements Function {
            @Override
            public boolean isVarArgs() {
                return false;
            }

            @Override
            public int getParameterNumber() {
                return 1;
            }

            @Override
            public String getFunctionName() {
                return "_class_get_simple_name";
            }

            @Override
            public Object call(Object[] input) {
                return ((Class<?>) input[0]).getSimpleName();
            }
        }

        private static class GetCanonicalName implements Function {
            @Override
            public boolean isVarArgs() {
                return false;
            }

            @Override
            public int getParameterNumber() {
                return 1;
            }

            @Override
            public String getFunctionName() {
                return "_class_get_canonical_name";
            }

            @Override
            public Object call(Object[] input) {
                return ((Class<?>) input[0]).getCanonicalName();
            }
        }

        private static class GetConstructor  implements Function {
            @Override
            public boolean isVarArgs() {
                return true;
            }

            @Override
            public int getParameterNumber() {
                return 1;
            }

            @Override
            public String getFunctionName() {
                return "_class_get_constructor";
            }

            @Override
            public Object call(Object[] input) {
                int length = input.length;
                Class<?>[] classes = new Class<?>[length - 1];
                for (int i = 0; i < length - 1; ++i) {
                    classes[i] = (Class<?>) input[i + 1];
                }
                try {
                    return ((Class<?>) input[0]).getConstructor(classes);
                } catch (NoSuchMethodException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        }

        private static class GetDeclaredConstructor implements Function {
            @Override
            public boolean isVarArgs() {
                return true;
            }

            @Override
            public int getParameterNumber() {
                return 1;
            }

            @Override
            public String getFunctionName() {
                return "_class_get_declared_constructor";
            }

            @Override
            public Object call(Object[] input) {
                int length = input.length;
                Class<?>[] classes = new Class<?>[length - 1];
                for (int i = 0; i < length - 1; ++i) {
                    classes[i] = (Class<?>) input[i + 1];
                }
                try {
                    return ((Class<?>) input[0]).getDeclaredConstructor(classes);
                } catch (NoSuchMethodException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        }

        private static class GetField implements Function {
            @Override
            public boolean isVarArgs() {
                return false;
            }

            @Override
            public int getParameterNumber() {
                return 2;
            }

            @Override
            public String getFunctionName() {
                return "_class_get_field";
            }

            @Override
            public Object call(Object[] input) {
                try {
                    return ((Class<?>) input[0]).getField((String) input[1]);
                } catch (NoSuchFieldException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        }

        private static class GetDeclaredField implements Function {
            @Override
            public boolean isVarArgs() {
                return false;
            }

            @Override
            public int getParameterNumber() {
                return 2;
            }

            @Override
            public String getFunctionName() {
                return "_class_get_declared_field";
            }

            @Override
            public Object call(Object[] input) {
                try {
                    return ((Class<?>) input[0]).getDeclaredField((String) input[1]);
                } catch (NoSuchFieldException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        }

        private static class GetMethod implements Function {
            @Override
            public boolean isVarArgs() {
                return true;
            }

            @Override
            public int getParameterNumber() {
                return 2;
            }

            @Override
            public String getFunctionName() {
                return "_class_get_method";
            }

            @Override
            public Object call(Object[] input) {
                // class, string, class...
                int length = input.length;
                Class<?>[] classes = new Class<?>[length - 2];
                for (int i = 0; i < length - 2; ++i) {
                    classes[i] = (Class<?>) input[i + 2];
                }
                try {
                    return ((Class<?>) input[0]).getMethod((String) input[1], classes);
                } catch (NoSuchMethodException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        }

        private static class GetDeclaredMethod implements Function {
            @Override
            public boolean isVarArgs() {
                return true;
            }

            @Override
            public int getParameterNumber() {
                return 2;
            }

            @Override
            public String getFunctionName() {
                return "_class_get_declared_method";
            }

            @Override
            public Object call(Object[] input) {
                // class, string, class...
                int length = input.length;
                Class<?>[] classes = new Class<?>[length - 2];
                for (int i = 0; i < length - 2; ++i) {
                    classes[i] = (Class<?>) input[i + 2];
                }
                try {
                    return ((Class<?>) input[0]).getDeclaredMethod((String) input[1], classes);
                } catch (NoSuchMethodException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        }

        private static class GetSuperclass implements Function {
            @Override
            public boolean isVarArgs() {
                return false;
            }

            @Override
            public int getParameterNumber() {
                return 1;
            }

            @Override
            public String getFunctionName() {
                return "_class_get_superclass";
            }

            @Override
            public Object call(Object[] input) {
                return ((Class<?>) input[0]).getSuperclass();
            }
        }

        private static class IsAnonymousClass implements Function {
            @Override
            public boolean isVarArgs() {
                return false;
            }

            @Override
            public int getParameterNumber() {
                return 1;
            }

            @Override
            public String getFunctionName() {
                return "_is_anonymous_class";
            }

            @Override
            public Object call(Object[] input) {
                return ((Class<?>) input[0]).isAnonymousClass();
            }
        }

        private static class IsArray implements Function {
            @Override
            public boolean isVarArgs() {
                return false;
            }

            @Override
            public int getParameterNumber() {
                return 1;
            }

            @Override
            public String getFunctionName() {
                return "_is_array";
            }

            @Override
            public Object call(Object[] input) {
                return ((Class<?>) input[0]).isArray();
            }
        }

        private static class IsAssignableFrom implements Function {
            @Override
            public boolean isVarArgs() {
                return false;
            }

            @Override
            public int getParameterNumber() {
                return 2;
            }

            @Override
            public String getFunctionName() {
                return "_is_assignable_from";
            }

            @Override
            public Object call(Object[] input) {
                return ((Class<?>) input[0]).isAssignableFrom((Class<?>) input[1]);
            }
        }

        private static class IsEnum implements Function {
            @Override
            public boolean isVarArgs() {
                return false;
            }

            @Override
            public int getParameterNumber() {
                return 1;
            }

            @Override
            public String getFunctionName() {
                return "_is_enum";
            }

            @Override
            public Object call(Object[] input) {
                return ((Class<?>) input[0]).isEnum();
            }
        }

        private static class InstanceOf implements Function {
            @Override
            public boolean isVarArgs() {
                return false;
            }

            @Override
            public int getParameterNumber() {
                return 2;
            }

            @Override
            public String getFunctionName() {
                return "_instance_of";
            }

            @Override
            public Object call(Object[] input) {
                return ((Class<?>) input[1]).isInstance(input[0]);
            }
        }

        private static class IsInterface implements Function {
            @Override
            public boolean isVarArgs() {
                return false;
            }

            @Override
            public int getParameterNumber() {
                return 1;
            }

            @Override
            public String getFunctionName() {
                return "_is_interface";
            }

            @Override
            public Object call(Object[] input) {
                return ((Class<?>) input[0]).isInterface();
            }
        }

        private static class IsLocalClass implements Function {
            @Override
            public boolean isVarArgs() {
                return false;
            }

            @Override
            public int getParameterNumber() {
                return 1;
            }

            @Override
            public String getFunctionName() {
                return "_is_local_class";
            }

            @Override
            public Object call(Object[] input) {
                return ((Class<?>) input[0]).isLocalClass();
            }
        }

        private static class IsMemberClass implements Function {
            @Override
            public boolean isVarArgs() {
                return false;
            }

            @Override
            public int getParameterNumber() {
                return 1;
            }

            @Override
            public String getFunctionName() {
                return "_is_member_class";
            }

            @Override
            public Object call(Object[] input) {
                return ((Class<?>) input[0]).isMemberClass();
            }
        }

        private static class IsPrimitive implements Function {
            @Override
            public boolean isVarArgs() {
                return false;
            }

            @Override
            public int getParameterNumber() {
                return 1;
            }

            @Override
            public String getFunctionName() {
                return "_is_primitive";
            }

            @Override
            public Object call(Object[] input) {
                return ((Class<?>) input[0]).isPrimitive();
            }
        }

        private static class NewInstance implements Function {
            @Override
            public boolean isVarArgs() {
                return false;
            }

            @Override
            public int getParameterNumber() {
                return 1;
            }

            @Override
            public String getFunctionName() {
                return "_new_instance";
            }

            @Override
            public Object call(Object[] input) {
                try {
                    return ((Class<?>) input[0]).newInstance();
                } catch (InstantiationException e) {
                    throw new IllegalArgumentException(e);
                } catch (IllegalAccessException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        }
    }



    // TODO annotation
}