package com.dws.challenge.exception;

public class DuplicateAccountIdException extends RuntimeException {

	private static final long serialVersionUID = 7721041225019147591L;

	public DuplicateAccountIdException(String message) {
		super(message);
	}
}
