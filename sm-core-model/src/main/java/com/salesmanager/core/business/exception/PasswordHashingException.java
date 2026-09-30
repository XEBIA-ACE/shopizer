package com.salesmanager.core.business.exception;

public class PasswordHashingException extends ServiceException {

	private static final long serialVersionUID = 2915672457061634102L;

	public PasswordHashingException(String message) {
		super(EXCEPTION_ERROR, message);
	}

	public PasswordHashingException(String message, Throwable cause) {
		super(message, cause);
	}

}
