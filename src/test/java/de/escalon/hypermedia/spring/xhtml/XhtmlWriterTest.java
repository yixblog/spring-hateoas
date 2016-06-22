package de.escalon.hypermedia.spring.xhtml;

import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

import org.custommonkey.xmlunit.XMLAssert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import de.escalon.hypermedia.action.Input;
import de.escalon.hypermedia.spring.AffordanceBuilder;
import de.escalon.hypermedia.spring.sample.test.CreativeWork;
import de.escalon.hypermedia.spring.sample.test.Event;
import de.escalon.hypermedia.spring.sample.test.EventStatusType;

/**
 * Created by Dietrich on 06.06.2015.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration
public class XhtmlWriterTest {

	@Autowired
	private WebApplicationContext wac;

	private MockMvc mockMvc;

	Writer writer = new StringWriter();

	XhtmlWriter xhtml = new XhtmlWriter(writer);

	Writer writerOld = new StringWriter();

	OldXhtmlWriter xhtmlOld = new OldXhtmlWriter(writerOld);

	@Configuration
	@EnableWebMvc
	static class WebConfig extends WebMvcConfigurerAdapter {

	}

	@Before
	public void setup() {
		mockMvc = webAppContextSetup(wac).build();
	}

	@Test
	public void testPutBodyComplete() throws Exception {

		@RequestMapping("/")
		class DummyController {

			@RequestMapping(method = RequestMethod.PUT)
			public ResponseEntity<Void> putMultiplePossibleValues(@RequestBody final Event event) {
				return null;
			}
		}

		Link affordance = AffordanceBuilder
				.linkTo(AffordanceBuilder.methodOn(DummyController.class)
						.putMultiplePossibleValues(new Event(0, null, new CreativeWork(null), null, EventStatusType.EVENT_SCHEDULED)))
				.withSelfRel();

		String xml = writeXml(Arrays.asList(affordance));

		// renders writable event bean properties only
		XMLAssert.assertXpathEvaluatesTo("EVENT_CANCELLED", "//select[@name='eventStatus']/option[1]", xml);
		XMLAssert.assertXpathEvaluatesTo("EVENT_POSTPONED", "//select[@name='eventStatus']/option[2]", xml);
		XMLAssert.assertXpathEvaluatesTo("EVENT_SCHEDULED", "//select[@name='eventStatus']/option[3]", xml);
		XMLAssert.assertXpathEvaluatesTo("EVENT_RESCHEDULED", "//select[@name='eventStatus']/option[4]", xml);
		XMLAssert.assertXpathEvaluatesTo("EVENT_SCHEDULED", "//select[@name='eventStatus']/option[@selected]", xml);
		XMLAssert.assertXpathEvaluatesTo("7-10", "//select[@name='typicalAgeRange']/option[1]", xml);
		XMLAssert.assertXpathEvaluatesTo("11-", "//select[@name='typicalAgeRange']/option[2]", xml);

		XMLAssert.assertXpathNotExists("//input[@name='performer']", xml);
		XMLAssert.assertXpathNotExists("//select[@multiple]", xml);
	}

	@Test
	public void testPostBodyComplete() throws Exception {

		@RequestMapping("/")
		class DummyController {

			@RequestMapping(method = RequestMethod.POST)
			public ResponseEntity<Void> postMultiplePossibleValues(@RequestBody final Event event) {
				return null;
			}
		}

		Link affordance = AffordanceBuilder
				.linkTo(AffordanceBuilder.methodOn(DummyController.class)
						.postMultiplePossibleValues(new Event(0, null, new CreativeWork(null), null, EventStatusType.EVENT_SCHEDULED)))
				.withSelfRel();

		String xml = writeXml(Arrays.asList(affordance));

		// renders event bean constructor arguments
		XMLAssert.assertXpathEvaluatesTo("EVENT_CANCELLED", "//select[@name='eventStatus']/option[1]", xml);
		XMLAssert.assertXpathEvaluatesTo("EVENT_POSTPONED", "//select[@name='eventStatus']/option[2]", xml);
		XMLAssert.assertXpathEvaluatesTo("EVENT_SCHEDULED", "//select[@name='eventStatus']/option[3]", xml);
		XMLAssert.assertXpathEvaluatesTo("EVENT_RESCHEDULED", "//select[@name='eventStatus']/option[4]", xml);
		XMLAssert.assertXpathEvaluatesTo("EVENT_SCHEDULED", "//select[@name='eventStatus']/option[@selected]", xml);
		XMLAssert.assertXpathEvaluatesTo("7-10", "//select[@name='typicalAgeRange']/option[1]", xml);
		XMLAssert.assertXpathEvaluatesTo("11-", "//select[@name='typicalAgeRange']/option[2]", xml);
		XMLAssert.assertXpathExists("//input[@name='performer']", xml);
		XMLAssert.assertXpathExists("//input[@name='location']", xml);
		XMLAssert.assertXpathExists("//input[@name='workPerformed.name']", xml);
		XMLAssert.assertXpathNotExists("//select[@multiple]", xml);
	}

	@Test
	public void testPostBodyReadOnlyEventStatus() throws Exception {

		@RequestMapping("/")
		class DummyController {

			@RequestMapping(method = RequestMethod.POST)
			public ResponseEntity<Void> postEventStatusOnly(@RequestBody @Input(readOnly = "eventStatus") final Event event) {
				return null;
			}
		}

		Link affordance = AffordanceBuilder
				.linkTo(AffordanceBuilder.methodOn(DummyController.class)
						.postEventStatusOnly(new Event(0, null, new CreativeWork(null), null, EventStatusType.EVENT_SCHEDULED)))
				.withSelfRel();

		String xml = writeXml(Arrays.asList(affordance));

		XMLAssert.assertXpathEvaluatesTo("EVENT_CANCELLED", "//select[@name='eventStatus']/option[1]", xml);
		XMLAssert.assertXpathEvaluatesTo("EVENT_POSTPONED", "//select[@name='eventStatus']/option[2]", xml);
		XMLAssert.assertXpathEvaluatesTo("EVENT_SCHEDULED", "//select[@name='eventStatus']/option[3]", xml);
		XMLAssert.assertXpathEvaluatesTo("EVENT_RESCHEDULED", "//select[@name='eventStatus']/option[4]", xml);
		XMLAssert.assertXpathEvaluatesTo("EVENT_SCHEDULED", "//select[@name='eventStatus']/option[@selected]", xml);

		XMLAssert.assertXpathNotExists("//select[@name='typicalAgeRange']", xml);
		XMLAssert.assertXpathNotExists("//input[@name='performer']", xml);
		XMLAssert.assertXpathNotExists("//input[@name='location']", xml);
		XMLAssert.assertXpathNotExists("//input[@name='workPerformed.name']", xml);
	}

	@Test
	public void testPostBodyHiddenEventStatus() throws Exception {

		@RequestMapping("/")
		class DummyController {

			@RequestMapping(method = RequestMethod.POST)
			public ResponseEntity<Void> postEventStatusOnly(@RequestBody @Input(hidden = "eventStatus") final Event event) {
				return null;
			}
		}

		Link affordance = AffordanceBuilder
				.linkTo(AffordanceBuilder.methodOn(DummyController.class)
						.postEventStatusOnly(new Event(0, null, new CreativeWork(null), null, EventStatusType.EVENT_SCHEDULED)))
				.withSelfRel();

		String xml = writeXml(Arrays.asList(affordance));

		XMLAssert.assertXpathEvaluatesTo("EVENT_SCHEDULED", "//option[@selected='selected']/text()", xml);

		XMLAssert.assertXpathNotExists("//input[@name='performer']", xml);
		XMLAssert.assertXpathNotExists("//select[@name='typicalAgeRange']", xml);
		XMLAssert.assertXpathNotExists("//input[@name='location']", xml);
		XMLAssert.assertXpathNotExists("//input[@name='workPerformed.name']", xml);
	}

	@Test
	public void testPostBodyHiddenEventStatusWithWorkPerformedName() throws Exception {

		@RequestMapping("/")
		class DummyController {

			@RequestMapping(method = RequestMethod.POST)
			public ResponseEntity<Void> postEventStatusOnly(
					@RequestBody @Input(hidden = "eventStatus", include = "name") final Event event) {
				return null;
			}
		}

		Link affordance = AffordanceBuilder
				.linkTo(AffordanceBuilder.methodOn(DummyController.class)
						.postEventStatusOnly(new Event(0, null, new CreativeWork(null), null, EventStatusType.EVENT_SCHEDULED)))
				.withSelfRel();

		String xml = writeXml(Arrays.asList(affordance));

		XMLAssert.assertXpathEvaluatesTo("EVENT_SCHEDULED", "//option[@selected='selected']/text()", xml);
		XMLAssert.assertXpathExists("//input[@name='workPerformed.name']", xml);

		XMLAssert.assertXpathNotExists("//input[@name='performer']", xml);
		XMLAssert.assertXpathNotExists("//select[@name='typicalAgeRange']", xml);
		XMLAssert.assertXpathNotExists("//input[@name='location']", xml);
	}

	private String writeXml(final List<Link> links) {
		String xml = "";
		try {
			xhtml.writeLinks(links);

			xml = writer.toString();

			xhtmlOld.writeLinks(links);
			System.out.println("++++ new XhtmlWriter implementation result-->" + xml);
			System.out.println("---- old XhtmlWriter implementation result-->" + writerOld.toString());
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return xml;
	}
}