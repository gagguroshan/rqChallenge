package com.example.rqchallenge;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Map;
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

import com.example.rqchallenge.employees.controller.IEmployeeCmdController;
import com.example.rqchallenge.employees.controller.IEmployeeQryController;
import com.example.rqchallenge.employees.dto.Employee;
import com.example.rqchallenge.employees.service.IEmployeeService;
import com.example.rqchallenge.exception.handler.RestResponseEntityExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = IEmployeeCmdController.class)
public class EmployeeCmdControllerTest {

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

	private Optional<Employee> getEmployee() {
		Employee e = new Employee();
		e.setId(1l);
		e.setEmployeeName("Narendra Sharma");
		e.setEmployeeSalary(100000000);
		e.setEmployeeAge(42);
		return Optional.ofNullable(e);
	}

	@Test
	void whenCreateEmployee_Success() throws Exception {
		Map<String, Object> input = new HashMap<>();
		input.put("name", "Narendra Sharma");
		input.put("age", 42);
		input.put("salary", 100000000);

		mockMvc.perform(post("/employees").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(input))).andDo(print()).andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.content().string("success"));
	}

	@Test
	void whenDeleteEmployee_thenReturnSuccess() throws Exception {
		String id = "1";
		given(employeeService.deleteEmployee(id)).willReturn("success");

		mockMvc.perform(delete("/employees/{id}", id)).andDo(print()).andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.content().string("success"));
	}
}
