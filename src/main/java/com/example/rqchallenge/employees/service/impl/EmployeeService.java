package com.example.rqchallenge.employees.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.rqchallenge.employees.dto.Employee;
import com.example.rqchallenge.employees.helper.EmployeeHelper;
import com.example.rqchallenge.employees.helper.NodeType;
import com.example.rqchallenge.employees.helper.Path;
import com.example.rqchallenge.employees.service.IEmployeeService;
import com.example.rqchallenge.exception.TooManyRequestsException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class EmployeeService implements IEmployeeService {

	Logger log = LoggerFactory.getLogger(Employee.class);

	private WebClient.Builder webClient;

	private EmployeeHelper employeeHelper;

	public EmployeeService(WebClient.Builder webClient, EmployeeHelper employeeHelper) {
		this.webClient = webClient;
		this.employeeHelper = employeeHelper;
	}

	@Override
	public List<Employee> getAllEmployees() {
        List<Employee> employees = employeeHelper
        		.handleRequest(webClient, Path.GET_ALL)
        		.map(apiResponse -> {
		            List<Employee> employeeList = null;
		        	ObjectMapper mapper = new ObjectMapper();
					try {
						employeeList = mapper
								.readerForListOf(Employee.class)
								.readValue(employeeHelper
										.parse(apiResponse, NodeType.data, mapper));
					} catch (IOException e) {
						log.error(e.getMessage());
						throw new RuntimeException("Error parsing json: ", e);
					}
		            return employeeList;
		        })
		        .block();

        		return employees;
	}

	@Override
	public List<Employee> getEmployeesByNameSearch(String searchString) throws IOException{
        return getAllEmployees().stream()
	      .filter(employee -> employee.getEmployeeName().contains(searchString))
	      .toList();  
	}

	@Override
	public Optional<Employee> getEmployeeById(String id) {
        Optional<Employee> employee = Optional.of(webClient.build().get()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/employee/"+id).build())
                .retrieve()
                .onStatus(
                        httpStatus -> httpStatus.value() == 429,
                        clientResponse -> Mono.error(new TooManyRequestsException(HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase()))
                )
                .toEntity(String.class)
                .map(apiResponse -> {
                    Employee employeeResp = null;
                	ObjectMapper mapper = new ObjectMapper();
                    JsonNode apiResponseNode = null;
					try {
						apiResponseNode = mapper.readTree(apiResponse.getBody());
					} catch (JsonProcessingException e) {
						log.error(e.getMessage());
					}
                    JsonNode dataNode = apiResponseNode.at("/data");
					try {
						employeeResp = mapper.reader(Employee.class).readValue(dataNode);
					} catch (IOException e) {
						log.error(e.getMessage());
					}
                    return employeeResp;
                })
                .block());
        return employee;
	}

	@Override
	public Integer getHighestSalaryOfEmployees() throws IOException {
        return getAllEmployees().stream()
        		.collect(Collectors.maxBy(
        				Comparator.comparingDouble(Employee::getEmployeeSalary)))
        		.get().getEmployeeSalary();
	}

	@Override
	public List<String> getTopTenHighestEarningEmployeeNames() throws IOException {
		List<Employee> topTenHighestSalariedEmployees = getAllEmployees().stream()
				.sorted(new Comparator<Employee>() {
					@Override
					public int compare(Employee e1, Employee e2) {
						return e2.getEmployeeSalary() - e1.getEmployeeSalary();
					}
				})
        		.limit(10)
        		.toList();
		List<String> employeeNames = new ArrayList<String>();
		topTenHighestSalariedEmployees.stream().forEach(employee -> employeeNames.add(employee.getEmployeeName()));
		return employeeNames;
	}

    @Override
    public String createEmployee(Map<String, Object> employeeInput) {
    	return webClient.build().post()
        .uri(uriBuilder -> uriBuilder.path("/api/v1/create").build())
        .bodyValue(employeeInput)
        .retrieve()
        .onStatus(
                httpStatus -> httpStatus.value() == 429,
                clientResponse -> Mono.error(new TooManyRequestsException(HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase()))
        )
        .toEntity(String.class)
        .map(apiResponse -> {
            ObjectMapper mapper = new ObjectMapper();
            String status = null;
			try {
				status = mapper.readTree(apiResponse.getBody()).at("/status").asText();
			} catch (JsonProcessingException e) {
				log.error(e.getMessage());
			}
            return status;
        })
        .block();
    }

	@Override
	public String deleteEmployee(String id) {
        Optional<Employee> employeeResp = getEmployeeById(id);
    	return webClient.build().delete()
    			.uri(uriBuilder -> uriBuilder.path("/api/v1/delete/" + id).build())
                .retrieve()
                .onStatus(
                        httpStatus -> httpStatus.value() == 429,
                        clientResponse -> Mono.error(new TooManyRequestsException(HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase()))
                )
                .toEntity(String.class)
                .map(apiResponse -> {
                	ObjectMapper mapper = new ObjectMapper();
                    String status = null;
					try {
						status = mapper.readTree(apiResponse.getBody()).at("/status").asText();
					} catch (IOException e) {
						log.error(e.getMessage());
					}
					if("success".equalsIgnoreCase(status)) {
						return employeeResp.get().getEmployeeName();
					}
					return "failed";
                })
                .block();
	}
}