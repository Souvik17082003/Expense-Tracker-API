package com.example.demo.dto;

public class MonthlyReportDTO {

	private String month; // e.g. "2026-09"
	private double totalIncome;
	private double totalExpense;
	private double balance;
	private long transactionCount;

	public MonthlyReportDTO() {
	}

	public MonthlyReportDTO(String month, double totalIncome, double totalExpense, double balance, long transactionCount) {
		this.month = month;
		this.totalIncome = totalIncome;
		this.totalExpense = totalExpense;
		this.balance = balance;
		this.transactionCount = transactionCount;
	}

	public String getMonth() {
		return month;
	}

	public void setMonth(String month) {
		this.month = month;
	}

	public double getTotalIncome() {
		return totalIncome;
	}

	public void setTotalIncome(double totalIncome) {
		this.totalIncome = totalIncome;
	}

	public double getTotalExpense() {
		return totalExpense;
	}

	public void setTotalExpense(double totalExpense) {
		this.totalExpense = totalExpense;
	}

	public double getBalance() {
		return balance;
	}

	public void setBalance(double balance) {
		this.balance = balance;
	}

	public long getTransactionCount() {
		return transactionCount;
	}

	public void setTransactionCount(long transactionCount) {
		this.transactionCount = transactionCount;
	}
}
