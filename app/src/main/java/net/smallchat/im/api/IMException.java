
package net.smallchat.im.api;

public class IMException extends Exception {

	private static final long serialVersionUID = 475022994858770424L;
	private int statusCode = -1;
	private int msgId = -1;
	
	
    public IMException(String msg) {
        super(msg);
    }

    public IMException(Exception cause) {
        super(cause);
    }

    public IMException(String msg, int statusCode) {
        super(msg);
        this.statusCode = statusCode;
    }

    public IMException(String msg, Exception cause) {
        super(msg, cause);
    }

    public IMException(String msg, Exception cause, int statusCode) {
        super(msg, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return this.statusCode;
    }
    
    
	public IMException() {
		super(); 
	}

	public IMException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public IMException(Throwable throwable) {
		super(throwable);
	}

	public IMException(int statusCode) {
		super();
		this.statusCode = statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}
	
}
