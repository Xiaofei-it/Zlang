package xiaofei.library.zlang;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Xiaofei on 2017/10/2.
 */

public final class Libraries {

    private Libraries() {}

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
                throw new IllegalArgumentException();
            }
            if (!foundConstructor.isAccessible()) {
                foundConstructor.setAccessible(true);
            }
            try {
                 return foundConstructor.newInstance(parameters);
            } catch (InstantiationException e) {
                throw new IllegalArgumentException(e);
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException(e);
            } catch (InvocationTargetException e) {
                throw new IllegalArgumentException(e);
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
                throw new IllegalArgumentException();
            }
            if (!foundConstructor.isAccessible()) {
                foundConstructor.setAccessible(true);
            }
            try {
                return foundConstructor.newInstance(parameters);
            } catch (InstantiationException e) {
                throw new IllegalArgumentException(e);
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException(e);
            } catch (InvocationTargetException e) {
                throw new IllegalArgumentException(e);
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
            int length = input.length - 2;
            Object[] parameters = new Object[length];
            if (length >= 1) {
                System.arraycopy(input, 1, parameters, 0, length);
            }
            Method foundMethod = null;
            do {
                Method[] methods = clazz.getDeclaredMethods();
                for (Method method : methods) {
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
                throw new IllegalArgumentException();
            }
            if (!foundMethod.isAccessible()) {
                foundMethod.setAccessible(true);
            }
            try {
                return foundMethod.invoke(input[0], parameters);
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException(e);
            } catch (InvocationTargetException e) {
                throw new IllegalArgumentException(e);
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
            int length = input.length - 2;
            Object[] parameters = new Object[length];
            if (length >= 1) {
                System.arraycopy(input, 1, parameters, 0, length);
            }
            Method foundMethod = null;
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
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
                throw new IllegalArgumentException();
            }
            if (!foundMethod.isAccessible()) {
                foundMethod.setAccessible(true);
            }
            try {
                return foundMethod.invoke(input[0], parameters);
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException(e);
            } catch (InvocationTargetException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }
}
