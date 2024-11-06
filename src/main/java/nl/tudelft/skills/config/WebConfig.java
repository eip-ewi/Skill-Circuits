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
import java.util.Locale;
import java.util.Objects;

import lombok.AllArgsConstructor;
import nl.tudelft.labracore.lib.security.LabradorUserDetails;
import nl.tudelft.labracore.lib.security.user.AuthenticatedPerson;
import nl.tudelft.labracore.lib.security.user.NoAuthenticatedPersonException;
import nl.tudelft.skills.annotation.AuthenticatedSCPerson;
import nl.tudelft.skills.model.SCPerson;
import nl.tudelft.skills.repository.labracore.PersonRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

@Configuration
@AllArgsConstructor
public class WebConfig implements WebMvcConfigurer {

	private final PersonRepository personRepository;

	@Bean
	public LocaleResolver localeResolver() {
		CookieLocaleResolver resolver = new CookieLocaleResolver();
		resolver.setDefaultLocale(Locale.getDefault());
		return resolver;
	}

	@Bean
	public LocaleChangeInterceptor localeChangeInterceptor() {
		LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
		lci.setParamName("lang");
		return lci;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(localeChangeInterceptor());
	}

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(this.authenticatedSCPersonArgumentResolver());
	}

	@Bean
	public AuthenticatedSCPersonArgumentResolver authenticatedSCPersonArgumentResolver() {
		return new AuthenticatedSCPersonArgumentResolver(personRepository);
	}

	public static class AuthenticatedSCPersonArgumentResolver implements HandlerMethodArgumentResolver {

		private final PersonRepository personRepository;

		public AuthenticatedSCPersonArgumentResolver(PersonRepository personRepository) {
			this.personRepository = personRepository;
		}

		public boolean supportsParameter(MethodParameter parameter) {
			return parameter.hasParameterAnnotation(AuthenticatedSCPerson.class) && SCPerson.class.isAssignableFrom(parameter.getParameterType());
		}

		public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			Object ud = auth == null ? null : auth.getPrincipal();
			if (ud instanceof LabradorUserDetails) {
				Long personId = ((LabradorUserDetails)ud).getUser().getId();
				return personRepository.findById(personId).orElseGet(() -> personRepository.save(SCPerson.builder().id(personId).build()));
			} else if (((AuthenticatedPerson) Objects.requireNonNull((AuthenticatedPerson)parameter.getParameterAnnotation(AuthenticatedPerson.class))).required()) {
				throw new NoAuthenticatedPersonException("No authenticated person found for request.");
			} else {
				return null;
			}
		}
	}

}
