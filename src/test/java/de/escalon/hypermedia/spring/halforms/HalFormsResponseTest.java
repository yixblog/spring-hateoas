package de.escalon.hypermedia.spring.halforms;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static de.escalon.hypermedia.spring.Path.on;
import static de.escalon.hypermedia.spring.Path.path;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.RelProvider;
import org.springframework.hateoas.UriTemplate;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.hateoas.core.DefaultRelProvider;
import org.springframework.hateoas.hal.CurieProvider;
import org.springframework.hateoas.hal.DefaultCurieProvider;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.http.HttpMethod;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import de.escalon.hypermedia.spring.halforms.Jackson2HalFormsModule.HalFormsHandlerInstantiator;
import de.escalon.hypermedia.spring.halforms.testbeans.DummyController;
import de.escalon.hypermedia.spring.halforms.testbeans.Item;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration
public class HalFormsResponseTest {

	public static final Logger LOG = LoggerFactory.getLogger(HalFormsResponseTest.class);

	private MockMvc mockMvc;

	private static final ObjectMapper objectMapper = new ObjectMapper();

	private static DummyController dm;

	private static final RelProvider relProvider = new DefaultRelProvider();

	private static final CurieProvider curieProvider = new DefaultCurieProvider("test",
			new UriTemplate("http://localhost:8080/profile/{rel}"));

	@Configuration
	@EnableWebMvc
	@EnableHypermediaSupport(type = { HypermediaType.HAL })
	static class WebConfig extends WebMvcConfigurerAdapter {

		@Bean
		public DummyController dummyController() {
			dm = new DummyController();
			return dm;
		}

		@Override
		public void configureMessageConverters(final List<HttpMessageConverter<?>> converters) {
			super.configureMessageConverters(converters);
		}

		@Override
		public void configureHandlerExceptionResolvers(final List<HandlerExceptionResolver> exceptionResolvers) {
			final ExceptionHandlerExceptionResolver resolver = new ExceptionHandlerExceptionResolver();
			resolver.setWarnLogCategory(resolver.getClass().getName());
			exceptionResolvers.add(resolver);
		}

	}

	@Autowired private WebApplicationContext wac;

	@Before
	public void setUp() {

		mockMvc = webAppContextSetup(wac).build();
		dm.setUp();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

		objectMapper.registerModule(new Jackson2HalModule());
		objectMapper.registerModule(new Jackson2HalFormsModule());
		objectMapper.setHandlerInstantiator(new HalFormsHandlerInstantiator(relProvider, curieProvider, null, true));
	}

	@Test
	public void testRequestWithStatusFound() throws Exception {

		int index = 0;
		for (Item item : dm.items) {
			assertCommonForItem(index, DummyController.DELETE, HttpMethod.PUT.toString());
			String jsonEdit = assertCommonForItem(index, DummyController.MODIFY, HttpMethod.PUT.toString());
			System.out.println(jsonEdit);
			assertProperty(jsonEdit, 0, path(on(Item.class).getId()), false, false, Integer.toString(item.getId()));
			assertProperty(jsonEdit, 1, path(on(Item.class).getName()), DummyController.NAME_READONLY.contains(index),
					DummyController.NAME_REQUIRED.contains(index), item.getName());
			assertProperty(jsonEdit, 2, path(on(Item.class).getType()), DummyController.TYPE_READONLY.contains(index),
					DummyController.TYPE_REQUIRED.contains(index), item.getType().toString());
			assertProperty(jsonEdit, 3, path(on(Item.class).getMultiple()), false, false, item.getMultiple().toString());
			assertProperty(jsonEdit, 4, path(on(Item.class).getSingleSub()), DummyController.SUBITEM_READONLY.contains(index),
					DummyController.SUBITEM_REQUIRED.contains(index), item.getSingleSub().toString());
			assertProperty(jsonEdit, 5, path(on(Item.class).getSubItemId()),
					DummyController.SUBITEM_ID_READONLY.contains(index), DummyController.SUBITEM_ID_REQUIRED.contains(index),
					Integer.toString(item.getSubItemId()));

			index++;
		}
	}

	private void assertProperty(String jsonEdit, int i, String name, boolean readOnly, boolean required, String value) {
		assertThat(jsonEdit, hasJsonPath("$._templates.default.properties[" + i + "].name", equalTo(name)));
		assertThat(jsonEdit, hasJsonPath("$._templates.default.properties[" + i + "].readOnly", equalTo(readOnly)));

		String requiredProperty = "$._templates.default.properties[" + i + "].required";
		if (required) {
			assertThat(jsonEdit, hasJsonPath(requiredProperty, equalTo(required)));
		} else {
			try {
				assertThat(jsonEdit, hasJsonPath(requiredProperty, equalTo(required)));

			} catch (AssertionError e) {
				assertThat(jsonEdit, hasNoJsonPath(requiredProperty));

			}
		}
		if (value != null) {
			try {
				assertThat(jsonEdit, hasJsonPath("$._templates.default.properties[" + i + "].value", equalTo(value)));
			} catch (AssertionError e) {
				e.printStackTrace();
			}
		}

	}

	private String assertCommonForItem(int item, String rel, String method) {
		// If @RequestParam annotation is not present the request is correct
		Object entity = HalFormsUtils.toHalFormsDocument(dm.get(item, DummyController.MODIFY));
		String json = objectMapper.valueToTree(entity).toString();
		System.out.println(json);

		assertThat(json, hasJsonPath("$._links.self"));
		assertThat(json, hasJsonPath("$._templates"));
		/**
		 * By default we are sending three links, get (even that it has no parameters), put and delete, the first one is
		 * default as it is mandatory
		 */
		assertThat(json, hasJsonPath("$._templates.default"));
		assertThat(json, hasJsonPath("$._templates.default.method", equalTo(method)));
		if (method.equals("GET") || method.equals("DELETE")) {
			assertThat(json, hasNoJsonPath("$._templates.delete.properties"));
		}
		return json;
	}
}
