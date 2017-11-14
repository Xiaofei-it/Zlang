package xiaofei.library.zlang;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Eric on 2017/11/14.
 */

class Storage {
    private static volatile Storage instance = null;

    private ConcurrentHashMap<String, Class<?>> classes;
    private Storage() {
        classes = new ConcurrentHashMap<>();
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
        Class<?> result = classes.get(className);
        if (result != null) {
            return result;
        }
        Class<?> tmp = Class.forName(className);
        result = classes.putIfAbsent(className, tmp);
        if (result == null) {
            return tmp;
        } else {
            return result;
        }
    }

}
