package com.dws.challenge.domain;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RequestInput {

	@NotNull
	@NotEmpty
	private final String fromAccountId;

	@NotNull
	@NotEmpty
	private final String toAccountId;

	@NotNull
	@Min(value = 1, message = "Initial amount must be positive.")
	private BigDecimal amount;

	@JsonCreator
	public RequestInput(@JsonProperty("fromAccountId") String fromAccountId,
			@JsonProperty("toAccountId") String toAccountId, @JsonProperty("amount") BigDecimal amount) {
		super();
		this.fromAccountId = fromAccountId;
		this.toAccountId = toAccountId;
		this.amount = amount;
	}

}
