package kz.esign;

/** Utility-дің барлық күтілетін қателері үшін ортақ exception. */
public class EsignException extends RuntimeException {

    public EsignException(String message) {
        super(message);
    }

    public EsignException(String message, Throwable cause) {
        super(message, cause);
    }
}
