package com.example.rqchallenge.employees.helper;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient.Builder;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;

import com.example.rqchallenge.exception.TooManyRequestsException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;

@Component
public class EmployeeHelper {
	public Mono<ResponseEntity<String>> handleRequest(Builder webClient, Path path, String... id) {
		return retreive(webClient, path, id)
				.get()
				.onStatus(
			                httpStatus -> httpStatus.value() == 429,
			                clientResponse -> Mono.error(new TooManyRequestsException(HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase()))
						)
				.toEntity(String.class);
	}

	private Optional<ResponseSpec> retreive(Builder webClient, Path path, String... id) {
	    switch(path) {
	      case GET_ALL:
	  		return Optional.ofNullable(webClient.build().get()
			        .uri(uriBuilder -> uriBuilder.path("/api/v1/employees").build()).retrieve());
	      case GET:
	  		return Optional.ofNullable(webClient.build().get()
			        .uri(uriBuilder -> uriBuilder.path("/api/v1/employee/").path(id.toString()).build()).retrieve());
	      case CREATE:
	  		return Optional.ofNullable(webClient.build().get()
			        .uri(uriBuilder -> uriBuilder.path("/api/v1/create").build()).retrieve());
	      case DELETE:
	  		return Optional.ofNullable(webClient.build().get()
			        .uri(uriBuilder -> uriBuilder.path("/api/v1/delete").build()).retrieve());      
	    }
		throw new RuntimeException("Invalid Request");
	}

	public JsonNode parse(ResponseEntity<String> apiResponse, NodeType nodeType, ObjectMapper mapper) {
        JsonNode apiResponseNode = null;
		try {
			apiResponseNode = mapper.readTree(apiResponse.getBody());
		} catch (JsonMappingException e) {
			throw new RuntimeException("Error parsing json: ", e);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Error parsing json: ", e);
		}
        return apiResponseNode.at("/" + nodeType.toString());
	}
	
}