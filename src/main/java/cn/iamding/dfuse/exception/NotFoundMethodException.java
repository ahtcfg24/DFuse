package cn.iamding.dfuse.exception;

/**
 * Exception while not find method by annotation
 */
public class NotFoundMethodException extends Exception {
    public NotFoundMethodException(String message) {
        super(message);
    }
}
