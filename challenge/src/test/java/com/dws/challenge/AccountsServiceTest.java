package com.dws.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.exception.InsufficientBalanceException;
import com.dws.challenge.exception.NotFoundAccountIdException;
import com.dws.challenge.service.AccountsService;

@SpringBootTest
class AccountsServiceTest {

	@Autowired
	private AccountsService accountsService;

	@Test
	void addAccount() {
		Account account = new Account("Id-123");
		account.setBalance(new BigDecimal(1000));
		this.accountsService.createAccount(account);

		assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
	}

	@Test
	void addAccount_failsOnDuplicateId() {
		String uniqueId = "Id-" + System.currentTimeMillis();
		Account account = new Account(uniqueId);
		this.accountsService.createAccount(account);

		try {
			this.accountsService.createAccount(account);
			fail("Should have failed when adding duplicate account");
		} catch (DuplicateAccountIdException ex) {
			assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
		}
	}

	@Test
	void testSuccessfulTransfer() throws Exception {

		createAccountData();
		String fromAccountId = "Id-101";
		String toAccountId = "Id-102";
		BigDecimal amount = new BigDecimal(2000);
		this.accountsService.transferAmount(fromAccountId, toAccountId, amount);
		Account account = this.accountsService.getAccount("Id-102");
		assertThat(account.getBalance()).isEqualTo(new BigDecimal(4000));
	}

	@Test
	void testTransferWithInsufficientBalance() throws Exception {
		createAccountData();
		String fromAccountId = "Id-101";
		String toAccountId = "Id-102";
		BigDecimal amount = new BigDecimal(10000);
		try {
			this.accountsService.transferAmount(fromAccountId, toAccountId, amount);
			fail("nsufficient balance");
		} catch (InsufficientBalanceException ex) {
			assertThat(ex.getMessage())
					.isEqualTo("Insufficient balance " + amount + " to trasfer from account " + fromAccountId + "");
		}
	}

	@Test
	void testTransferWithInvalidAccountId() throws Exception {
		createAccountData();
		String fromAccountId = "Id-100";
		String toAccountId = "Id-102";
		BigDecimal transferAmount = new BigDecimal(2000);
		try {
			this.accountsService.transferAmount(fromAccountId, toAccountId, transferAmount);
			fail("Invalid account ID");
		} catch (NotFoundAccountIdException ex) {
			assertThat(ex.getMessage()).isEqualTo("Invalid account ID");
		}

	}

	@Test
	public void testConcurrentTransfers() throws InterruptedException {

		createAccountConcurrentData();
		String fromAccountId = "Id-1001";
		String toAccountId = "Id-1002";
		BigDecimal transferAmount = new BigDecimal(100);

		int numberOfThreads = 10;
		CountDownLatch latch = new CountDownLatch(numberOfThreads);
		ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

		for (int i = 0; i < numberOfThreads; i++) {
			executor.submit(() -> {
				try {
					this.accountsService.transferAmount(fromAccountId, toAccountId, transferAmount);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					latch.countDown();
				}
			});
		}

		latch.await();
		executor.shutdown();
		Account account = this.accountsService.getAccount(fromAccountId);
		assertEquals(BigDecimal.valueOf(1000).subtract(transferAmount.multiply(BigDecimal.valueOf(numberOfThreads))),
				account.getBalance());

		account = this.accountsService.getAccount(toAccountId);
		assertEquals(BigDecimal.valueOf(1000).add(transferAmount.multiply(BigDecimal.valueOf(numberOfThreads))),
				account.getBalance());

	}

	private void createAccountConcurrentData() {

		accountsService.clearAccounts();

		Account account = new Account("Id-" + 1001, new BigDecimal("1000"));
		this.accountsService.createAccount(account);

		account = new Account("Id-" + 1002, new BigDecimal("1000"));
		this.accountsService.createAccount(account);

	}

	private void createAccountData() {

		accountsService.clearAccounts();

		Account account = new Account("Id-" + 101, new BigDecimal("5000"));
		this.accountsService.createAccount(account);

		account = new Account("Id-" + 102, new BigDecimal("2000"));
		this.accountsService.createAccount(account);

		account = new Account("Id-" + 103, new BigDecimal("1000"));
		this.accountsService.createAccount(account);

		account = new Account("Id-" + 104, new BigDecimal("4000"));
		this.accountsService.createAccount(account);
	}
}
