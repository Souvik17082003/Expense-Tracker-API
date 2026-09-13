package com.example.demo;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.controller.TransctionController;
import com.example.demo.dto.BalanceResponse;
import com.example.demo.dto.MonthlyReportDTO;
import com.example.demo.dto.YearlyReportDTO;
import com.example.demo.entity.Transction;
import com.example.demo.enumm.Catagory;
import com.example.demo.enumm.TransctionType;
import com.example.demo.service.TransctionService;

@WebMvcTest(TransctionController.class)
public class TransctionControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private TransctionService service;

	private Transction incomeTx;
	private Transction expenseTx;

	@BeforeEach
	void setUp() {
		incomeTx = new Transction();
		incomeTx.setId(1L);
		incomeTx.setAmount(5000.0);
		incomeTx.setCatagory(Catagory.Salary);
		incomeTx.setTransType(TransctionType.income);
		incomeTx.setDescription("Monthly salary");

		expenseTx = new Transction();
		expenseTx.setId(2L);
		expenseTx.setAmount(1500.0);
		expenseTx.setCatagory(Catagory.Food);
		expenseTx.setTransType(TransctionType.expenses);
		expenseTx.setDescription("Groceries");
	}

	@Test
	void testGetIncomeTransactions() throws Exception {
		when(service.getIncomeTransactions()).thenReturn(List.of(incomeTx));

		mockMvc.perform(get("/api/transactions/income"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].amount").value(5000.0))
				.andExpect(jsonPath("$[0].transType").value("income"));
	}

	@Test
	void testGetExpenseTransactions() throws Exception {
		when(service.getExpenseTransactions()).thenReturn(List.of(expenseTx));

		mockMvc.perform(get("/api/transactions/expenses"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].amount").value(1500.0))
				.andExpect(jsonPath("$[0].transType").value("expenses"));
	}

	@Test
	void testGetTransactionsByCategory() throws Exception {
		when(service.getTransactionsByCategory("Food")).thenReturn(List.of(expenseTx));

		mockMvc.perform(get("/api/transactions/category/Food"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].catagory").value("Food"))
				.andExpect(jsonPath("$[0].amount").value(1500.0));
	}

	@Test
	void testGetBalance() throws Exception {
		BalanceResponse balance = new BalanceResponse(5000.0, 1500.0, 3500.0);
		when(service.getBalance()).thenReturn(balance);

		mockMvc.perform(get("/api/transactions/balance"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalIncome").value(5000.0))
				.andExpect(jsonPath("$.totalExpense").value(1500.0))
				.andExpect(jsonPath("$.balance").value(3500.0));
	}

	@Test
	void testSearchTransactions() throws Exception {
		when(service.searchByDescription("Groceries")).thenReturn(List.of(expenseTx));

		mockMvc.perform(get("/api/transactions/search").param("keyword", "Groceries"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].description").value("Groceries"));
	}

	@Test
	void testGetTransactionsByDate() throws Exception {
		when(service.getTransactionsByDate("2026-09-13")).thenReturn(List.of(expenseTx));

		mockMvc.perform(get("/api/transactions/date/2026-09-13"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(2));
	}

	@Test
	void testGetTransactionsByType() throws Exception {
		when(service.getTransactionsByType("income")).thenReturn(List.of(incomeTx));

		mockMvc.perform(get("/api/transactions/type/income"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].transType").value("income"));
	}

	@Test
	void testGetTransactionsByDateRange() throws Exception {
		when(service.getTransactionsByDateRange("2026-09-01", "2026-09-30")).thenReturn(List.of(expenseTx));

		mockMvc.perform(get("/api/transactions/date-range")
				.param("startDate", "2026-09-01")
				.param("endDate", "2026-09-30"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(2));
	}

	@Test
	void testGetTransactionsByAmountRange() throws Exception {
		when(service.getTransactionsByAmountRange(1000.0, 2000.0)).thenReturn(List.of(expenseTx));

		mockMvc.perform(get("/api/transactions/amount-range")
				.param("min", "1000.0")
				.param("max", "2000.0"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].amount").value(1500.0));
	}

	@Test
	void testGetMonthlyReport() throws Exception {
		MonthlyReportDTO report = new MonthlyReportDTO("2026-09", 5000.0, 1500.0, 3500.0, 2);
		when(service.getMonthlyReport(2026, 9)).thenReturn(List.of(report));

		mockMvc.perform(get("/api/transactions/monthly")
				.param("year", "2026")
				.param("month", "9"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].month").value("2026-09"))
				.andExpect(jsonPath("$[0].totalIncome").value(5000.0))
				.andExpect(jsonPath("$[0].totalExpense").value(1500.0))
				.andExpect(jsonPath("$[0].balance").value(3500.0));
	}

	@Test
	void testGetYearlyReport() throws Exception {
		YearlyReportDTO report = new YearlyReportDTO(2026, 5000.0, 1500.0, 3500.0, 2);
		when(service.getYearlyReport(2026)).thenReturn(List.of(report));

		mockMvc.perform(get("/api/transactions/yearly")
				.param("year", "2026"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].year").value(2026))
				.andExpect(jsonPath("$[0].totalIncome").value(5000.0));
	}

	@Test
	void testGetCategorySummary() throws Exception {
		when(service.getCategorySummary()).thenReturn(Map.of("Food", 1500.0));

		mockMvc.perform(get("/api/transactions/category-summary"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.Food").value(1500.0));
	}

	@Test
	void testGetHighestExpense() throws Exception {
		when(service.getHighestExpense()).thenReturn(expenseTx);

		mockMvc.perform(get("/api/transactions/highest-expense"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.amount").value(1500.0));
	}

	@Test
	void testGetHighestIncome() throws Exception {
		when(service.getHighestIncome()).thenReturn(incomeTx);

		mockMvc.perform(get("/api/transactions/highest-income"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.amount").value(5000.0));
	}

	@Test
	void testGetTransactionsPaginationAndSorting() throws Exception {
		Page<Transction> page = new PageImpl<>(List.of(incomeTx, expenseTx));
		when(service.getTransactions(any(Pageable.class))).thenReturn(page);

		mockMvc.perform(get("/api/transactions")
				.param("page", "0")
				.param("size", "10")
				.param("sort", "date,desc"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content.length()").value(2));
	}
}
