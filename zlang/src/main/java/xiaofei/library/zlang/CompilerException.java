package xiaofei.library.zlang;

/**
 * Created by Xiaofei on 2017/9/13.
 */

public class CompilerException extends RuntimeException {

    private CompilerError error;

    private String message;

    public CompilerException(CompilerError error) {
        this(error, null);
    }

    public CompilerException(CompilerError error,String message) {
        this.error = error;
        this.message = message;
    }

    public CompilerError getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }
}