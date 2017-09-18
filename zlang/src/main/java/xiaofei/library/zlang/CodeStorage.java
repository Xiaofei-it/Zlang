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

    private HashMap<String, ArrayList<Code>> codeMap;

    private CodeStorage() {
        codeMap = new HashMap<>();
    }

    static CodeStorage getInstance() {
        if (instance == null) {
            instance = new CodeStorage();
        }
        return instance;
    }

    void put(String funName, ArrayList<Code> codes) {
        codeMap.put(funName, codes);
    }

    ArrayList<Code> get(String funName) {
        return codeMap.get(funName);
    }
}
