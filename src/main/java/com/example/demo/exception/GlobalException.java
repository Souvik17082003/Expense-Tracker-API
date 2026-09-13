package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalException {


	@ExceptionHandler(ResourceNotFoundException.class)
		 public ResponseEntity<String>ResourceNotFoundhander(ResourceNotFoundException ex){
return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
		 }
	
	@ExceptionHandler(DuplicateResourceException.class)
		 public ResponseEntity<String>DuplicateResourcehandler(DuplicateResourceException ex){
return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
		 }
	
	@ExceptionHandler(RuntimeException.class)
		 public ResponseEntity<String>RuntimeExceptionhandler(RuntimeException ex){
return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
		 }


}