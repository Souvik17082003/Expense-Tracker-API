package com.example.demo.service;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.demo.dto.BalanceResponse;
import com.example.demo.dto.MonthlyReportDTO;
import com.example.demo.dto.YearlyReportDTO;
import com.example.demo.entity.Transction;
import com.example.demo.enumm.Catagory;
import com.example.demo.enumm.TransctionType;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repositry.TransctionRepositry;

@Service
public class TransctionService {
private TransctionRepositry repositry;
	
	public TransctionService(TransctionRepositry repositry) {
		this.repositry=repositry;
	}

	public Transction createTransct(Transction transction) {
		// TODO Auto-generated method stub
	return  repositry.save(transction);
	
	}

	public Transction getTrans(Long id) {
		// TODO Auto-generated method stub
	Transction ta	=repositry.findById(id)
						.orElseThrow(()->new ResourceNotFoundException("The transction with this "+id+"is not exsist"));
		return  ta;
	}

	public List<Transction> getAllTrans() {
		// TODO Auto-generated method stub
List<Transction>tralistt		=repositry.findAll();
		return tralistt;
	}

	public Transction updateTrans(Long id, Transction transreq) {
		// TODO Auto-generated method stub
	Transction tra	=repositry.findById(id)
		.orElseThrow(()-> new ResourceNotFoundException("The Transction with this "+id+"is not present"));
		
	tra.setAmount(transreq.getAmount());
	tra.setCatagory(transreq.getCatagory());
	tra.setDate(transreq.getDate());
	tra.setDescription(transreq.getDescription());
	tra.setTransType(transreq.getTransType());
	repositry.save(tra);
return tra;
	}

	public void deleteTranas(Long id) {
		// TODO Auto-generated method stub
	Transction ta	=repositry.findById(id)
		.orElseThrow(()-> new ResourceNotFoundException("The Transction with this "+id+"is not present"));
		repositry.delete(ta);
	}

	public List<Transction> getIncomeTransactions() {
		return repositry.findByTransType(TransctionType.income);
	}

	public List<Transction> getExpenseTransactions() {
		return repositry.findByTransType(TransctionType.expenses);
	}

	public List<Transction> getTransactionsByCategory(String categoryName) {
		Catagory matchedCategory = null;
		for (Catagory cat : Catagory.values()) {
			if (cat.name().equalsIgnoreCase(categoryName)) {
				matchedCategory = cat;
				break;
			}
		}
		if (matchedCategory == null) {
			throw new ResourceNotFoundException("Category '" + categoryName + "' is not valid");
		}
		return repositry.findByCatagory(matchedCategory);
	}

	public BalanceResponse getBalance() {
		List<Transction> all = repositry.findAll();
		double totalIncome = all.stream()
				.filter(t -> t.getTransType() == TransctionType.income)
				.mapToDouble(Transction::getAmount)
				.sum();
		double totalExpense = all.stream()
				.filter(t -> t.getTransType() == TransctionType.expenses)
				.mapToDouble(Transction::getAmount)
				.sum();
		double balance = totalIncome - totalExpense;
		return new BalanceResponse(totalIncome, totalExpense, balance);
	}

	public List<Transction> searchByDescription(String keyword) {
		if (keyword == null || keyword.trim().isEmpty()) {
			return repositry.findAll();
		}
		return repositry.findByDescriptionContainingIgnoreCase(keyword.trim());
	}

	public List<Transction> getTransactionsByDate(String dateStr) {
		Date start = parseDate(dateStr, false);
		Date end = parseDate(dateStr, true);
		return repositry.findByDateBetween(start, end);
	}

	public List<Transction> getTransactionsByType(String type) {
		TransctionType matchedType = null;
		for (TransctionType t : TransctionType.values()) {
			if (t.name().equalsIgnoreCase(type)) {
				matchedType = t;
				break;
			}
		}
		if (matchedType == null) {
			throw new ResourceNotFoundException("Transaction type '" + type + "' is not valid");
		}
		return repositry.findByTransType(matchedType);
	}

	public List<Transction> getTransactionsByDateRange(String startStr, String endStr) {
		Date start = (startStr != null && !startStr.trim().isEmpty()) ? parseDate(startStr, false) : new Date(0L);
		Date end = (endStr != null && !endStr.trim().isEmpty()) ? parseDate(endStr, true) : new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 365 * 100);
		return repositry.findByDateBetween(start, end);
	}

	public List<Transction> getTransactionsByAmountRange(Double min, Double max) {
		double minAmount = min != null ? min : 0.0;
		double maxAmount = max != null ? max : Double.MAX_VALUE;
		return repositry.findByAmountBetween(minAmount, maxAmount);
	}

	public Map<String, Double> getCategorySummary() {
		List<Transction> expenses = repositry.findByTransType(TransctionType.expenses);
		return expenses.stream()
				.filter(t -> t.getCatagory() != null)
				.collect(Collectors.groupingBy(
						t -> t.getCatagory().name(),
						Collectors.summingDouble(Transction::getAmount)
				));
	}

	public Transction getHighestExpense() {
		return repositry.findFirstByTransTypeOrderByAmountDesc(TransctionType.expenses)
				.orElse(null);
	}

	public Transction getHighestIncome() {
		return repositry.findFirstByTransTypeOrderByAmountDesc(TransctionType.income)
				.orElse(null);
	}

	public List<MonthlyReportDTO> getMonthlyReport(Integer year, Integer month) {
		List<Transction> all = repositry.findAll();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");

		Map<String, List<Transction>> grouped = all.stream()
				.filter(t -> t.getDate() != null)
				.collect(Collectors.groupingBy(t -> sdf.format(t.getDate())));

		List<MonthlyReportDTO> reports = new ArrayList<>();
		grouped.forEach((ym, txList) -> {
			String[] parts = ym.split("-");
			int txYear = Integer.parseInt(parts[0]);
			int txMonth = Integer.parseInt(parts[1]);

			if (year != null && txYear != year) {
				return;
			}
			if (month != null && txMonth != month) {
				return;
			}

			double income = txList.stream()
					.filter(t -> t.getTransType() == TransctionType.income)
					.mapToDouble(Transction::getAmount)
					.sum();
			double expense = txList.stream()
					.filter(t -> t.getTransType() == TransctionType.expenses)
					.mapToDouble(Transction::getAmount)
					.sum();
			double bal = income - expense;
			reports.add(new MonthlyReportDTO(ym, income, expense, bal, txList.size()));
		});

		reports.sort(Comparator.comparing(MonthlyReportDTO::getMonth).reversed());
		return reports;
	}

	public List<YearlyReportDTO> getYearlyReport(Integer year) {
		List<Transction> all = repositry.findAll();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy");

		Map<Integer, List<Transction>> grouped = all.stream()
				.filter(t -> t.getDate() != null)
				.collect(Collectors.groupingBy(t -> Integer.parseInt(sdf.format(t.getDate()))));

		List<YearlyReportDTO> reports = new ArrayList<>();
		grouped.forEach((txYear, txList) -> {
			if (year != null && !txYear.equals(year)) {
				return;
			}

			double income = txList.stream()
					.filter(t -> t.getTransType() == TransctionType.income)
					.mapToDouble(Transction::getAmount)
					.sum();
			double expense = txList.stream()
					.filter(t -> t.getTransType() == TransctionType.expenses)
					.mapToDouble(Transction::getAmount)
					.sum();
			double bal = income - expense;
			reports.add(new YearlyReportDTO(txYear, income, expense, bal, txList.size()));
		});

		reports.sort(Comparator.comparing(YearlyReportDTO::getYear).reversed());
		return reports;
	}

	public Page<Transction> getTransactions(Pageable pageable) {
		return repositry.findAll(pageable);
	}

	private Date parseDate(String dateStr, boolean isEndOfDay) {
		if (dateStr == null || dateStr.trim().isEmpty()) {
			return null;
		}
		try {
			LocalDate localDate = LocalDate.parse(dateStr.trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			LocalDateTime dateTime = isEndOfDay ? localDate.atTime(LocalTime.MAX) : localDate.atStartOfDay();
			return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
		} catch (Exception e) {
			try {
				LocalDateTime ldt = LocalDateTime.parse(dateStr.trim(), DateTimeFormatter.ISO_DATE_TIME);
				return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
			} catch (Exception ex) {
				throw new ResourceNotFoundException("Invalid date format: '" + dateStr + "'. Expected format: yyyy-MM-dd");
			}
		}
	}
}
