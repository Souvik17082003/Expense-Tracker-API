package com.example.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.example.demo.dto.BalanceResponse;
import com.example.demo.dto.MonthlyReportDTO;
import com.example.demo.dto.YearlyReportDTO;
import com.example.demo.entity.Transction;
import com.example.demo.enumm.Catagory;
import com.example.demo.enumm.TransctionType;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repositry.TransctionRepositry;
import com.example.demo.service.TransctionService;

@ExtendWith(MockitoExtension.class)
public class TransctionServiceTest {

	@Mock
	private TransctionRepositry repositry;

	@InjectMocks
	private TransctionService service;

	private Transction incomeTx;
	private Transction expenseTx;

	@BeforeEach
	void setUp() throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date txDate = sdf.parse("2026-09-13");

		incomeTx = new Transction();
		incomeTx.setId(1L);
		incomeTx.setAmount(5000.0);
		incomeTx.setCatagory(Catagory.Salary);
		incomeTx.setTransType(TransctionType.income);
		incomeTx.setDescription("Monthly salary");
		incomeTx.setDate(txDate);

		expenseTx = new Transction();
		expenseTx.setId(2L);
		expenseTx.setAmount(1500.0);
		expenseTx.setCatagory(Catagory.Food);
		expenseTx.setTransType(TransctionType.expenses);
		expenseTx.setDescription("Groceries");
		expenseTx.setDate(txDate);
	}

	@Test
	void testGetIncomeTransactions() {
		when(repositry.findByTransType(TransctionType.income)).thenReturn(List.of(incomeTx));

		List<Transction> results = service.getIncomeTransactions();
		assertEquals(1, results.size());
		assertEquals(TransctionType.income, results.get(0).getTransType());
		assertEquals(5000.0, results.get(0).getAmount());
	}

	@Test
	void testGetExpenseTransactions() {
		when(repositry.findByTransType(TransctionType.expenses)).thenReturn(List.of(expenseTx));

		List<Transction> results = service.getExpenseTransactions();
		assertEquals(1, results.size());
		assertEquals(TransctionType.expenses, results.get(0).getTransType());
		assertEquals(1500.0, results.get(0).getAmount());
	}

	@Test
	void testGetTransactionsByCategoryValid() {
		when(repositry.findByCatagory(Catagory.Food)).thenReturn(List.of(expenseTx));

		List<Transction> results = service.getTransactionsByCategory("Food");
		assertEquals(1, results.size());
		assertEquals(Catagory.Food, results.get(0).getCatagory());

		// Case-insensitive test
		List<Transction> resultsIgnoreCase = service.getTransactionsByCategory("food");
		assertEquals(1, resultsIgnoreCase.size());
	}

	@Test
	void testGetTransactionsByCategoryInvalid() {
		assertThrows(ResourceNotFoundException.class, () -> {
			service.getTransactionsByCategory("InvalidCategoryName");
		});
	}

	@Test
	void testGetBalance() {
		when(repositry.findAll()).thenReturn(Arrays.asList(incomeTx, expenseTx));

		BalanceResponse balanceResponse = service.getBalance();
		assertNotNull(balanceResponse);
		assertEquals(5000.0, balanceResponse.getTotalIncome());
		assertEquals(1500.0, balanceResponse.getTotalExpense());
		assertEquals(3500.0, balanceResponse.getBalance());
	}

	@Test
	void testSearchByDescription() {
		when(repositry.findByDescriptionContainingIgnoreCase("Groceries")).thenReturn(List.of(expenseTx));

		List<Transction> results = service.searchByDescription("Groceries");
		assertEquals(1, results.size());
		assertEquals("Groceries", results.get(0).getDescription());
	}

	@Test
	void testGetTransactionsByDate() {
		when(repositry.findByDateBetween(any(Date.class), any(Date.class))).thenReturn(List.of(expenseTx));

		List<Transction> results = service.getTransactionsByDate("2026-09-13");
		assertEquals(1, results.size());
	}

	@Test
	void testGetTransactionsByType() {
		when(repositry.findByTransType(TransctionType.expenses)).thenReturn(List.of(expenseTx));

		List<Transction> results = service.getTransactionsByType("expenses");
		assertEquals(1, results.size());

		assertThrows(ResourceNotFoundException.class, () -> {
			service.getTransactionsByType("invalid-type");
		});
	}

	@Test
	void testGetTransactionsByDateRange() {
		when(repositry.findByDateBetween(any(Date.class), any(Date.class))).thenReturn(Arrays.asList(incomeTx, expenseTx));

		List<Transction> results = service.getTransactionsByDateRange("2026-09-01", "2026-09-30");
		assertEquals(2, results.size());
	}

	@Test
	void testGetTransactionsByAmountRange() {
		when(repositry.findByAmountBetween(1000.0, 2000.0)).thenReturn(List.of(expenseTx));

		List<Transction> results = service.getTransactionsByAmountRange(1000.0, 2000.0);
		assertEquals(1, results.size());
	}

	@Test
	void testGetCategorySummary() {
		when(repositry.findByTransType(TransctionType.expenses)).thenReturn(List.of(expenseTx));

		Map<String, Double> summary = service.getCategorySummary();
		assertEquals(1500.0, summary.get("Food"));
	}

	@Test
	void testGetHighestExpenseAndIncome() {
		when(repositry.findFirstByTransTypeOrderByAmountDesc(TransctionType.expenses)).thenReturn(Optional.of(expenseTx));
		when(repositry.findFirstByTransTypeOrderByAmountDesc(TransctionType.income)).thenReturn(Optional.of(incomeTx));

		Transction highestExp = service.getHighestExpense();
		assertNotNull(highestExp);
		assertEquals(1500.0, highestExp.getAmount());

		Transction highestInc = service.getHighestIncome();
		assertNotNull(highestInc);
		assertEquals(5000.0, highestInc.getAmount());
	}

	@Test
	void testMonthlyAndYearlyReport() {
		when(repositry.findAll()).thenReturn(Arrays.asList(incomeTx, expenseTx));

		List<MonthlyReportDTO> monthly = service.getMonthlyReport(2026, 9);
		assertEquals(1, monthly.size());
		assertEquals("2026-09", monthly.get(0).getMonth());
		assertEquals(5000.0, monthly.get(0).getTotalIncome());
		assertEquals(1500.0, monthly.get(0).getTotalExpense());
		assertEquals(3500.0, monthly.get(0).getBalance());

		List<YearlyReportDTO> yearly = service.getYearlyReport(2026);
		assertEquals(1, yearly.size());
		assertEquals(2026, yearly.get(0).getYear());
		assertEquals(5000.0, yearly.get(0).getTotalIncome());
	}

	@Test
	void testGetTransactionsPageable() {
		Pageable pageable = PageRequest.of(0, 10);
		Page<Transction> page = new PageImpl<>(List.of(incomeTx));
		when(repositry.findAll(pageable)).thenReturn(page);

		Page<Transction> resultPage = service.getTransactions(pageable);
		assertEquals(1, resultPage.getContent().size());
	}
}
