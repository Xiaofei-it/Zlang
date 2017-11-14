package xiaofei.library.zlang;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Xiaofei on 2017/10/2.
 */

public final class Libraries {

    public static final JavaLibrary REFLECTION_UTILS = new ReflectionUtils();

    private Libraries() {}

    private static class ReflectionUtils extends JavaLibrary {

        private ReflectionUtils() {
        }

        @Override
        protected JavaFunction[] onProvideJavaFunctions() {
            return new JavaFunction[]{
                    new NewInstance(),
                    new NewInstancePublic(),
                    new MethodInvocation(),
                    new PublicMethodInvocation(),
                    new FieldGetter(),
                    new PublicFieldGetter(),
                    new FieldSetter(),
                    new PublicFieldSetter(),
            };
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
                return "new_instance";
            }

            @Override
            public Object call(Object[] input) {
                int length = input.length - 1;
                Object[] parameters = new Object[length];
                if (length >= 1) {
                    System.arraycopy(input, 1, parameters, 0, length);
                }
                Constructor[] constructors = ((Class<?>) input[0]).getDeclaredConstructors();
                Constructor foundConstructor = null;
                for (Constructor constructor : constructors) {
                    Class<?>[] parameterTypes = constructor.getParameterTypes();
                    if (parameterTypes.length != length) {
                        continue;
                    }
                    boolean found = true;
                    for (int i = 0; i < length; ++i) {
                        if (parameters[i] != null && !parameterTypes[i].isInstance(parameters[i])) {
                            found = false;
                            break;
                        }
                    }
                    if (found) {
                        foundConstructor = constructor;
                        break;
                    }
                }
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

        private static class NewInstancePublic implements JavaFunction {
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
                return "new_instance_public";
            }

            @Override
            public Object call(Object[] input) {
                int length = input.length - 1;
                Object[] parameters = new Object[length];
                if (length >= 1) {
                    System.arraycopy(input, 1, parameters, 0, length);
                }
                Constructor[] constructors = ((Class<?>) input[0]).getConstructors();
                Constructor foundConstructor = null;
                for (Constructor constructor : constructors) {
                    Class<?>[] parameterTypes = constructor.getParameterTypes();
                    if (parameterTypes.length != length) {
                        continue;
                    }
                    boolean found = true;
                    for (int i = 0; i < length; ++i) {
                        if (parameters[i] != null && !parameterTypes[i].isInstance(parameters[i])) {
                            found = false;
                            break;
                        }
                    }
                    if (found) {
                        foundConstructor = constructor;
                        break;
                    }
                }
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
                return 1;
            }

            @Override
            public String getFunctionName() {
                return "invoke_method";
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
                Method foundMethod = null;
                do {
                    Method[] methods = clazz.getDeclaredMethods();
                    for (Method method : methods) {
                        if (!method.getName().equals(methodName)) {
                            continue;
                        }
                        Class<?>[] parameterTypes = method.getParameterTypes();
                        if (parameterTypes.length != length) {
                            continue;
                        }
                        boolean found = true;
                        for (int i = 0; i < length; ++i) {
                            if (parameters[i] != null && !parameterTypes[i].isInstance(parameters[i])) {
                                found = false;
                                break;
                            }
                        }
                        if (found) {
                            foundMethod = method;
                            break;
                        }
                    }
                    if (clazz != Object.class) {
                        clazz = clazz.getSuperclass();
                    }
                } while (foundMethod != null && clazz != Object.class);
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
                return 1;
            }

            @Override
            public String getFunctionName() {
                return "invoke_public_method";
            }

            @Override
            public Object call(Object[] input) {
                Class<?> clazz = input[0].getClass();
                String methodName = (String) input[1];
                int length = input.length - 2;
                Object[] parameters = new Object[length];
                if (length >= 1) {
                    System.arraycopy(input, 1, parameters, 0, length);
                }
                Method foundMethod = null;
                Method[] methods = clazz.getMethods();
                for (Method method : methods) {
                    if (!method.getName().equals(methodName)) {
                        continue;
                    }
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    if (parameterTypes.length != length) {
                        continue;
                    }
                    boolean found = true;
                    for (int i = 0; i < length; ++i) {
                        if (parameters[i] != null && !parameterTypes[i].isInstance(parameters[i])) {
                            found = false;
                            break;
                        }
                    }
                    if (found) {
                        foundMethod = method;
                        break;
                    }
                }
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
                return "get_field";
            }

            @Override
            public Object call(Object[] input) {
                Class<?> clazz = input[0].getClass();
                String name = (String) input[1];
                Field field = null;
                do {
                    try {
                        field = clazz.getDeclaredField(name);
                    } catch (NoSuchFieldException e) {

                    }
                    if (clazz != Object.class) {
                        clazz = clazz.getSuperclass();
                    }
                } while (field == null && clazz != Object.class);
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
                return "get_field";
            }

            @Override
            public Object call(Object[] input) {
                Class<?> clazz = input[0].getClass();
                String name = (String) input[1];
                Field field = null;
                try {
                    field = clazz.getField(name);
                    if (!field.isAccessible()) {
                        field.setAccessible(true);
                    }
                    return field.get(input[0]);
                } catch (NoSuchFieldException e) {
                    throw new ZlangRuntimeException(ZlangRuntimeError.NO_SUCH_FIELD,
                            "Class: " + input[0] + " Field name : " + name);
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
                return "set_field";
            }

            @Override
            public Object call(Object[] input) {
                Class<?> clazz = input[0].getClass();
                String name = (String) input[1];
                Field field = null;
                do {
                    try {
                        field = clazz.getDeclaredField(name);
                    } catch (NoSuchFieldException e) {

                    }
                    if (clazz != Object.class) {
                        clazz = clazz.getSuperclass();
                    }
                } while (field == null && clazz != Object.class);
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
                return "set_field";
            }

            @Override
            public Object call(Object[] input) {
                Class<?> clazz = input[0].getClass();
                String name = (String) input[1];
                Field field = null;
                try {
                    field = clazz.getField(name);
                    if (!field.isAccessible()) {
                        field.setAccessible(true);
                    }
                    field.set(input[0], input[2]);
                    return null;
                } catch (NoSuchFieldException e) {
                    throw new ZlangRuntimeException(ZlangRuntimeError.NO_SUCH_FIELD,
                            "Class: " + input[0] + " Field name : " + name);
                } catch (IllegalAccessException e) {
                    throw new ZlangRuntimeException(ZlangRuntimeError.FIELD_SET_ERROR, field.toString());
                }
            }
        }
    }
}
