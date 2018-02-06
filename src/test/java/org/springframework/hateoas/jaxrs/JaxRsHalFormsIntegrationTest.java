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

import static org.assertj.core.api.Assertions.*;
import static org.springframework.hateoas.jaxrs.JaxRsLinkBuilder.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author Greg Turnquist
 */
public class JaxRsHalFormsIntegrationTest extends JerseyTest {

	private final static Map<Integer, Employee> EMPLOYEES = new TreeMap<>();
	private final static String MEDIA_TYPE = MediaTypes.HAL_FORMS_JSON_VALUE;

	@BeforeClass
	public static void setUpClass() {

		EMPLOYEES.put(0, new Employee("Frodo Baggins", "ring bearer"));
		EMPLOYEES.put(1, new Employee("Bilbo Baggins", "burglar"));
		System.out.println(EMPLOYEES.values());
	}


	@Test
	public void fetchingAllResourcesShouldWork() throws IOException {

		Resources<Resource<Employee>> resources = target("/api/employees")
			.request(MEDIA_TYPE)
			.get(new GenericType<Resources<Resource<Employee>>>() {});

		assertThat(resources.getLinks()).hasSize(1);
		assertThat(resources.getRequiredLink(Link.REL_SELF)).isEqualTo(new Link("/api/employees", "self"));

		List<Resource<Employee>> resourceList = new ArrayList<>(resources.getContent());

		assertThat(resourceList).hasSize(2);
		
		assertThat(resourceList.get(0).getContent()).isEqualTo(new Employee("Frodo Baggins", "ring bearer"));;
		assertThat(resourceList.get(0).getLinks()).hasSize(2);
		assertThat(resourceList.get(0).getRequiredLink(Link.REL_SELF)).isEqualTo(new Link("/api/employees/0"));
		assertThat(resourceList.get(0).getRequiredLink("employees")).isEqualTo(new Link("/api/employees", "employees"));

		assertThat(resourceList.get(1).getContent()).isEqualTo(new Employee("Bilbo Baggins", "burglar"));
		assertThat(resourceList.get(1).getLinks()).hasSize(2);
		assertThat(resourceList.get(1).getRequiredLink(Link.REL_SELF)).isEqualTo(new Link("/api/employees/1"));
		assertThat(resourceList.get(1).getRequiredLink("employees")).isEqualTo(new Link("/api/employees", "employees"));
	}

	@Test
	public void creatingNewEmployeeShouldWork() throws URISyntaxException {

		Response creationResponse = target("/api/employees")
			.request(MediaType.APPLICATION_JSON)
			.post(Entity.json(new Employee("Samwise Gamgee", "gardener")));

		assertThat(creationResponse.getStatus()).isEqualTo(HttpStatus.CREATED.value());

		Resource<Employee> employeeResource = target(new URI(creationResponse.getHeaderString(HttpHeaders.LOCATION)).getPath())
			.request(MEDIA_TYPE)
			.get(new GenericType<Resource<Employee>>() {});

		assertThat(employeeResource.getContent()).isEqualTo(new Employee("Samwise Gamgee", "gardener"));
	}

	@Test
	public void updatingExistingEmployeeShouldWork() {

		Resource<Employee> bilbo = target("/api/employees/1")
			.request(MEDIA_TYPE)
			.get(new GenericType<Resource<Employee>>() {});

		assertThat(bilbo.getContent().getRole()).isEqualTo("burglar");

		bilbo.getContent().setRole("ring bearer");

		Response response = target("/api/employees/1")
			.request(MediaType.APPLICATION_JSON)
			.put(Entity.json(bilbo.getContent()));

		assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());

		Resource<Employee> updatedBilbo = target("/api/employees/1")
			.request(MEDIA_TYPE)
			.get(new GenericType<Resource<Employee>>() {});

		assertThat(updatedBilbo.getContent().getRole()).isEqualTo("ring bearer");
	}

	@Override
	protected Application configure() {

		ResourceConfig resourceConfig = new ResourceConfig(EmployeeController.class);
		resourceConfig.register(Jackson2HalFormsFeature.class);
		resourceConfig.property(LoggingFeature.LOGGING_FEATURE_LOGGER_LEVEL_SERVER, "SEVERE");
		return resourceConfig;
	}

	@Override
	protected void configureClient(ClientConfig config) {
		config.register(Jackson2HalFormsFeature.class);
		config.property(LoggingFeature.LOGGING_FEATURE_LOGGER_LEVEL_CLIENT, "SEVERE");
	}

	@Path("/api")
	public static class EmployeeController {

		@GET
		@Path("/employees")
		@Produces(MEDIA_TYPE)
		public Resources<Resource<Employee>> all() {

			List<Resource<Employee>> employeeResources = EMPLOYEES.keySet().stream()
				.map(this::findOne)
				.collect(Collectors.toList());

			return new Resources<>(employeeResources,
				linkTo(methodOn(EmployeeController.class).all()).withSelfRel()
					.andAffordance(afford(methodOn(EmployeeController.class).newEmployee(null))));
		}

		@POST
		@Path("/employees")
		@Consumes(MediaType.APPLICATION_JSON)
		public Response newEmployee(Employee employee) {

			int newEmployeeId = EMPLOYEES.size();

			EMPLOYEES.put(newEmployeeId, employee);

			try {
				return Response
					.created(new URI(findOne(newEmployeeId).getRequiredLink(Link.REL_SELF).getHref()))
					.build();
			} catch (Exception e) {
				return Response.serverError().entity(e.getMessage()).build();
			}
		}

		@GET
		@Path("/employees/{id}")
		@Produces(MEDIA_TYPE)
		public Resource<Employee> findOne(@PathParam("id") int id) {
			return new Resource<>(EMPLOYEES.get(id),
				linkTo(methodOn(EmployeeController.class).findOne(id)).withSelfRel()
					.andAffordance(afford(methodOn(EmployeeController.class).updateEmployee(null, id))),
				linkTo(methodOn(EmployeeController.class).all()).withRel("employees"));
		}

		@PUT
		@Path("/employees/{id}")
		@Consumes(MediaType.APPLICATION_JSON)
		public Response updateEmployee(Employee employee, @PathParam("id") int id) {

			EMPLOYEES.put(id, employee);

			return Response.noContent().build();
		}
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
	static class Employee {
		
		private String name;
		private String role;
	}
}
