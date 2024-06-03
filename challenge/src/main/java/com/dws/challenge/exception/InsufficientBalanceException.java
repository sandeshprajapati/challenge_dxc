package com.dws.challenge.exception;

public class InsufficientBalanceException extends RuntimeException {

	private static final long serialVersionUID = 7721041225019147591L;

	public InsufficientBalanceException(String message) {
		super(message);
	}
}
