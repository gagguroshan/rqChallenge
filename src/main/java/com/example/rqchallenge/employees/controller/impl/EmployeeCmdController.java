package com.example.rqchallenge.employees.controller.impl;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.rqchallenge.employees.controller.IEmployeeCmdController;
import com.example.rqchallenge.employees.service.IEmployeeService;

@RestController
@RequestMapping("employees")
public class EmployeeCmdController implements IEmployeeCmdController {

	private IEmployeeService employeeService;

	public EmployeeCmdController(IEmployeeService employeeService) {
		this.employeeService = employeeService;
	}

	@Override
	public ResponseEntity<String> createEmployee(Map<String, Object> employeeInput) {
		return ResponseEntity.ok(employeeService.createEmployee(employeeInput));
	}

	@Override
	public ResponseEntity<String> deleteEmployeeById(String id) {
		return ResponseEntity.ok(employeeService.deleteEmployee(id));
	}
}
