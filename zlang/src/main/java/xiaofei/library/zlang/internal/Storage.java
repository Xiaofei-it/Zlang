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

package xiaofei.library.zlang.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Xiaofei on 2017/11/14.
 */

class Storage {

    private static final ConcurrentHashMap<Class<?>, Class<?>> PRIMITIVE_CLASSES = new ConcurrentHashMap<Class<?>, Class<?>>() {
        {
            put(Byte.class, byte.class);
            put(Character.class, char.class);
            put(Short.class, short.class);
            put(Integer.class, int.class);
            put(Long.class, long.class);
            put(Float.class, float.class);
            put(Double.class, double.class);
        }
    };

    private static final ConcurrentHashMap<String, Class<?>> PRIMITIVE_CLASS_MAP = new ConcurrentHashMap<String, Class<?>>() {
        {
            put("byte", byte.class);
            put("char", char.class);
            put("short", short.class);
            put("int", int.class);
            put("long", long.class);
            put("float", float.class);
            put("double", double.class);
        }
    };

    private static volatile Storage instance = null;

    private ConcurrentHashMap<String, Class<?>> classMap;

    private ConcurrentHashMap<String, CopyOnWriteArrayList<Constructor<?>>> constructorListMap;

    private ConcurrentHashMap<String, ConcurrentHashMap<String, CopyOnWriteArrayList<Method>>> methodListMapMap;

    private ConcurrentHashMap<String, ConcurrentHashMap<String, CopyOnWriteArrayList<Method>>> publicMethodListMapMap;

    private ConcurrentHashMap<String, ConcurrentHashMap<String, Field>> fieldMapMap;

    private ConcurrentHashMap<String, ConcurrentHashMap<String, Field>> publicFieldMapMap;

    private Storage() {
        classMap = new ConcurrentHashMap<>();
        constructorListMap = new ConcurrentHashMap<>();
        methodListMapMap = new ConcurrentHashMap<>();
        publicMethodListMapMap = new ConcurrentHashMap<>();
        fieldMapMap = new ConcurrentHashMap<>();
        publicFieldMapMap = new ConcurrentHashMap<>();
    }

    static Storage getInstance() {
        if (instance == null) {
            synchronized (Storage.class) {
                if (instance == null) {
                    instance = new Storage();
                }
            }
        }
        return instance;
    }

    Class<?> getClass(String className) throws ClassNotFoundException {
        Class<?> result = PRIMITIVE_CLASS_MAP.get(className);
        if (result != null) {
            return result;
        }
        result = classMap.get(className);
        if (result != null) {
            return result;
        }
        Class<?> tmp = Class.forName(className);
        result = classMap.putIfAbsent(className, tmp);
        if (result == null) {
            return tmp;
        } else {
            return result;
        }
    }

    private static boolean matchParameter(Object parameter, Class<?> parameterType) {
        if (!parameterType.isPrimitive()) {
            return parameterType.isInstance(parameter);
        }
        return PRIMITIVE_CLASSES.get(parameter.getClass()) == parameterType;
    }

    private static boolean matchParameters(Object[] parameters, Class<?>[] parameterTypes) {
        int length = parameters.length;
        if (length != parameterTypes.length) {
            return false;
        }
        for (int i = 0; i < length; ++i) {
            if (parameters[i] != null && !matchParameter(parameters[i], parameterTypes[i])) {
                return false;
            }
        }
        return true;
    }

    Constructor<?> getConstructor(Class<?> clazz, Object[] parameters) {
        String className = clazz.getName();
        CopyOnWriteArrayList<Constructor<?>> constructorList = constructorListMap.get(className);
        if (constructorList == null) {
            CopyOnWriteArrayList<Constructor<?>> tmp = new CopyOnWriteArrayList<>();
            constructorList = constructorListMap.putIfAbsent(className, tmp);
            if (constructorList == null) {
                constructorList = tmp;
            }
        }
        Iterator<Constructor<?>> iterator = constructorList.iterator();
        while (iterator.hasNext()) {
            Constructor<?> constructor = iterator.next();
            if (matchParameters(parameters, constructor.getParameterTypes())) {
                return constructor;
            }
        }
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            if (matchParameters(parameters, constructor.getParameterTypes())) {
                constructorList.add(constructor); // Maybe add twice.
                return constructor;
            }
        }
        return null;
    }

    Method getMethod(Class<?> clazz, String methodName, Object[] parameters) {
        String className = clazz.getName();
        ConcurrentHashMap<String, CopyOnWriteArrayList<Method>> methodListMap = methodListMapMap.get(className);
        if (methodListMap == null) {
            ConcurrentHashMap<String, CopyOnWriteArrayList<Method>> tmp = new ConcurrentHashMap<>();
            methodListMap = methodListMapMap.putIfAbsent(className, tmp);
            if (methodListMap == null) {
                methodListMap = tmp;
            }
        }
        CopyOnWriteArrayList<Method> methodList = methodListMap.get(methodName);
        if (methodList == null) {
            CopyOnWriteArrayList<Method> tmp = new CopyOnWriteArrayList<>();
            methodList = methodListMap.putIfAbsent(className, tmp);
            if (methodList == null) {
                methodList = tmp;
            }
        }
        Iterator<Method> iterator = methodList.iterator();
        while (iterator.hasNext()) {
            Method method = iterator.next();
            if (!method.getName().equals(methodName)) {
                continue;
            }
            if (matchParameters(parameters, method.getParameterTypes())) {
                return method;
            }
        }
        while (clazz != Object.class) {
            for (Method method : clazz.getDeclaredMethods()) {
                if (!method.getName().equals(methodName)) {
                    continue;
                }
                if (matchParameters(parameters, method.getParameterTypes())) {
                    methodList.add(method); // Maybe add twice.
                    return method;
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    Method getPublicMethod(Class<?> clazz, String methodName, Object[] parameters) {
        String className = clazz.getName();
        ConcurrentHashMap<String, CopyOnWriteArrayList<Method>> methodListMap = publicMethodListMapMap.get(className);
        if (methodListMap == null) {
            ConcurrentHashMap<String, CopyOnWriteArrayList<Method>> tmp = new ConcurrentHashMap<>();
            methodListMap = publicMethodListMapMap.putIfAbsent(className, tmp);
            if (methodListMap == null) {
                methodListMap = tmp;
            }
        }
        CopyOnWriteArrayList<Method> methodList = methodListMap.get(methodName);
        if (methodList == null) {
            CopyOnWriteArrayList<Method> tmp = new CopyOnWriteArrayList<>();
            methodList = methodListMap.putIfAbsent(className, tmp);
            if (methodList == null) {
                methodList = tmp;
            }
        }
        Iterator<Method> iterator = methodList.iterator();
        while (iterator.hasNext()) {
            Method method = iterator.next();
            if (!method.getName().equals(methodName)) {
                continue;
            }
            if (matchParameters(parameters, method.getParameterTypes())) {
                return method;
            }
        }
        for (Method method : clazz.getMethods()) {
            if (!method.getName().equals(methodName)) {
                continue;
            }
            if (matchParameters(parameters, method.getParameterTypes())) {
                methodList.add(method); // Maybe add twice.
                return method;
            }
        }
        return null;
    }

    Field getField(Class<?> clazz, String fieldName) {
        String className = clazz.getName();
        ConcurrentHashMap<String, Field> fieldMap = fieldMapMap.get(className);
        if (fieldMap == null) {
            ConcurrentHashMap<String, Field> tmp = new ConcurrentHashMap<>();
            fieldMap = fieldMapMap.putIfAbsent(className, tmp);
            if (fieldMap == null) {
                fieldMap = tmp;
            }
        }
        Field result = fieldMap.get(fieldName);
        if (result != null) {
            return result;
        }
        while (clazz != Object.class) {
            try {
                result = clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {

            }
            if (result != null) {
                fieldMap.putIfAbsent(fieldName, result);
                return result;
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    Field getPublicField(Class<?> clazz, String fieldName) {
        String className = clazz.getName();
        ConcurrentHashMap<String, Field> fieldMap = publicFieldMapMap.get(className);
        if (fieldMap == null) {
            ConcurrentHashMap<String, Field> tmp = new ConcurrentHashMap<>();
            fieldMap = publicFieldMapMap.putIfAbsent(className, tmp);
            if (fieldMap == null) {
                fieldMap = tmp;
            }
        }
        Field result = fieldMap.get(fieldName);
        if (result != null) {
            return result;
        }
        try {
            result = clazz.getField(fieldName);
            if (result != null) {
                fieldMap.putIfAbsent(fieldName, result);
                return result;
            }
        } catch (NoSuchFieldException e) {
            return null;
        }
        return null;
    }
    // TODO call super.??? call super,item  call super()
}
