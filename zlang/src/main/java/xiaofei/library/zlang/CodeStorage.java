package xiaofei.library.zlang;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Xiaofei on 2017/9/18.
 *
 * This class is not thread safe.
 */

class CodeStorage {

    private static CodeStorage instance = null;

    private HashMap<String, HashMap<Integer, ArrayList<Code>>> codeMap;

    private CodeStorage() {
        codeMap = new HashMap<>();
    }

    static CodeStorage getInstance() {
        if (instance == null) {
            instance = new CodeStorage();
        }
        return instance;
    }

    void put(String funName, int paraNumber, ArrayList<Code> codes) {
        HashMap<Integer, ArrayList<Code>> map = codeMap.get(funName);
        if (map == null) {
            map = new HashMap<Integer, ArrayList<Code>>();
            codeMap.put(funName, map);
        }
        map.put(paraNumber, codes);
    }

    ArrayList<Code> get(String funName, int paraNumber) {
        HashMap<Integer, ArrayList<Code>> map = codeMap.get(funName);
        if (map == null) {
            return null;
        }
        return map.get(paraNumber);
    }
}
