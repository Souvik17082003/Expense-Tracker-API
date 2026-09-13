package com.example.demo.dto;

public class BalanceResponse {

	private double totalIncome;
	private double totalExpense;
	private double balance;

	public BalanceResponse() {
	}

	public BalanceResponse(double totalIncome, double totalExpense, double balance) {
		this.totalIncome = totalIncome;
		this.totalExpense = totalExpense;
		this.balance = balance;
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
}
