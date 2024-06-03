package com.dws.challenge.repository;

import java.util.List;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.DuplicateAccountIdException;

public interface AccountsRepository {

	void createAccount(Account account) throws DuplicateAccountIdException;

	void updateAccount(Account account);
	
	Account getAccount(String accountId);

	void clearAccounts();

	List<Account> getAllAccounts();

}
