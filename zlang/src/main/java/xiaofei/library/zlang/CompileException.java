package xiaofei.library.zlang;

/**
 * Created by Xiaofei on 2017/9/13.
 */

public class CompileException extends RuntimeException {

    CompileException(CompileError error, int lineNumber, int start, String info) {
        super("" + error + ": " + info + " At " + lineNumber + ":" + (start - 1));
    }
}