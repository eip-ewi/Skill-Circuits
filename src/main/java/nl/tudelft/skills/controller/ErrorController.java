/*
 * Skill Circuits
 * Copyright (C) 2025 - Delft University of Technology
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package nl.tudelft.skills.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ControllerAdvice
public class ErrorController {

	private static final Logger logger = LoggerFactory.getLogger(ErrorController.class);

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<Void> handle404(ResourceNotFoundException e) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
	}

	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<Void> handleResponseStatusException(ResponseStatusException e) {
		return ResponseEntity.status(e.getStatusCode()).build();
	}

	@ExceptionHandler(Exception.class)
	public String defaultErrorHandler(HttpServletRequest request, Exception e, HttpServletResponse response)
			throws Exception {
		logger.error("A Request ({}) raised an exception", request.getRequestURI(), e);

		ResponseStatus statusAnnotation = AnnotationUtils.findAnnotation(e.getClass(), ResponseStatus.class);
		if (statusAnnotation != null) {
			response.setStatus(statusAnnotation.code().value());
		}

		return "index";
	}

	@GetMapping("/error")
	public String errorPage() {
		return "index";
	}

}
