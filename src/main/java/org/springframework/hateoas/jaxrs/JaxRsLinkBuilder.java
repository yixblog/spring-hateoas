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

import static org.springframework.hateoas.core.DummyInvocationUtils.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Path;

import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkBuilder;
import org.springframework.hateoas.TemplateVariables;
import org.springframework.hateoas.core.AffordanceModelFactory;
import org.springframework.hateoas.core.AnnotationMappingDiscoverer;
import org.springframework.hateoas.core.DummyInvocationUtils;
import org.springframework.hateoas.core.LinkBuilderSupport;
import org.springframework.hateoas.core.MappingDiscoverer;
import org.springframework.http.MediaType;
import org.springframework.plugin.core.OrderAwarePluginRegistry;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.util.Assert;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.DefaultUriTemplateHandler;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * {@link LinkBuilder} to derive URI mappings from a JAX-RS {@link Path} annotation.
 * 
 * @author Oliver Gierke
 * @author Kamill Sokol
 * @author Andrew Naydyonock
 */
public class JaxRsLinkBuilder extends LinkBuilderSupport<JaxRsLinkBuilder> {

	private static final MappingDiscoverer DISCOVERER = new AnnotationMappingDiscoverer(Path.class);
	private static final CustomUriTemplateHandler HANDLER = new CustomUriTemplateHandler();
	private static final JaxRsLinkBuilderFactory FACTORY = new JaxRsLinkBuilderFactory();
	private static final JaxRsAffordanceBuilder AFFORDANCE_BUILDER;


	private final TemplateVariables variables;

	static {

		List<AffordanceModelFactory> factories = SpringFactoriesLoader.loadFactories(AffordanceModelFactory.class,
			JaxRsLinkBuilder.class.getClassLoader());

		PluginRegistry<? extends AffordanceModelFactory, MediaType> MODEL_FACTORIES = OrderAwarePluginRegistry
			.create(factories);
		AFFORDANCE_BUILDER = new JaxRsAffordanceBuilder(MODEL_FACTORIES);
	}


	/**
	 * Creates a new {@link JaxRsLinkBuilder} from the given {@link UriComponentsBuilder}.
	 * 
	 * @param builder must not be {@literal null}.
	 */
	private JaxRsLinkBuilder(UriComponentsBuilder builder) {
		this(builder.build(), TemplateVariables.NONE, null);
	}

	JaxRsLinkBuilder(UriComponents uriComponents, TemplateVariables variables, MethodInvocation invocation) {

		super(uriComponents);

		this.variables = variables;
		this.addAffordances(findAffordances(invocation, uriComponents));
	}


	/**
	 * Creates a {@link JaxRsLinkBuilder} instance to link to the {@link Path} mapping tied to the given class.
	 * 
	 * @param service the class to discover the annotation on, must not be {@literal null}.
	 * @return
	 */
	public static JaxRsLinkBuilder linkTo(Class<?> service) {
		return linkTo(service, new Object[0]);
	}

	/**
	 * Creates a new {@link JaxRsLinkBuilder} instance to link to the {@link Path} mapping tied to the given class binding
	 * the given parameters to the URI template.
	 * 
	 * @param resourceType the class to discover the annotation on, must not be {@literal null}.
	 * @param parameters additional parameters to bind to the URI template declared in the annotation, must not be
	 *          {@literal null}.
	 * @return
	 */
	public static JaxRsLinkBuilder linkTo(Class<?> resourceType, Object... parameters) {

		Assert.notNull(resourceType, "Controller type must not be null!");
		Assert.notNull(parameters, "Parameters must not be null!");

		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(DISCOVERER.getMapping(resourceType));
		UriComponents expandedComponents = HANDLER.expandAndEncode(builder, parameters);

		return new JaxRsLinkBuilder(ServletUriComponentsBuilder.fromCurrentServletMapping())//
				.slash(expandedComponents, true);
	}

	public static JaxRsLinkBuilder linkTo(Object invocationValue) {
		return FACTORY.linkTo(invocationValue);
	}

	/**
	 * Creates a new {@link JaxRsLinkBuilder} instance to link to the {@link Path} mapping tied to the given class binding
	 * the given parameters to the URI template.
	 *
	 * @param resourceType the class to discover the annotation on, must not be {@literal null}.
	 * @param parameters map of additional parameters to bind to the URI template declared in the annotation, must not be
	 *          {@literal null}.
	 * @return
	 */
	public static JaxRsLinkBuilder linkTo(Class<?> resourceType, Map<String, ?> parameters) {

		Assert.notNull(resourceType, "Controller type must not be null!");
		Assert.notNull(parameters, "Parameters must not be null!");

		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(DISCOVERER.getMapping(resourceType));
		UriComponents expandedComponents = HANDLER.expandAndEncode(builder, parameters);

		return new JaxRsLinkBuilder(ServletUriComponentsBuilder.fromCurrentServletMapping())//
				.slash(expandedComponents, true);
	}

	/**
	 * Extract a {@link Link} from the {@link JaxRsLinkBuilder} and look up the related {@link Affordance}. Should
	 * only be one.
	 *
	 * <pre>
	 * Link findOneLink = linkTo(methodOn(EmployeeController.class).findOne(id)).withSelfRel()
	 * 		.andAffordance(afford(methodOn(EmployeeController.class).updateEmployee(null, id)));
	 * </pre>
	 *
	 * This takes a link and adds an {@link Affordance} based on another Spring MVC handler method.
	 *
	 * @param invocationValue
	 * @return
	 */
	public static Affordance afford(Object invocationValue) {

		JaxRsLinkBuilder linkBuilder = linkTo(invocationValue);

		Assert.isTrue(linkBuilder.getAffordances().size() == 1, "A base can only have one affordance, itself");

		return linkBuilder.getAffordances().get(0);
	}

	/**
	 * Wrapper for {@link DummyInvocationUtils#methodOn(Class, Object...)} to be available in case you work with static
	 * imports of {@link JaxRsLinkBuilder}.
	 *
	 * @param controller must not be {@literal null}.
	 * @param parameters parameters to extend template variables in the type level mapping.
	 * @return
	 */
	public static <T> T methodOn(Class<T> controller, Object... parameters) {
		return DummyInvocationUtils.methodOn(controller, parameters);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.UriComponentsLinkBuilder#getThis()
	 */
	@Override
	protected JaxRsLinkBuilder getThis() {
		return this;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.UriComponentsLinkBuilder#createNewInstance(org.springframework.web.util.UriComponentsBuilder)
	 */
	@Override
	protected JaxRsLinkBuilder createNewInstance(UriComponentsBuilder builder) {
		return new JaxRsLinkBuilder(builder);
	}

	/**
	 * Look up {@link Affordance}s and {@link org.springframework.hateoas.AffordanceModel}s based on the
	 * {@link MethodInvocation} and {@link UriComponents}.
	 *
	 * @param invocation
	 * @param components
	 * @return
	 */
	private static Collection<Affordance> findAffordances(MethodInvocation invocation, UriComponents components) {
		return AFFORDANCE_BUILDER.create(invocation, DISCOVERER, components);
	}

	private static class CustomUriTemplateHandler extends DefaultUriTemplateHandler {

		public CustomUriTemplateHandler() {
			setStrictEncoding(true);
		}

		/* 
		 * (non-Javadoc)
		 * @see org.springframework.web.util.DefaultUriTemplateHandler#expandAndEncode(org.springframework.web.util.UriComponentsBuilder, java.util.Map)
		 */
		@Override
		public UriComponents expandAndEncode(UriComponentsBuilder builder, Map<String, ?> uriVariables) {
			return super.expandAndEncode(builder, uriVariables);
		}

		/* 
		 * (non-Javadoc)
		 * @see org.springframework.web.util.DefaultUriTemplateHandler#expandAndEncode(org.springframework.web.util.UriComponentsBuilder, java.lang.Object[])
		 */
		@Override
		public UriComponents expandAndEncode(UriComponentsBuilder builder, Object[] uriVariables) {
			return super.expandAndEncode(builder, uriVariables);
		}
	}
}
