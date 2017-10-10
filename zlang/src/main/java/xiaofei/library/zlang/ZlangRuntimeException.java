package xiaofei.library.zlang;

/**
 * Created by Xiaofei on 2017/10/10.
 */

public class ZlangRuntimeException extends RuntimeException {

    public final ZlangRuntimeError error;

    public final String message;

    ZlangRuntimeException(ZlangRuntimeError error, String message) {
        this.error = error;
        this.message = message;
    }

    ZlangRuntimeException(ZlangRuntimeError error) {
        this(error, null);
    }
}
