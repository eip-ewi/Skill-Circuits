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
package nl.tudelft.skills.config;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ClientRequest;

import io.opentracing.Span;
import io.opentracing.contrib.spring.web.client.WebClientSpanDecorator;
import io.opentracing.contrib.web.servlet.filter.ServletFilterSpanDecorator;

@Configuration
public class JaegerConfig {
	@Bean
	public WebClientSpanDecorator customWebClientSpanDecorator() {
		return new WebClientSpanDecorator.StandardTags() {
			@Override
			public void onRequest(ClientRequest clientRequest, Span span) {
				super.onRequest(clientRequest, span);

				span.setOperationName(
						"OUT: " + clientRequest.method().name() + " " + clientRequest.url().getPath());
			}
		};
	}

	@Bean
	public List<ServletFilterSpanDecorator> customServletFilterSpanDecorator() {
		return List.of(ServletFilterSpanDecorator.STANDARD_TAGS, new ServletFilterSpanDecorator() {
			@Override
			public void onRequest(HttpServletRequest httpServletRequest, Span span) {
				span.setOperationName(
						httpServletRequest.getMethod() + " " + httpServletRequest.getServletPath());
			}

			@Override
			public void onResponse(HttpServletRequest httpServletRequest,
					HttpServletResponse httpServletResponse, Span span) {
			}

			@Override
			public void onError(HttpServletRequest httpServletRequest,
					HttpServletResponse httpServletResponse, Throwable exception, Span span) {
			}

			@Override
			public void onTimeout(HttpServletRequest httpServletRequest,
					HttpServletResponse httpServletResponse, long timeout, Span span) {
			}
		});
	}
}
