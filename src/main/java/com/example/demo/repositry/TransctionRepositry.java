package com.example.demo.repositry;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Transction;
import com.example.demo.enumm.Catagory;
import com.example.demo.enumm.TransctionType;

@Repository
public interface TransctionRepositry extends JpaRepository<Transction, Long> {

	List<Transction> findByTransType(TransctionType transType);

	List<Transction> findByCatagory(Catagory catagory);

	List<Transction> findByDescriptionContainingIgnoreCase(String keyword);

	List<Transction> findByDateBetween(Date startDate, Date endDate);

	List<Transction> findByAmountBetween(double minAmount, double maxAmount);

	Optional<Transction> findFirstByTransTypeOrderByAmountDesc(TransctionType transType);
}


