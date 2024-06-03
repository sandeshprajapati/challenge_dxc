package com.dws.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.math.BigDecimal;

import com.dws.challenge.domain.Account;
import com.dws.challenge.service.AccountsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@WebAppConfiguration
class AccountsControllerTest {

	private MockMvc mockMvc;

	@Autowired
	private AccountsService accountsService;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@BeforeEach
	void prepareMockMvc() {
		this.mockMvc = webAppContextSetup(this.webApplicationContext).build();

		// Reset the existing accounts before each test.
		accountsService.getAccountsRepository().clearAccounts();
	}

	@Test
	void createAccount() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

		Account account = accountsService.getAccount("Id-123");
		assertThat(account.getAccountId()).isEqualTo("Id-123");
		assertThat(account.getBalance()).isEqualByComparingTo("1000");
	}

	@Test
	void createDuplicateAccount() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isBadRequest());
	}

	@Test
	void createAccountNoAccountId() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON).content("{\"balance\":1000}"))
				.andExpect(status().isInternalServerError());
	}

	@Test
	void createAccountNoBalance() throws Exception {
		this.mockMvc.perform(
				post("/v1/accounts").contentType(MediaType.APPLICATION_JSON).content("{\"accountId\":\"Id-123\"}"))
				.andExpect(status().isInternalServerError());
	}

	@Test
	void createAccountNoBody() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	void createAccountNegativeBalance() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"Id-123\",\"balance\":-1000}")).andExpect(status().isInternalServerError());
	}

	@Test
	void createAccountEmptyAccountId() throws Exception {
		this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
				.content("{\"accountId\":\"\",\"balance\":1000}")).andExpect(status().isInternalServerError());
	}

	@Test
	void getAccount() throws Exception {
		String uniqueAccountId = "Id-" + System.currentTimeMillis();
		Account account = new Account(uniqueAccountId, new BigDecimal("123.45"));
		this.accountsService.createAccount(account);
		this.mockMvc.perform(get("/v1/accounts/" + uniqueAccountId)).andExpect(status().isOk())
				.andExpect(content().string("{\"accountId\":\"" + uniqueAccountId + "\",\"balance\":123.45}"));
	}

	@Test
	void testSuccessfulTransfer() throws Exception {

		createAccountData();

		this.mockMvc
				.perform(post("/v1/accounts/transfer-amount").contentType(MediaType.APPLICATION_JSON)
						.content("{\"fromAccountId\":\"Id-101\",\"toAccountId\":\"Id-102\",\"amount\":1000}"))
				.andExpect(status().isOk());
	}

	@Test
	void testTransferWithInsufficientBalance() throws Exception {

		createAccountData();

		this.mockMvc
				.perform(post("/v1/accounts/transfer-amount").contentType(MediaType.APPLICATION_JSON)
						.content("{\"fromAccountId\":\"Id-101\",\"toAccountId\":\"Id-102\",\"amount\":10000}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void testTransferWithNegativeAmount() throws Exception {

		createAccountData();

		this.mockMvc
				.perform(post("/v1/accounts/transfer-amount").contentType(MediaType.APPLICATION_JSON)
						.content("{\"fromAccountId\":\"Id-101\",\"toAccountId\":\"Id-102\",\"amount\":-10000}"))
				.andExpect(status().isInternalServerError());
	}
	
	@Test
	void testTransferWithInvalidAccountId() throws Exception {

		createAccountData();

		this.mockMvc
				.perform(post("/v1/accounts/transfer-amount").contentType(MediaType.APPLICATION_JSON)
						.content("{\"fromAccountId\":\"Id-107\",\"toAccountId\":\"Id-102\",\"amount\":10000}"))
				.andExpect(status().isBadRequest());
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
