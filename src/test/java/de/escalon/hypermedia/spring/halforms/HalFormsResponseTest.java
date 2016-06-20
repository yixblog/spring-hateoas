package de.escalon.hypermedia.spring.halforms;

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
import org.springframework.hateoas.core.DefaultRelProvider;
import org.springframework.hateoas.hal.CurieProvider;
import org.springframework.hateoas.hal.DefaultCurieProvider;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import de.escalon.hypermedia.spring.halforms.testbeans.DummyController;

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
	static class WebConfig extends WebMvcConfigurerAdapter {

		@Bean
		public DummyController dummyController() {
			dm = new DummyController();
			return dm;
		}

		@Override
		public void configureMessageConverters(final List<HttpMessageConverter<?>> converters) {
			super.configureMessageConverters(converters);

			// TODO: enable converter for testing
			converters.add(new HalFormsMessageConverter(objectMapper, relProvider, curieProvider, null));
		}

		@Override
		public void configureHandlerExceptionResolvers(final List<HandlerExceptionResolver> exceptionResolvers) {
			final ExceptionHandlerExceptionResolver resolver = new ExceptionHandlerExceptionResolver();
			resolver.setWarnLogCategory(resolver.getClass().getName());
			exceptionResolvers.add(resolver);
		}

	}

	@Autowired
	private WebApplicationContext wac;

	@Before
	public void setUp() {
		mockMvc = webAppContextSetup(wac).build();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		dm.prepareResources();
	}

	@Test
	public void testRequestWithStatusFound() throws Exception {

		// If @RequestParam annotation is not present the request is correct
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/test/item/1?rel=MODIFY").contentType(MediaType.APPLICATION_JSON))
				.andReturn();

		LOG.debug(result.getResponse().getContentAsString());

	}
}
