package xiaofei.library.zlang;

/**
 * Created by Xiaofei on 2017/10/10.
 */

public class ZlangRuntimeException extends RuntimeException {

    ZlangRuntimeException(ZlangRuntimeError error, String info) {
        super("Runtime error: " + error + " Info: " + info);
    }

    ZlangRuntimeException(ZlangRuntimeError error) {
        this(error, null);
    }
}
