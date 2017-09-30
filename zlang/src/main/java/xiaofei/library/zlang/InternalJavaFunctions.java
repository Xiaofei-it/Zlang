package xiaofei.library.zlang;

/**
 * Created by Xiaofei on 2017/9/21.
 */

class InternalJavaFunctions extends JavaLibrary {

    static final InternalJavaFunctions INSTANCE = new InternalJavaFunctions();

    private InternalJavaFunctions() {
        addFunctions(new JavaFunction[]{
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
        });
    }

    private static class Test {
        private static class Add2 implements JavaFunction {
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

        private static class Add3 implements JavaFunction {
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
        private static class Equal implements JavaFunction {
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
        private static class Compare implements JavaFunction {
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
        private static class HashCode implements JavaFunction {
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
        private static class GetClass implements JavaFunction {
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
        private static class Get implements JavaFunction {
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

        private static class Set implements JavaFunction {
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

        private static class GetLength implements JavaFunction {
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

        private static class NewInstance implements JavaFunction {
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
        private static class ClassCast implements JavaFunction {
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

        private static class ForName implements JavaFunction {
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

        private static class GetName implements JavaFunction {
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

        private static class GetSimpleName implements JavaFunction {
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

        private static class GetCanonicalName implements JavaFunction {
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

        private static class GetConstructor  implements JavaFunction {
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

        private static class GetDeclaredConstructor implements JavaFunction {
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

        private static class GetField implements JavaFunction {
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

        private static class GetDeclaredField implements JavaFunction {
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

        private static class GetMethod implements JavaFunction {
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

        private static class GetDeclaredMethod implements JavaFunction {
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

        private static class GetSuperclass implements JavaFunction {
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

        private static class IsAnonymousClass implements JavaFunction {
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

        private static class IsArray implements JavaFunction {
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

        private static class IsAssignableFrom implements JavaFunction {
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

        private static class IsEnum implements JavaFunction {
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

        private static class InstanceOf implements JavaFunction {
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

        private static class IsInterface implements JavaFunction {
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

        private static class IsLocalClass implements JavaFunction {
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

        private static class IsMemberClass implements JavaFunction {
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

        private static class IsPrimitive implements JavaFunction {
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

        private static class NewInstance implements JavaFunction {
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