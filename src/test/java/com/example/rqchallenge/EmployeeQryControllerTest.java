package com.example.rqchallenge;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.rqchallenge.employees.controller.IEmployeeQryController;
import com.example.rqchallenge.employees.dto.Employee;
import com.example.rqchallenge.employees.service.IEmployeeService;
import com.example.rqchallenge.exception.handler.RestResponseEntityExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = IEmployeeQryController.class)
public class EmployeeQryControllerTest {

	@Autowired
	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@MockBean
	private IEmployeeService employeeService;

	@Before
	public void setup() {
		this.mockMvc = MockMvcBuilders.standaloneSetup(IEmployeeQryController.class)
				.setControllerAdvice(new RestResponseEntityExceptionHandler()).build();
	}

	@Test
	void whenGetAllEmployees_thenReturnJsonArray() throws Exception {
		List<Employee> allEmployees = List.of(getEmployee().get());

		given(employeeService.getAllEmployees()).willReturn(allEmployees);

		mockMvc.perform(get("/employees").contentType(MediaType.APPLICATION_JSON)).andDo(print())
				.andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].employee_name", is("Narendra Sharma")))
				.andExpect(jsonPath("$[0].employee_salary", is(100000000)))
				.andExpect(jsonPath("$[0].employee_age", is(42)));
		;
	}

	@Test
	void whenGetEmployeeById_thenReturnEmployee() throws Exception {
		given(employeeService.getEmployeeById("1")).willReturn(getEmployee());

		mockMvc.perform(get("/employees/{id}", 1).contentType(MediaType.APPLICATION_JSON)).andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.employee_name", is(getEmployee().get().getEmployeeName())))
				.andExpect(jsonPath("$.employee_salary", is(getEmployee().get().getEmployeeSalary())))
				.andExpect(jsonPath("$.employee_age", is(getEmployee().get().getEmployeeAge())));
		;
	}

	@Test
	void whenGetEmployeeByNameSearch_thenReturnEmployeeList() throws Exception {

		String searchString = "Nar";

		given(employeeService.getEmployeesByNameSearch(searchString)).willReturn(List.of(getEmployee().get()));

		mockMvc.perform(get("/employees/search/{searchString}", searchString).contentType(MediaType.APPLICATION_JSON))
				.andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].employee_name", is(getEmployee().get().getEmployeeName())))
				.andExpect(jsonPath("$[0].employee_salary", is(getEmployee().get().getEmployeeSalary())))
				.andExpect(jsonPath("$[0].employee_age", is(getEmployee().get().getEmployeeAge())));
	}

	private Optional<Employee> getEmployee() {
		Employee e = new Employee();
		e.setId(1l);
		e.setEmployeeName("Narendra Sharma");
		e.setEmployeeSalary(100000000);
		e.setEmployeeAge(42);
		return Optional.ofNullable(e);
	}

	@Test
	void whenGetEmployeeByNameSearch_IfNotPresent_thenReturnEmptyEmployeeList() throws Exception {

		String searchString = "nar";

		given(employeeService.getEmployeesByNameSearch(searchString)).willReturn(new ArrayList<>());

		mockMvc.perform(get("/employees/search/{searchString}", searchString).contentType(MediaType.APPLICATION_JSON))
				.andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
	}

	@Test
	void whenGetHighestSalaryOfEmployee_thenReturnHighestSalary() throws Exception {

		given(employeeService.getHighestSalaryOfEmployees()).willReturn(getEmployee().get().getEmployeeSalary());

		mockMvc.perform(get("/employees/highestSalary")).andDo(print()).andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.content().string("100000000"));
	}

	@Test
	void whenGetTopTenHighestEarningEmployeeNames_thenReturnListOfEmployeeName() throws Exception {

		List<String> empList = new ArrayList<>();
		for (int i = 1; i <= 10; i++) {
			empList.add(getEmployee().get().getEmployeeName());
		}
		given(employeeService.getTopTenHighestEarningEmployeeNames()).willReturn(empList);

		mockMvc.perform(get("/employees/topTenHighestEarningEmployeeNames").contentType(MediaType.APPLICATION_JSON))
				.andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(10)))
				.andExpect(jsonPath("$.[0]", is("Narendra Sharma")));
	}
}