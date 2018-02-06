/*
 * Copyright 2012-2016 the original author or authors.
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

import static org.springframework.hateoas.TemplateVariables.*;
import static org.springframework.hateoas.core.DummyInvocationUtils.*;
import static org.springframework.hateoas.core.EncodingUtils.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.springframework.core.MethodParameter;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkBuilder;
import org.springframework.hateoas.LinkBuilderFactory;
import org.springframework.hateoas.TemplateVariables;
import org.springframework.hateoas.core.AnnotationAttribute;
import org.springframework.hateoas.core.AnnotationMappingDiscoverer;
import org.springframework.hateoas.core.MappingDiscoverer;
import org.springframework.hateoas.core.MethodParameters;
import org.springframework.hateoas.mvc.UriComponentsContributor;
import org.springframework.hateoas.AnnotatedParametersParameterAccessor;
import org.springframework.hateoas.AnnotatedParametersParameterAccessor.BoundMethodParameter;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;

/**
 * Factory for {@link LinkBuilder} instances based on the path mapping annotated on the given JAX-RS service.
 * 
 * @author Ricardo Gladwell
 * @author Oliver Gierke
 * @author Andrew Naydyonock
 */
public class JaxRsLinkBuilderFactory implements LinkBuilderFactory<JaxRsLinkBuilder> {

	private static final MappingDiscoverer DISCOVERER = new AnnotationMappingDiscoverer(Path.class);
	private static final AnnotatedParametersParameterAccessor PATH_VARIABLE_ACCESSOR = new AnnotatedParametersParameterAccessor(
		new AnnotationAttribute(PathParam.class));


	private List<UriComponentsContributor> uriComponentsContributors = new ArrayList<>();

	/**
	 * Configures the {@link UriComponentsContributor} to be used when building {@link Link} instances from method
	 * invocations.
	 *
	 * @see #linkTo(Object)
	 * @param uriComponentsContributors the uriComponentsContributors to set
	 */
	public void setUriComponentsContributors(List<? extends UriComponentsContributor> uriComponentsContributors) {
		this.uriComponentsContributors = Collections.unmodifiableList(uriComponentsContributors);
	}


	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.LinkBuilderFactory#linkTo(java.lang.Class)
	 */
	public JaxRsLinkBuilder linkTo(Class<?> service) {
		return JaxRsLinkBuilder.linkTo(service);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.LinkBuilderFactory#linkTo(java.lang.Class, java.lang.Object[])
	 */
	@Override
	public JaxRsLinkBuilder linkTo(Class<?> service, Object... parameters) {
		return JaxRsLinkBuilder.linkTo(service, parameters);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.LinkBuilderFactory#linkTo(java.lang.Class, java.util.Map)
	 */
	@Override
	public JaxRsLinkBuilder linkTo(Class<?> service, Map<String, ?> parameters) {
		return JaxRsLinkBuilder.linkTo(service, parameters);
	}

	public JaxRsLinkBuilder linkTo(Object invocationValue) {

		Assert.isInstanceOf(LastInvocationAware.class, invocationValue);
		LastInvocationAware invocations = (LastInvocationAware) invocationValue;

		MethodInvocation invocation = invocations.getLastInvocation();
		Iterator<Object> classMappingParameters = invocations.getObjectParameters();
		Method method = invocation.getMethod();

		String mapping = DISCOVERER.getMapping(invocation.getTargetType(), method);

		UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/").path(mapping);

		UriTemplate template = new UriTemplate(mapping);
		Map<String, Object> values = new HashMap<>();
		Iterator<String> names = template.getVariableNames().iterator();

		while (classMappingParameters.hasNext()) {
			values.put(names.next(), encodePath(classMappingParameters.next()));
		}

		for (BoundMethodParameter parameter : PATH_VARIABLE_ACCESSOR.getBoundParameters(invocation)) {
			values.put(parameter.getVariableName(), encodePath(parameter.asString()));
		}

		UriComponents components = applyUriComponentsContributer(builder, invocation).buildAndExpand(values);
		TemplateVariables variables = NONE;

		return new JaxRsLinkBuilder(components, variables, invocation);
	}

	/**
	 * Applies the configured {@link UriComponentsContributor}s to the given {@link UriComponentsBuilder}.
	 *
	 * @param builder will never be {@literal null}.
	 * @param invocation will never be {@literal null}.
	 * @return
	 */
	protected UriComponentsBuilder applyUriComponentsContributer(UriComponentsBuilder builder,
																 MethodInvocation invocation) {

		MethodParameters parameters = new MethodParameters(invocation.getMethod());
		Iterator<Object> parameterValues = Arrays.asList(invocation.getArguments()).iterator();

		for (MethodParameter parameter : parameters.getParameters()) {
			Object parameterValue = parameterValues.next();
			for (UriComponentsContributor contributor : uriComponentsContributors) {
				if (contributor.supportsParameter(parameter)) {
					contributor.enhance(builder, parameter, parameterValue);
				}
			}
		}

		return builder;
	}

}
