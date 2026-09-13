package com.example.demo.dto;

public class YearlyReportDTO {

	private int year;
	private double totalIncome;
	private double totalExpense;
	private double balance;
	private long transactionCount;

	public YearlyReportDTO() {
	}

	public YearlyReportDTO(int year, double totalIncome, double totalExpense, double balance, long transactionCount) {
		this.year = year;
		this.totalIncome = totalIncome;
		this.totalExpense = totalExpense;
		this.balance = balance;
		this.transactionCount = transactionCount;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
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
