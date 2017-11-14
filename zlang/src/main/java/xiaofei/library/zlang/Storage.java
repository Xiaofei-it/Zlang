package xiaofei.library.zlang;

import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Eric on 2017/11/14.
 */

class Storage {
    private static volatile Storage instance = null;

    private ConcurrentHashMap<String, Class<?>> classMap;

    private ConcurrentHashMap<String, CopyOnWriteArrayList<Constructor<?>>> constructorListMap;

    private ConcurrentHashMap<String, CopyOnWriteArrayList<Constructor<?>>> publicConstructorListMap;

    private Storage() {
        classMap = new ConcurrentHashMap<>();
        constructorListMap = new ConcurrentHashMap<>();
        publicConstructorListMap = new ConcurrentHashMap<>();
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
        Class<?> result = classMap.get(className);
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

    private static boolean match(Object[] parameters, Class<?>[] parameterTypes) {
        int length = parameters.length;
        if (length != parameterTypes.length) {
            return false;
        }
        for (int i = 0; i < length; ++i) {
            if (parameters[i] != null && !parameterTypes[i].isInstance(parameters[i])) {
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
            if (match(parameters, constructor.getParameterTypes())) {
                return constructor;
            }
        }
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            if (match(parameters, constructor.getParameterTypes())) {
                constructorList.add(constructor); // Maybe add twice.
                return constructor;
            }
        }
        return null;
    }

    Constructor<?> getPublicConstructor(Class<?> clazz, Object[] parameters) {
        String className = clazz.getName();
        CopyOnWriteArrayList<Constructor<?>> constructorList = publicConstructorListMap.get(className);
        if (constructorList == null) {
            CopyOnWriteArrayList<Constructor<?>> tmp = new CopyOnWriteArrayList<>();
            constructorList = publicConstructorListMap.putIfAbsent(className, tmp);
            if (constructorList == null) {
                constructorList = tmp;
            }
        }
        Iterator<Constructor<?>> iterator = constructorList.iterator();
        while (iterator.hasNext()) {
            Constructor<?> constructor = iterator.next();
            if (match(parameters, constructor.getParameterTypes())) {
                return constructor;
            }
        }
        for (Constructor<?> constructor : clazz.getConstructors()) {
            if (match(parameters, constructor.getParameterTypes())) {
                constructorList.add(constructor); // Maybe add twice.
                return constructor;
            }
        }
        return null;
    }
    // TODO call super.??? call super,item  call super()
}
