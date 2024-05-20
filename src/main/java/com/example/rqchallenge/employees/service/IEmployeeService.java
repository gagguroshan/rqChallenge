package com.example.rqchallenge.employees.service;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.rqchallenge.employees.dto.Employee;

@Service
public interface IEmployeeService {

    List<Employee> getAllEmployees();

    List<Employee> getEmployeesByNameSearch(String searchString) throws IOException;

    Optional<Employee> getEmployeeById(String id);

    Integer getHighestSalaryOfEmployees() throws IOException;

    List<String> getTopTenHighestEarningEmployeeNames() throws IOException;

    String createEmployee(Map<String, Object> employeeInput);

    String deleteEmployee(String id);	
}
