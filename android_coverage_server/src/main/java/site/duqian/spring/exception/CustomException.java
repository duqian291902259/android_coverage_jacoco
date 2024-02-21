package site.duqian.spring.exception;

public class CustomException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private int code;
    private String msg;

    public CustomException(int code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }

    public CustomException(int code, String msg, Throwable e) {
        super(msg, e);
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return this.code;
    }

    public String getMsg() {
        return this.msg;
    }

    public CustomException() {
        super();
    }
}
