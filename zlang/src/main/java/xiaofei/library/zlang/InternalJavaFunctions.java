package xiaofei.library.zlang;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Xiaofei on 2017/9/21.
 */

class InternalJavaFunctions extends JavaLibrary {

    static final InternalJavaFunctions INSTANCE = new InternalJavaFunctions();

    private InternalJavaFunctions() {
        addFunctions(new JavaFunction[]{
                new Test.Add2(),
                new Test.Add3(),

                new Array.GetLength(),
                new Array.NewArray(),
                new Array.ArrayOf(),

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
                new Clazz.GetEnclosingClass(),
                new Clazz.GetDeclaringClass(),
                new Clazz.GetEnclosingMethod(),

                new Method.GetName(),
                new Method.GetReturnType(),
                new Method.GetParameterTypes(),
                new Method.Invoke(),

                new Field.Get(),
                new Field.Set(),
                new Field.GetName(),
                new Field.GetType(),

                new Constructor.GetName(),
                new Constructor.GetParameterTypes(),
                new Constructor.NewInstance(),

                new Map.ContainsKey(),
                new Map.ContainsValue(),
                new Map.Get(),
                new Map.Put(),

                new List.Get(),
                new List.Set(),

                new Collection.Add(),
                new Collection.IsEmpty(),
                new Collection.NewList(),
                new Collection.NewMap(),
                new Collection.NewSet(),
                new Collection.Remove(),
                new Collection.Size(),

                new Reference.SoftRef(),
                new Reference.WeakRef(),

                new Output.Print(),
                new Output.Println(),
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
                    throw new ZlangRuntimeException(ZlangRuntimeError.ILLEGAL_ARGUMENT, input[0] + " is not a comparable.");
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
                return "_length";
            }

            @Override
            public Object call(Object[] input) {
                return java.lang.reflect.Array.getLength(input[0]);
            }
        }

        private static class ArrayOf implements JavaFunction {
            @Override
            public boolean isVarArgs() {
                return true;
            }
            @Override
            public int getParameterNumber() {
                return 0;
            }

            @Override
            public String getFunctionName() {
                return "_array_of";
            }

            @Override
            public Object call(Object[] input) {
                return input;
            }
        }

        private static class NewArray implements JavaFunction {
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
                Class<?> clazz;
                if (input[0] instanceof Class<?>) {
                    clazz = (Class<?>) input[0];
                } else if (input[0] instanceof String) {
                    try {
                        clazz = Class.forName((String) input[0]);
                    } catch (ClassNotFoundException e) {
                        throw new ZlangRuntimeException(ZlangRuntimeError.CLASS_NOT_FOUND, "" + input[0]);
                    }
                } else {
                    throw new ZlangRuntimeException(ZlangRuntimeError.ILLEGAL_ARGUMENT, "" + input[0]);
                }
                if (input.length == 2 && input[1] instanceof int[]) {
                    return java.lang.reflect.Array.newInstance(clazz, (int[]) input[1]);
                }
                int length = input.length - 1;
                if (length == 0) {
                    throw new ZlangRuntimeException(ZlangRuntimeError.ILLEGAL_ARGUMENT, "No dimensions specified.");
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
                    throw new ZlangRuntimeException(ZlangRuntimeError.CLASS_NOT_FOUND, input[0].toString());
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
                if (length == 2 && input[1] instanceof Class<?>[]) {
                    try {
                        return ((Class<?>) input[0]).getConstructor((Class<?>[]) input[1]);
                    } catch (NoSuchMethodException e) {
                        throw new ZlangRuntimeException(ZlangRuntimeError.NO_SUCH_CONSTRUCTOR, input[0].toString());
                    }
                }
                Class<?>[] classes = new Class<?>[length - 1];
                for (int i = 0; i < length - 1; ++i) {
                    classes[i] = (Class<?>) input[i + 1];
                }
                try {
                    return ((Class<?>) input[0]).getConstructor(classes);
                } catch (NoSuchMethodException e) {
                    throw new ZlangRuntimeException(ZlangRuntimeError.NO_SUCH_CONSTRUCTOR, input[0].toString());
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
                if (length == 2 && input[1] instanceof Class<?>[]) {
                    try {
                        return ((Class<?>) input[0]).getDeclaredConstructor((Class<?>[]) input[1]);
                    } catch (NoSuchMethodException e) {
                        throw new ZlangRuntimeException(ZlangRuntimeError.NO_SUCH_CONSTRUCTOR, input[0].toString());
                    }
                }
                Class<?>[] classes = new Class<?>[length - 1];
                for (int i = 0; i < length - 1; ++i) {
                    classes[i] = (Class<?>) input[i + 1];
                }
                try {
                    return ((Class<?>) input[0]).getDeclaredConstructor(classes);
                } catch (NoSuchMethodException e) {
                    throw new ZlangRuntimeException(ZlangRuntimeError.NO_SUCH_CONSTRUCTOR, input[0].toString());
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
                    throw new ZlangRuntimeException(ZlangRuntimeError.NO_SUCH_FIELD, "Class: " + input[0] + " Field: " + input[1]);
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
                    throw new ZlangRuntimeException(ZlangRuntimeError.NO_SUCH_FIELD, "Class: " + input[0] + " Field: " + input[1]);
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
                if (length == 3 && input[2] instanceof Class<?>[]) {
                    try {
                        return ((Class<?>) input[0]).getMethod((String) input[1], (Class<?>[]) input[2]);
                    } catch (NoSuchMethodException e) {
                        throw new ZlangRuntimeException(ZlangRuntimeError.NO_SUCH_METHOD, "Class: " + input[0] + " Method: " + input[1]);
                    }
                }
                Class<?>[] classes = new Class<?>[length - 2];
                for (int i = 0; i < length - 2; ++i) {
                    classes[i] = (Class<?>) input[i + 2];
                }
                try {
                    return ((Class<?>) input[0]).getMethod((String) input[1], classes);
                } catch (NoSuchMethodException e) {
                    throw new ZlangRuntimeException(ZlangRuntimeError.NO_SUCH_METHOD, "Class: " + input[0] + " Method: " + input[1]);
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
                if (length == 3 && input[2] instanceof Class<?>[]) {
                    try {
                        return ((Class<?>) input[0]).getDeclaredMethod((String) input[1], (Class<?>[]) input[2]);
                    } catch (NoSuchMethodException e) {
                        throw new ZlangRuntimeException(ZlangRuntimeError.NO_SUCH_METHOD, "Class: " + input[0] + " Method: " + input[1]);
                    }
                }
                Class<?>[] classes = new Class<?>[length - 2];
                for (int i = 0; i < length - 2; ++i) {
                    classes[i] = (Class<?>) input[i + 2];
                }
                try {
                    return ((Class<?>) input[0]).getDeclaredMethod((String) input[1], classes);
                } catch (NoSuchMethodException e) {
                    throw new ZlangRuntimeException(ZlangRuntimeError.NO_SUCH_METHOD, "Class: " + input[0] + " Method: " + input[1]);
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

        private static class GetEnclosingClass implements JavaFunction {
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
                return "_get_enclosing_class";
            }

            @Override
            public Object call(Object[] input) {
                return ((Class<?>) input[0]).getEnclosingClass();
            }
        }

        private static class GetDeclaringClass implements JavaFunction {
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
                return "_get_declaring_class";
            }

            @Override
            public Object call(Object[] input) {
                return ((Class<?>) input[0]).getDeclaringClass();
            }
        }

        private static class GetEnclosingMethod implements JavaFunction {
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
                return "_get_enclosing_method";
            }

            @Override
            public Object call(Object[] input) {
                return ((Class<?>) input[0]).getEnclosingMethod();
            }
        }
    }

    private static class Field {

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
                return "_field_name";
            }

            @Override
            public Object call(Object[] input) {
                return ((java.lang.reflect.Field) input[0]).getName();
            }
        }

        private static class GetType implements JavaFunction {
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
                return "_field_type";
            }

            @Override
            public Object call(Object[] input) {
                return ((java.lang.reflect.Field) input[0]).getType();
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
                return "_field_set";
            }

            @Override
            public Object call(Object[] input) {
                java.lang.reflect.Field field = (java.lang.reflect.Field) input[0];
                try {
                    field.setAccessible(true);
                    field.set(input[1], input[2]);
                    return null;
                } catch (IllegalAccessException e) {
                    throw new ZlangRuntimeException(ZlangRuntimeError.FIELD_SET_ERROR, field.toString());
                }
            }
        }

        private static class Get implements JavaFunction {
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
                return "_field_get";
            }

            @Override
            public Object call(Object[] input) {
                java.lang.reflect.Field field = (java.lang.reflect.Field) input[0];
                try {
                    field.setAccessible(true);
                    return field.get(input[1]);
                } catch (IllegalAccessException e) {
                    throw new ZlangRuntimeException(ZlangRuntimeError.FIELD_GET_ERROR, field.toString());
                }
            }
        }
    }

    private static class Method {

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
                return "_method_name";
            }

            @Override
            public Object call(Object[] input) {
                return ((java.lang.reflect.Method) input[0]).getName();
            }
        }

        private static class GetParameterTypes implements JavaFunction {
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
                return "_method_parameter_types";
            }

            @Override
            public Object call(Object[] input) {
                return ((java.lang.reflect.Method) input[0]).getParameterTypes();
            }
        }

        private static class GetReturnType implements JavaFunction {
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
                return "_method_return_type";
            }

            @Override
            public Object call(Object[] input) {
                return ((java.lang.reflect.Method) input[0]).getReturnType();
            }
        }

        private static class Invoke implements JavaFunction {
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
                return "_method_invoke";
            }

            @Override
            public Object call(Object[] input) {
                int length = input.length - 2;
                if (length == 3 && input[2] instanceof Object[]) {
                    java.lang.reflect.Method method = (java.lang.reflect.Method) input[0];
                    try {
                        method.setAccessible(true);
                        return method.invoke(input[1], (Object[]) input[2]);
                    } catch (IllegalAccessException e) {
                        throw new ZlangRuntimeException(ZlangRuntimeError.METHOD_INVOCATION_ERROR, method.toString());
                    } catch (InvocationTargetException e) {
                        throw new ZlangRuntimeException(ZlangRuntimeError.METHOD_INVOCATION_ERROR, method.toString());
                    }
                }
                Object[] parameters = new Object[length];
                for (int i = 0; i < length; ++i) {
                    parameters[i] = input[i + 2];
                }
                java.lang.reflect.Method method = (java.lang.reflect.Method) input[0];
                try {
                    method.setAccessible(true);
                    return method.invoke(input[1], parameters);
                } catch (IllegalAccessException e) {
                    throw new ZlangRuntimeException(ZlangRuntimeError.METHOD_INVOCATION_ERROR, method.toString());
                } catch (InvocationTargetException e) {
                    throw new ZlangRuntimeException(ZlangRuntimeError.METHOD_INVOCATION_ERROR, method.toString());
                }
            }
        }
    }

    private static class Constructor {
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
                return "_constructor_name";
            }

            @Override
            public Object call(Object[] input) {
                return ((java.lang.reflect.Constructor) input[0]).getName();
            }
        }

        private static class GetParameterTypes implements JavaFunction {
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
                return "_constructor_parameter_types";
            }

            @Override
            public Object call(Object[] input) {
                return ((java.lang.reflect.Constructor) input[0]).getParameterTypes();
            }
        }

        private static class NewInstance implements JavaFunction {
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
                return "_constructor_new_instance";
            }

            @Override
            public Object call(Object[] input) {
                int length = input.length - 1;
                java.lang.reflect.Constructor constructor = (java.lang.reflect.Constructor) input[0];
                try {
                    if (length == 2 && input[1] instanceof Object[]) {
                        constructor.setAccessible(true);
                        return constructor.newInstance((Object[]) input[1]);
                    }
                    Object[] parameters = new Object[length];
                    for (int i = 0; i < length; ++i) {
                        parameters[i] = input[i + 1];
                    }
                    constructor.setAccessible(true);
                    return constructor.newInstance(parameters);
                } catch (InvocationTargetException e) {
                    throw new ZlangRuntimeException(ZlangRuntimeError.NEW_INSTANCE_ERROR, constructor.toString());
                } catch (InstantiationException e) {
                    throw new ZlangRuntimeException(ZlangRuntimeError.NEW_INSTANCE_ERROR, constructor.toString());
                } catch (IllegalAccessException e) {
                    throw new ZlangRuntimeException(ZlangRuntimeError.NEW_INSTANCE_ERROR, constructor.toString());
                }
            }
        }
    }

    private static class List {
        private static class Get implements JavaFunction {
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
                return "_list_get";
            }

            @Override
            public Object call(Object[] input) {
                return ((java.util.List) input[0]).get((int) input[1]);
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
                return "_list_set";
            }

            @Override
            public Object call(Object[] input) {
                ((java.util.List) input[0]).set((int) input[1], input[2]);
                return null;
            }
        }
    }

    private static class Map {
        private static class Put implements JavaFunction {
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
                return "_map_put";
            }

            @Override
            public Object call(Object[] input) {
                ((java.util.Map) input[0]).put(input[1], input[2]);
                return null;
            }
        }

        private static class Get implements JavaFunction {
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
                return "_map_get";
            }

            @Override
            public Object call(Object[] input) {
                ((java.util.Map) input[0]).get(input[1]);
                return null;
            }
        }

        private static class ContainsKey implements JavaFunction {
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
                return "_map_get";
            }

            @Override
            public Object call(Object[] input) {
                ((java.util.Map) input[0]).containsKey(input[1]);
                return null;
            }
        }

        private static class ContainsValue implements JavaFunction {
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
                return "_map_get";
            }

            @Override
            public Object call(Object[] input) {
                ((java.util.Map) input[0]).containsValue(input[1]);
                return null;
            }
        }
    }

    private static class Collection {
        private static class NewSet implements JavaFunction {
            @Override
            public boolean isVarArgs() {
                return false;
            }

            @Override
            public int getParameterNumber() {
                return 0;
            }

            @Override
            public String getFunctionName() {
                return "_new_set";
            }

            @Override
            public Object call(Object[] input) {
                return new HashSet<>();
            }
        }

        private static class NewMap implements JavaFunction {
            @Override
            public boolean isVarArgs() {
                return false;
            }

            @Override
            public int getParameterNumber() {
                return 0;
            }

            @Override
            public String getFunctionName() {
                return "_new_map";
            }

            @Override
            public Object call(Object[] input) {
                return new HashMap<>();
            }
        }

        private static class NewList implements JavaFunction {
            @Override
            public boolean isVarArgs() {
                return false;
            }

            @Override
            public int getParameterNumber() {
                return 0;
            }

            @Override
            public String getFunctionName() {
                return "_new_list";
            }

            @Override
            public Object call(Object[] input) {
                return new ArrayList<>();
            }
        }

        private static class Size implements JavaFunction {
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
                return "_size";
            }

            @Override
            public Object call(Object[] input) {
                Object o = input[0];
                if (o instanceof java.util.Map) {
                    return ((java.util.Map) o).size();
                }
                if (o instanceof java.util.Collection) {
                    return ((java.util.Collection) o).size();
                }
                // In case that o is null.
                throw new ZlangRuntimeException(ZlangRuntimeError.ILLEGAL_ARGUMENT, "" + input[0]);
            }
        }

        private static class IsEmpty implements JavaFunction {
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
                return "_is_empty";
            }

            @Override
            public Object call(Object[] input) {
                Object o = input[0];
                if (o instanceof java.util.Map) {
                    return ((java.util.Map) o).isEmpty();
                }
                if (o instanceof java.util.Collection) {
                    return ((java.util.Collection) o).isEmpty();
                }
                // In case that o is null.
                throw new ZlangRuntimeException(ZlangRuntimeError.ILLEGAL_ARGUMENT, "" + input[0]);
            }
        }

        private static class Add implements JavaFunction {
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
                return "_add";
            }

            @Override
            public Object call(Object[] input) {
                Object o = input[0];
                if (o instanceof java.util.Collection) {
                    return ((java.util.Collection) o).add(input[1]);
                }
                // In case that o is null.
                throw new ZlangRuntimeException(ZlangRuntimeError.ILLEGAL_ARGUMENT, "" + input[0]);
            }
        }

        private static class Remove implements JavaFunction {
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
                return "_is_empty";
            }

            @Override
            public Object call(Object[] input) {
                Object o = input[0];
                if (o instanceof java.util.Collection) {
                    return ((java.util.Collection) o).remove(input[1]);
                }
                // In case that o is null.
                throw new ZlangRuntimeException(ZlangRuntimeError.ILLEGAL_ARGUMENT, "" + input[0]);
            }
        }
    }

    private static class Reference {
        private static class WeakRef implements JavaFunction {
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
                return "_weak_ref";
            }

            @Override
            public Object call(Object[] input) {
                return new WeakReference<>(input[0]);
            }
        }

        private static class SoftRef implements JavaFunction {
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
                return "_soft_ref";
            }

            @Override
            public Object call(Object[] input) {
                return new SoftReference<>(input[0]);
            }
        }

    }

    private static class Output {
        private static class Print implements JavaFunction {
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
                return "_print";
            }

            @Override
            public Object call(Object[] input) {
                System.out.print(input[0]);
                return null;
            }
        }

        private static class Println implements JavaFunction {
            @Override
            public boolean isVarArgs() {
                return true;
            }

            @Override
            public int getParameterNumber() {
                return 0;
            }

            @Override
            public String getFunctionName() {
                return "_println";
            }

            @Override
            public Object call(Object[] input) {
                if (input.length == 0) {
                    System.out.println();
                } else {
                    System.out.println(input[0]);
                }
                return null;
            }
        }

    }
    // TODO annotation
}