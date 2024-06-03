package com.dws.challenge.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.stereotype.Service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.InsufficientBalanceException;
import com.dws.challenge.exception.NegativeAmountException;
import com.dws.challenge.exception.NotFoundAccountIdException;
import com.dws.challenge.repository.AccountsRepository;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AccountsService {

	private final ConcurrentHashMap<String, Lock> accountLocks = new ConcurrentHashMap<>();

	@Getter
	private final AccountsRepository accountsRepository;

	@Getter
	private final NotificationService notificationService;

	public AccountsService(AccountsRepository accountsRepository, NotificationService notificationService) {
		this.accountsRepository = accountsRepository;
		this.notificationService = notificationService;
	}

	public void createAccount(Account account) {
		this.accountsRepository.createAccount(account);
	}

	public Account getAccount(String accountId) {
		return this.accountsRepository.getAccount(accountId);
	}
	
	public void clearAccounts() {
		this.accountsRepository.clearAccounts();
	}

	public void transferAmount(String fromAccountId, String toAccountId, BigDecimal amount) {

		log.info("Print the balance before Transfer");
		printCurrentBalancet(fromAccountId, toAccountId);

		if (amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new NegativeAmountException("Transfer amount must be positive");
		}

		Lock lock1 = getLock(fromAccountId);
		Lock lock2 = getLock(toAccountId);

		Lock firstLock = fromAccountId.compareTo(toAccountId) < 0 ? lock1 : lock2;
		Lock secondLock = fromAccountId.compareTo(toAccountId) < 0 ? lock2 : lock1;

		firstLock.lock();
		try {
			secondLock.lock();
			try {
				Account accountFrom = accountsRepository.getAccount(fromAccountId);
				Account accountTo = accountsRepository.getAccount(toAccountId);

				if (accountFrom == null || accountTo == null) {
					log.info("Invalid account ID");
					throw new NotFoundAccountIdException("Invalid account ID");
				}

				if (accountFrom.getBalance().compareTo(amount) < 0) {
					log.info("Insufficient balance " + amount + " to trasfer from account " + fromAccountId);
					throw new InsufficientBalanceException(
							"Insufficient balance " + amount + " to trasfer from account " + fromAccountId);
				}

				accountFromWithdraw(accountFrom, amount);
				accountToDeposit(accountTo, amount);

				notificationService.notifyAboutTransfer(accountFrom, "Transferred amount :" + amount);
				notificationService.notifyAboutTransfer(accountTo, "Received amount :" + amount);

			} finally {
				secondLock.unlock();
			}
		} finally {
			firstLock.unlock();
		}

		printCurrentBalancet(fromAccountId, toAccountId);

	}

	private void printCurrentBalancet(String fromAccountId, String toAccountId) {
		Account debitAccount = getAccount(fromAccountId);
		if (debitAccount != null) {
			log.info("Account Id : {} Current A/c balance : {}", fromAccountId, debitAccount.getBalance());
		}
		Account creditAccount = getAccount(toAccountId);
		if (creditAccount != null) {
			log.info("Account Id : {} Current A/c balance : {}", toAccountId, creditAccount.getBalance());
		}
	}

	private void accountToDeposit(Account accountTo, BigDecimal amount) {
		BigDecimal deposit = accountTo.getBalance().add(amount);
		accountTo.setBalance(deposit);
		accountsRepository.updateAccount(accountTo);
	}

	private void accountFromWithdraw(Account accountFrom, BigDecimal amount) {
		BigDecimal witdrow = accountFrom.getBalance().subtract(amount);
		accountFrom.setBalance(witdrow);
		accountsRepository.updateAccount(accountFrom);
	}

	private Lock getLock(String accountId) {
		accountLocks.putIfAbsent(accountId, new ReentrantLock());
		return accountLocks.get(accountId);
	}

	public List<Account> getAllAccounts() {
		return accountsRepository.getAllAccounts();
	}

}
