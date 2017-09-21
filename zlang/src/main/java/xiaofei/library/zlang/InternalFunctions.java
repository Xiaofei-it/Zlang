package xiaofei.library.zlang;

/**
 * Created by zhaolifei on 2017/9/21.
 */

class InternalFunctions {
    static Object call(String functionName, Object[] parameters) {
        switch (functionName) {
            case "_":
                return null;
            default:
                throw new IllegalArgumentException();
        }
    }
}
