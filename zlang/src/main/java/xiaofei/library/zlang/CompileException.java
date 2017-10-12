package xiaofei.library.zlang;

/**
 * Created by Xiaofei on 2017/9/13.
 */

public class CompileException extends RuntimeException {

    public final CompileError error;

    public final String info;

    CompileException(CompileError error, int lineNumber, int start, String message) {
        this.error = error;
        this.info = "Compiler error: " + error + "\nAt Line " + lineNumber + " Position " + (start - 1) + "\nInformation: " + message;
    }
}