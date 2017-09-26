package xiaofei.library.zlang;

/**
 * Created by Xiaofei on 2017/9/26.
 */

public class ExecutorException extends RuntimeException {

    private ExecutorError error;

    private String message;

    public ExecutorException(ExecutorError error) {
        this(error, null);
    }

    public ExecutorException(ExecutorError error, String message) {
        this.error = error;
        this.message = message;
    }

    public ExecutorError getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }
}