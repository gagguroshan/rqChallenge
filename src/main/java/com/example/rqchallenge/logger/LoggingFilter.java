package com.example.rqchallenge.logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import com.example.rqchallenge.employees.dto.Employee;

@Component
public class LoggingFilter extends OncePerRequestFilter {

	private static final List<MediaType> VISIBLE_TYPES = Arrays.asList(MediaType.valueOf("text/*"),
			MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML,
			MediaType.valueOf("application/*+json"), MediaType.valueOf("application/*+xml"),
			MediaType.MULTIPART_FORM_DATA);
	Logger log = LoggerFactory.getLogger(Employee.class);
	private static final Path path = Paths.get("employee.log");
	private static BufferedWriter writer = null;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		try {
			writer = Files.newBufferedWriter(path, Charset.forName("UTF-8"));
			if (isAsyncDispatch(request)) {
				filterChain.doFilter(request, response);
			} else {
				doFilterWrapped(wrapRequest(request), wrapResponse(response), filterChain);
			}
		} finally {
			writer.close();
		}
	}

	protected void doFilterWrapped(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response,
			FilterChain filterChain) throws ServletException, IOException {
		try {
			beforeRequest(request, response);
			filterChain.doFilter(request, response);
		} finally {
			afterRequest(request, response);
			response.copyBodyToResponse();
		}
	}

	protected void beforeRequest(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response)
			throws IOException {
		if (log.isInfoEnabled()) {
			logRequestHeader(request, request.getRemoteAddr() + "|>");
		}
	}

	protected void afterRequest(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response)
			throws IOException {
		if (log.isInfoEnabled()) {
			logRequestBody(request, request.getRemoteAddr() + "|>");
			logResponse(response, request.getRemoteAddr() + "|<");
		}
	}

	private void logRequestHeader(ContentCachingRequestWrapper request, String prefix) throws IOException {
		String queryString = request.getQueryString();
		if (queryString == null) {
			printLines(prefix, request.getMethod(), request.getRequestURI());
			log.info("{} {} {}", prefix, request.getMethod(), request.getRequestURI());
		} else {
			printLines(prefix, request.getMethod(), request.getRequestURI(), queryString);
			log.info("{} {} {}?{}", prefix, request.getMethod(), request.getRequestURI(), queryString);
		}
		Collections.list(request.getHeaderNames())
				.forEach(headerName -> Collections.list(request.getHeaders(headerName))
						.forEach(headerValue -> log.info("{} {}: {}", prefix, headerName, headerValue)));
		printLines(prefix);
		printLines(RequestContextHolder.currentRequestAttributes().getSessionId());
		log.info("{}", prefix);

		log.info(" Session ID: ", RequestContextHolder.currentRequestAttributes().getSessionId());
	}

	private void printLines(String... args) throws IOException {

		try {
			for (String varArgs : args) {
				writer.write(varArgs);
				writer.newLine();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}

	}

	private void logRequestBody(ContentCachingRequestWrapper request, String prefix) {
		byte[] content = request.getContentAsByteArray();
		if (content.length > 0) {
			logContent(content, request.getContentType(), request.getCharacterEncoding(), prefix);
		}
	}

	private void logResponse(ContentCachingResponseWrapper response, String prefix) throws IOException {
		int status = response.getStatus();
		printLines(prefix, String.valueOf(status), HttpStatus.valueOf(status).getReasonPhrase());
		log.info("{} {} {}", prefix, status, HttpStatus.valueOf(status).getReasonPhrase());
		response.getHeaderNames().forEach(headerName -> response.getHeaders(headerName)
				.forEach(headerValue -> log.info("{} {}: {}", prefix, headerName, headerValue)));
		printLines(prefix);
		log.info("{}", prefix);
		byte[] content = response.getContentAsByteArray();
		if (content.length > 0) {
			logContent(content, response.getContentType(), response.getCharacterEncoding(), prefix);
		}
	}

	private void logContent(byte[] content, String contentType, String contentEncoding, String prefix) {
		MediaType mediaType = MediaType.valueOf(contentType);
		boolean visible = VISIBLE_TYPES.stream().anyMatch(visibleType -> visibleType.includes(mediaType));
		if (visible) {
			try {
				String contentString = new String(content, contentEncoding);
				Stream.of(contentString.split("\r\n|\r|\n")).forEach(line -> {
					try {
						printLines(line);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});
			} catch (UnsupportedEncodingException e) {
				log.info("{} [{} bytes content]", prefix, content.length);
			}
		} else {

			log.info("{} [{} bytes content]", prefix, content.length);
		}
	}

	private static ContentCachingRequestWrapper wrapRequest(HttpServletRequest request) {
		if (request instanceof ContentCachingRequestWrapper) {
			return (ContentCachingRequestWrapper) request;
		} else {
			return new ContentCachingRequestWrapper(request);
		}
	}

	private static ContentCachingResponseWrapper wrapResponse(HttpServletResponse response) {
		if (response instanceof ContentCachingResponseWrapper) {
			return (ContentCachingResponseWrapper) response;
		} else {
			return new ContentCachingResponseWrapper(response);
		}
	}
}