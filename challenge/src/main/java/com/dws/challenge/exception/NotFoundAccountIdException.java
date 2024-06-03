package com.dws.challenge.exception;

public class NotFoundAccountIdException extends RuntimeException {

	private static final long serialVersionUID = 7721041225019147591L;

	public NotFoundAccountIdException(String message) {
		super(message);
	}
}
