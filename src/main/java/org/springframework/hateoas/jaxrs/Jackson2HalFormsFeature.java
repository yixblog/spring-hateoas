/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.hateoas.jaxrs;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.hateoas.RelProvider;
import org.springframework.hateoas.core.EvoInflectorRelProvider;
import org.springframework.hateoas.hal.forms.HalFormsConfiguration;
import org.springframework.hateoas.hal.forms.Jackson2HalFormsModule;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

/**
 * JAX-RS {@link Feature} used to register a customized {@link ObjectMapper} with {@link Jackson2HalFormsModule}.
 *
 * Depending on the setup, you either need the provider directly via {@link #getProvider()} or you register this
 * class and it gets triggered by {@link Feature}'s callback.
 *
 * @author Greg Turnquist
 */
public class Jackson2HalFormsFeature implements Feature {

	static final ObjectMapper mapper;

	static {
		mapper = new ObjectMapper();
		mapper.registerModule(new Jackson2HalFormsModule());
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

		RelProvider relProvider = new EvoInflectorRelProvider();

		ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
		messageSource.setBasename("classpath:rest-messages");
		MessageSourceAccessor linkRelationMessageSource = new MessageSourceAccessor(messageSource);

		mapper.setHandlerInstantiator(new Jackson2HalFormsModule.HalFormsHandlerInstantiator(relProvider, null,
			linkRelationMessageSource, true, new HalFormsConfiguration()));
	}

	/**
	 * To get the {@link JacksonJaxbJsonProvider} provider itself, with HAL-FORMS support
	 *
	 * @return
	 */
	public static JacksonJsonProvider getProvider() {

		JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider(
			mapper, JacksonJaxbJsonProvider.DEFAULT_ANNOTATIONS);

		return provider;
	}

	/**
	 * To register via a callback (works for Grizzley containers).
	 *
	 * @param context
	 * @return
	 */
	@Override
	public boolean configure(FeatureContext context) {
		
		context.register(getProvider());

		return true;
	}
}
