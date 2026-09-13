package com.example.demo.controller;

import java.util.List;
import java.util.Map;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.BalanceResponse;
import com.example.demo.dto.MonthlyReportDTO;
import com.example.demo.dto.YearlyReportDTO;
import com.example.demo.entity.Transction;
import com.example.demo.service.TransctionService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping({"/api/transactions", "/api/v1/transction"})
public class TransctionController {


	private TransctionService service;
	public TransctionController(TransctionService service) {
		this.service=service;
	}

	@GetMapping
	public ResponseEntity<Page<Transction>> getTransactions(Pageable pageable) {
		Page<Transction> page = service.getTransactions(pageable);
		return ResponseEntity.ok(page);
	}
	
	@PostMapping("/create")
	public ResponseEntity<Transction>createTrans(@RequestBody Transction transction){
	Transction tra	=service.createTransct(transction);
	return ResponseEntity.status(HttpStatus.CREATED).body(tra);
	}
	
	
	@GetMapping("/{id}")
	public ResponseEntity<Transction>getTransction(@PathVariable Long id){
	Transction ta	=service.getTrans(id);
	return ResponseEntity.status(HttpStatus.ACCEPTED).body(ta);
	}
	
	@GetMapping("/all")
	public ResponseEntity<List<Transction>>getAllTrans(){
	List<Transction>listtra	=service.getAllTrans();
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(listtra);
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<Transction>updateTrasn(@PathVariable Long id,@RequestBody Transction transction){
	Transction ta	=service.updateTrans(id,transction);
	return ResponseEntity.status(HttpStatus.ACCEPTED).body(ta);
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<String>deleteTran(@PathVariable Long id){
		service.deleteTranas(id);
	return ResponseEntity.ok("Deleted succesfully");
	}

	@GetMapping("/income")
	public ResponseEntity<List<Transction>> getIncomeTransactions() {
		List<Transction> incomeList = service.getIncomeTransactions();
		return ResponseEntity.ok(incomeList);
	}

	@GetMapping("/expenses")
	public ResponseEntity<List<Transction>> getExpenseTransactions() {
		List<Transction> expenseList = service.getExpenseTransactions();
		return ResponseEntity.ok(expenseList);
	}

	@GetMapping("/category/{category}")
	public ResponseEntity<List<Transction>> getTransactionsByCategory(@PathVariable String category) {
		List<Transction> categoryList = service.getTransactionsByCategory(category);
		return ResponseEntity.ok(categoryList);
	}
	
	@GetMapping("/balance")
	public ResponseEntity<BalanceResponse> getBalance() {
		BalanceResponse balance = service.getBalance();
		return ResponseEntity.ok(balance);
	}

	@GetMapping("/search")
	public ResponseEntity<List<Transction>> searchTransactions(
			@RequestParam(required = false) String keyword,
			@RequestParam(required = false) String description,
			@RequestParam(required = false) String q) {
		String query = keyword != null ? keyword : (description != null ? description : q);
		List<Transction> results = service.searchByDescription(query);
		return ResponseEntity.ok(results);
	}

	@GetMapping("/date/{date}")
	public ResponseEntity<List<Transction>> getTransactionsByDate(@PathVariable String date) {
		List<Transction> list = service.getTransactionsByDate(date);
		return ResponseEntity.ok(list);
	}

	@GetMapping("/type/{type}")
	public ResponseEntity<List<Transction>> getTransactionsByType(@PathVariable String type) {
		List<Transction> list = service.getTransactionsByType(type);
		return ResponseEntity.ok(list);
	}

	@GetMapping("/date-range")
	public ResponseEntity<List<Transction>> getTransactionsByDateRange(
			@RequestParam(required = false) String startDate,
			@RequestParam(required = false) String endDate,
			@RequestParam(required = false) String start,
			@RequestParam(required = false) String end) {
		String from = startDate != null ? startDate : start;
		String to = endDate != null ? endDate : end;
		List<Transction> list = service.getTransactionsByDateRange(from, to);
		return ResponseEntity.ok(list);
	}

	@GetMapping("/amount-range")
	public ResponseEntity<List<Transction>> getTransactionsByAmountRange(
			@RequestParam(required = false) Double min,
			@RequestParam(required = false) Double max,
			@RequestParam(required = false) Double minAmount,
			@RequestParam(required = false) Double maxAmount) {
		Double from = min != null ? min : minAmount;
		Double to = max != null ? max : maxAmount;
		List<Transction> list = service.getTransactionsByAmountRange(from, to);
		return ResponseEntity.ok(list);
	}

	@GetMapping("/monthly")
	public ResponseEntity<List<MonthlyReportDTO>> getMonthlyReport(
			@RequestParam(required = false) Integer year,
			@RequestParam(required = false) Integer month) {
		List<MonthlyReportDTO> report = service.getMonthlyReport(year, month);
		return ResponseEntity.ok(report);
	}

	@GetMapping("/yearly")
	public ResponseEntity<List<YearlyReportDTO>> getYearlyReport(
			@RequestParam(required = false) Integer year) {
		List<YearlyReportDTO> report = service.getYearlyReport(year);
		return ResponseEntity.ok(report);
	}

	@GetMapping("/category-summary")
	public ResponseEntity<Map<String, Double>> getCategorySummary() {
		Map<String, Double> summary = service.getCategorySummary();
		return ResponseEntity.ok(summary);
	}

	@GetMapping("/highest-expense")
	public ResponseEntity<Transction> getHighestExpense() {
		Transction tx = service.getHighestExpense();
		return ResponseEntity.ok(tx);
	}

	@GetMapping("/highest-income")
	public ResponseEntity<Transction> getHighestIncome() {
		Transction tx = service.getHighestIncome();
		return ResponseEntity.ok(tx);
	}
}
