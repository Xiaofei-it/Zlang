package xiaofei.library.zlang;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Xiaofei on 2017/9/18.
 *
 * This class is not thread safe.
 */

class FunctionStorage {

    private static FunctionStorage instance = null;

    private HashMap<String, HashMap<Integer, String>> functionMap;

    private FunctionStorage() {
        functionMap = new HashMap<>();
    }

    static FunctionStorage getInstance() {
        if (instance == null) {
            instance = new FunctionStorage();
        }
        return instance;
    }

    void put(String function) {

    }

    String get(String funName, int paraNumber) {
        HashMap<Integer, String> map = functionMap.get(funName);
        if (map == null) {
            return null;
        }
        return map.get(paraNumber);
    }
}
