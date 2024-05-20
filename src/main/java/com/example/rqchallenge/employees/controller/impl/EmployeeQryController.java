package com.example.rqchallenge.employees.controller.impl;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.rqchallenge.employees.controller.IEmployeeQryController;
import com.example.rqchallenge.employees.dto.Employee;
import com.example.rqchallenge.employees.service.IEmployeeService;
import com.example.rqchallenge.exception.TooManyRequestsException;

@RestController
@RequestMapping("employees")
public class EmployeeQryController implements IEmployeeQryController {

	private IEmployeeService employeeService;

	public EmployeeQryController(IEmployeeService employeeService) {
		this.employeeService = employeeService;
	}

	@Override
	public ResponseEntity<List<Employee>> getAllEmployees() {
		return ResponseEntity.ok(employeeService.getAllEmployees());
	}

	@Override
	public ResponseEntity<List<Employee>> getEmployeesByNameSearch(String searchString) throws IOException {
		return ResponseEntity.ok(employeeService.getEmployeesByNameSearch(searchString));
	}

	@Override
	public ResponseEntity<Employee> getEmployeeById(String id) {
		return ResponseEntity.ok(employeeService.getEmployeeById(id).orElseThrow());
	}

	@Override
	public ResponseEntity<Integer> getHighestSalaryOfEmployees() throws IOException {
		return ResponseEntity.ok(employeeService.getHighestSalaryOfEmployees());
	}

	@Override
	public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() throws IOException {
		return ResponseEntity.ok(employeeService.getTopTenHighestEarningEmployeeNames());
	}
}
