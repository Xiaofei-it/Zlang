package xiaofei.library.zlang;

/**
 * Created by Xiaofei on 2017/9/13.
 */

public class CompileException extends RuntimeException {

    public final CompileError error;

    public final String message;

    public final int lineNumber;

    public final int start;

    public final int end;

    CompileException(CompileError error, int lineNumber, int start, int end) {
        this(error, lineNumber, start, end, null);
    }

    CompileException(CompileError error, int lineNumber, int start, int end, String message) {
        this.error = error;
        this.message = message;
        this.lineNumber = lineNumber;
        this.start = start;
        this.end = end;
    }
}