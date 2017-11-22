package xiaofei.library.zlang;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Xiaofei on 2017/9/21.
 */

class InternalJavaFunctions extends JavaLibrary {

    private static final Storage STORAGE = Storage.getInstance();

    static final InternalJavaFunctions INSTANCE = new InternalJavaFunctions();

    private InternalJavaFunctions() {
    }

    @Override
    protected JavaFunction[] onProvideJavaFunctions() {
        return new JavaFunction[]{

                new Array.GetLength(),
                new Array.NewArray(),
                new Array.ArrayOf(),

                new ObjectMethods.HashCode(),
                new ObjectMethods.Compare(),
                new ObjectMethods.Equal(),
                new ObjectMethods.GetClassName(),

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

                new Reflection.NewInstance(),
                new Reflection.MethodInvocation(),
                new Reflection.PublicMethodInvocation(),
                new Reflection.FieldGetter(),
                new Reflection.PublicFieldGetter(),
                new Reflection.FieldSetter(),
                new Reflection.PublicFieldSetter(),
                new Reflection.StaticMethodInvocation(),
                new Reflection.StaticPublicMethodInvocation(),
                new Reflection.StaticFieldGetter(),
                new Reflection.StaticPublicFieldGetter(),
                new Reflection.StaticFieldSetter(),
                new Reflection.StaticPublicFieldSetter(),
        };
    }

    private static Class<?> obtainClass(Object input) {
        if (input instanceof Class<?>) {
            return  (Class<?>) input;
        } else if (input instanceof String) {
            try {
                return STORAGE.getClass((String) input);
            } catch (ClassNotFoundException e) {
                throw new ZlangRuntimeException(ZlangRuntimeError.CLASS_NOT_FOUND, "" + input);
            }
        } else {
            throw new ZlangRuntimeException(ZlangRuntimeError.ILLEGAL_ARGUMENT, "" + input);
        }
    }

    private static Class<?> obtainClass2(Object input) {
        if (input instanceof String) {
            try {
                return STORAGE.getClass((String) input);
            } catch (ClassNotFoundException e) {
                throw new ZlangRuntimeException(ZlangRuntimeError.CLASS_NOT_FOUND, "" + input);
            }
        } else {
            return input.getClass();
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
        private static class GetClassName implements JavaFunction {
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
                return "_get_class_name";
            }

            @Override
            public Object call(Object[] input) {
                return input[0].getClass().getName();
            }
            // TODO f(1)
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
                Class<?> clazz =  obtainClass(input[0]);
                // TODO
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
                return "_map_contains_key";
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
                return "_map_contains_value";
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
                return "_remove";
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

    private static class Reflection {

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
                return "_new_instance";
            }

            @Override
            public Object call(Object[] input) {
                int length = input.length - 1;
                Object[] parameters = new Object[length];
                if (length >= 1) {
                    System.arraycopy(input, 1, parameters, 0, length);
                }
                Class<?> clazz = obtainClass(input[0]);
                Constructor foundConstructor = STORAGE.getConstructor(clazz, parameters);
                if (foundConstructor == null) {
                    throw new ZlangRuntimeException(ZlangRuntimeError.NO_SUCH_CONSTRUCTOR, "Class: " + input[0] + " Parameter number: " + length);
                }
                if (!foundConstructor.isAccessible()) {
                    foundConstructor.setAccessible(true);
                }
                try {
                    return foundConstructor.newInstance(parameters);
                } catch (InstantiationException e) {
                    throw new ZlangRuntimeException(ZlangRuntimeError.NEW_INSTANCE_ERROR, foundConstructor.toString());
                } catch (IllegalAccessException e) {
                    throw new ZlangRuntimeException(ZlangRuntimeError.NEW_INSTANCE_ERROR, foundConstructor.toString());
                } catch (InvocationTargetException e) {
                    throw new ZlangRuntimeException(ZlangRuntimeError.NEW_INSTANCE_ERROR, foundConstructor.toString());
                }
            }
        }

        private static class MethodInvocation implements JavaFunction {
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
                return "_invoke_method";
            }

            @Override
            public Object call(Object[] input) {
                Class<?> clazz = input[0].getClass();
                String methodName = (String) input[1];
                int length = input.length - 2;
                Object[] parameters = new Object[length];
                if (length >= 1) {
                    System.arraycopy(input, 2, parameters, 0, length);
                }
                Method foundMethod = STORAGE.getMethod(clazz, methodName, parameters);
                if (foundMethod == null) {
                    throw new ZlangRuntimeException(ZlangRuntimeError.NO_SUCH_METHOD,
                            "Class: " + input[0] + " Method name: "  + methodName + " Parameter number: " + length);
                }
                if (!foundMethod.isAccessible()) {
                    foundMethod.setAccessible(true);
                }
                try {
                    return foundMethod.invoke(input[0], parameters);
                } catch (IllegalAccessException e) {
                    throw new ZlangRuntimeException(ZlangRuntimeError.METHOD_INVOCATION_ERROR, foundMethod.toString());
                } catch (InvocationTargetException e) {
                    throw new ZlangRuntimeException(ZlangRuntimeError.METHOD_INVOCATION_ERROR, foundMethod.toString());
                }
            }
        }

        private static class PublicMethodInvocation implements JavaFunction {
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
                return "_invoke_public_method";
            }

            @Override
            public Object call(Object[] input) {
                Class<?> clazz = input[0].getClass();
                String methodName = (String) input[1];
                int length = input.length - 2;
                Object[] parameters = new Object[length];
                if (length >= 1) {
                    System.arraycopy(input, 2, parameters, 0, length);
                }
                Method foundMethod = STORAGE.getPublicMethod(clazz, methodName, parameters);
                if (foundMethod == null) {
                    throw new ZlangRuntimeException(ZlangRuntimeError.NO_SUCH_METHOD,
                            "Class: " + input[0] + " Method name: "  + methodName + " Parameter number: " + length);
                }
                if (!foundMethod.isAccessible()) {
                    foundMethod.setAccessible(true);
                }
                try {
                    return foundMethod.invoke(input[0], parameters);
                } catch (IllegalAccessException e) {
                    throw new ZlangRuntimeException(ZlangRuntimeError.METHOD_INVOCATION_ERROR, foundMethod.toString());
                } catch (InvocationTargetException e) {
                    throw new ZlangRuntimeException(ZlangRuntimeError.METHOD_INVOCATION_ERROR, foundMethod.toString());
                }
            }
        }

        private static class FieldGetter implements JavaFunction {
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
                return "_get_field";
            }

            @Override
            public Object call(Object[] input) {
                Class<?> clazz = input[0].getClass();
                String name = (String) input[1];
                Field field = STORAGE.getField(clazz, name);
                if (field == null) {
                    throw new ZlangRuntimeException(ZlangRuntimeError.NO_SUCH_FIELD,
                            "Class: " + input[0] + " Field name : " + name);
                }
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                try {
                    return field.get(input[0]);
                } catch (IllegalAccessException e) {
                    throw new ZlangRuntimeException(ZlangRuntimeError.FIELD_GET_ERROR, field.toString());
                }
            }
        }

        private static class PublicFieldGetter implements JavaFunction {
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
                return "_get_public_field";
            }

            @Override
            public Object call(Object[] input) {
                Class<?> clazz = input[0].getClass();
                String name = (String) input[1];
                Field field = STORAGE.getPublicField(clazz, name);
                if (field == null) {
                    throw new ZlangRuntimeException(ZlangRuntimeError.NO_SUCH_FIELD,
                            "Class: " + input[0] + " Field name : " + name);
                }
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                try {
                    return field.get(input[0]);
                } catch (IllegalAccessException e) {
                    throw new ZlangRuntimeException(ZlangRuntimeError.FIELD_GET_ERROR, field.toString());
                }
            }
        }

        private static class FieldSetter implements JavaFunction {
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
                return "_set_field";
            }

            @Override
            public Object call(Object[] input) {
                Class<?> clazz = input[0].getClass();
                String name = (String) input[1];
                Field field = STORAGE.getField(clazz, name);
                if (field == null) {
                    throw new ZlangRuntimeException(ZlangRuntimeError.NO_SUCH_FIELD,
                            "Class: " + input[0] + " Field name : " + name);
                }
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                try {
                    field.set(input[0], input[2]);
                    return null;
                } catch (IllegalAccessException e) {
                    throw new ZlangRuntimeException(ZlangRuntimeError.FIELD_SET_ERROR, field.toString());
                }
            }
        }

        private static class PublicFieldSetter implements JavaFunction {
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
                return "_set_public_field";
            }

            @Override
            public Object call(Object[] input) {
                Class<?> clazz = input[0].getClass();
                String name = (String) input[1];
                Field field = STORAGE.getPublicField(clazz, name);
                if (field == null) {
                    throw new ZlangRuntimeException(ZlangRuntimeError.NO_SUCH_FIELD,
                            "Class: " + input[0] + " Field name : " + name);
                }
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                try {
                    field.set(input[0], input[2]);
                    return null;
                } catch (IllegalAccessException e) {
                    throw new ZlangRuntimeException(ZlangRuntimeError.FIELD_SET_ERROR, field.toString());
                }

            }
        }

        //TODO
        private static class StaticMethodInvocation implements JavaFunction {
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
                return "_invoke_static_method";
            }

            @Override
            public Object call(Object[] input) {
                Class<?> clazz = obtainClass2(input[0]);
                String methodName = (String) input[1];
                int length = input.length - 2;
                Object[] parameters = new Object[length];
                if (length >= 1) {
                    System.arraycopy(input, 2, parameters, 0, length);
                }
                Method foundMethod = STORAGE.getMethod(clazz, methodName, parameters);
                if (foundMethod == null) {
                    throw new ZlangRuntimeException(ZlangRuntimeError.NO_SUCH_METHOD,
                            "Class: " + input[0] + " Method name: "  + methodName + " Parameter number: " + length);
                }
                if (!foundMethod.isAccessible()) {
                    foundMethod.setAccessible(true);
                }
                try {
                    return foundMethod.invoke(null, parameters);
                } catch (IllegalAccessException e) {
                    throw new ZlangRuntimeException(ZlangRuntimeError.METHOD_INVOCATION_ERROR, foundMethod.toString());
                } catch (InvocationTargetException e) {
                    throw new ZlangRuntimeException(ZlangRuntimeError.METHOD_INVOCATION_ERROR, foundMethod.toString());
                }
            }
        }

        private static class StaticPublicMethodInvocation implements JavaFunction {
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
                return "_invoke_static_public_method";
            }

            @Override
            public Object call(Object[] input) {
                Class<?> clazz = obtainClass2(input[0]);
                String methodName = (String) input[1];
                int length = input.length - 2;
                Object[] parameters = new Object[length];
                if (length >= 1) {
                    System.arraycopy(input, 2, parameters, 0, length);
                }
                Method foundMethod = STORAGE.getPublicMethod(clazz, methodName, parameters);
                if (foundMethod == null) {
                    throw new ZlangRuntimeException(ZlangRuntimeError.NO_SUCH_METHOD,
                            "Class: " + input[0] + " Method name: "  + methodName + " Parameter number: " + length);
                }
                if (!foundMethod.isAccessible()) {
                    foundMethod.setAccessible(true);
                }
                try {
                    return foundMethod.invoke(null, parameters);
                } catch (IllegalAccessException e) {
                    throw new ZlangRuntimeException(ZlangRuntimeError.METHOD_INVOCATION_ERROR, foundMethod.toString());
                } catch (InvocationTargetException e) {
                    throw new ZlangRuntimeException(ZlangRuntimeError.METHOD_INVOCATION_ERROR, foundMethod.toString());
                }
            }
        }

        private static class StaticFieldGetter implements JavaFunction {
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
                return "_get_static_field";
            }

            @Override
            public Object call(Object[] input) {
                Class<?> clazz = obtainClass2(input[0]);
                String name = (String) input[1];
                Field field = STORAGE.getField(clazz, name);
                if (field == null) {
                    throw new ZlangRuntimeException(ZlangRuntimeError.NO_SUCH_FIELD,
                            "Class: " + input[0] + " Field name : " + name);
                }
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                try {
                    return field.get(null);
                } catch (IllegalAccessException e) {
                    throw new ZlangRuntimeException(ZlangRuntimeError.FIELD_GET_ERROR, field.toString());
                }
            }
        }

        private static class StaticPublicFieldGetter implements JavaFunction {
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
                return "_get_static_public_field";
            }

            @Override
            public Object call(Object[] input) {
                Class<?> clazz = obtainClass2(input[0]);
                String name = (String) input[1];
                Field field = STORAGE.getPublicField(clazz, name);
                if (field == null) {
                    throw new ZlangRuntimeException(ZlangRuntimeError.NO_SUCH_FIELD,
                            "Class: " + input[0] + " Field name : " + name);
                }
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                try {
                    return field.get(null);
                } catch (IllegalAccessException e) {
                    throw new ZlangRuntimeException(ZlangRuntimeError.FIELD_GET_ERROR, field.toString());
                }
            }
        }

        private static class StaticFieldSetter implements JavaFunction {
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
                return "_set_static_field";
            }

            @Override
            public Object call(Object[] input) {
                Class<?> clazz = obtainClass2(input[0]);
                String name = (String) input[1];
                Field field = STORAGE.getField(clazz, name);
                if (field == null) {
                    throw new ZlangRuntimeException(ZlangRuntimeError.NO_SUCH_FIELD,
                            "Class: " + input[0] + " Field name : " + name);
                }
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                try {
                    field.set(null, input[2]);
                    return null;
                } catch (IllegalAccessException e) {
                    throw new ZlangRuntimeException(ZlangRuntimeError.FIELD_SET_ERROR, field.toString());
                }
            }
        }

        private static class StaticPublicFieldSetter implements JavaFunction {
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
                return "_set_static_public_field";
            }

            @Override
            public Object call(Object[] input) {
                Class<?> clazz = obtainClass2(input[0]);
                String name = (String) input[1];
                Field field = STORAGE.getPublicField(clazz, name);
                if (field == null) {
                    throw new ZlangRuntimeException(ZlangRuntimeError.NO_SUCH_FIELD,
                            "Class: " + input[0] + " Field name : " + name);
                }
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                try {
                    field.set(null, input[2]);
                    return null;
                } catch (IllegalAccessException e) {
                    throw new ZlangRuntimeException(ZlangRuntimeError.FIELD_SET_ERROR, field.toString());
                }

            }
        }
    }
    // TODO annotation
}