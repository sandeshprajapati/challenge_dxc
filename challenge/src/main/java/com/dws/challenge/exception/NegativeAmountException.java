package com.dws.challenge.exception;

public class NegativeAmountException extends RuntimeException {

	private static final long serialVersionUID = 7721041225019147591L;

	public NegativeAmountException(String message) {
		super(message);
	}
}
