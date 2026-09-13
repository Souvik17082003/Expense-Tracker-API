package com.example.demo.entity;

import java.util.Date;

import com.example.demo.enumm.Catagory;
import com.example.demo.enumm.TransctionType;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Transction {
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private double amount;
	private Date date;
	private String description;
	private  Catagory catagory;
	private TransctionType transType;
	
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public double getAmount() {
		return amount;
	}
	public void setAmount(double amount) {
		this.amount = amount;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	
	public Catagory getCatagory() {
		return catagory;
	}
	public void setCatagory(Catagory catagory) {
		this.catagory = catagory;
	}
	public TransctionType getTransType() {
		return transType;
	}
	public void setTransType(TransctionType transType) {
		this.transType = transType;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}


}
