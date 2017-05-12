/*
 * Copyright 2014 the original author or authors.
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
package org.springframework.hateoas;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.hateoas.TemplateVariable.VariableType.*;

import java.util.List;

import org.junit.Test;
import org.springframework.hateoas.TemplateVariable.VariableType;

/**
 * Unit tests for {@link TemplateVariables}.
 * 
 * @author Oliver Gierke
 */
public class TemplateVariablesUnitTest {

	/**
	 * @see #137
	 */
	@Test
	public void rendersNoTempalteVariablesAsEmptyString() {
		assertThat(TemplateVariables.NONE.toString()).isEqualTo("");
	}

	/**
	 * @see #137
	 */
	@Test
	public void rendersSingleVariableCorrectly() {

		TemplateVariables variables = TemplateVariables.of(TemplateVariable.of("foo", SEGMENT));
		assertThat(variables.toString()).isEqualTo("{/foo}");
	}

	/**
	 * @see #137
	 */
	@Test
	public void combinesMultipleVariablesOfTheSameType() {

		TemplateVariable first = TemplateVariable.of("foo", REQUEST_PARAM);
		TemplateVariable second = TemplateVariable.of("bar", REQUEST_PARAM);

		TemplateVariables variables = TemplateVariables.of(first, second);

		assertThat(variables.toString()).isEqualTo("{?foo,bar}");
	}

	/**
	 * @see #137
	 */
	@Test
	public void combinesMultipleVariablesOfTheDifferentType() {

		TemplateVariable first = TemplateVariable.of("foo", SEGMENT);
		TemplateVariable second = TemplateVariable.of("bar", REQUEST_PARAM);

		TemplateVariables variables = TemplateVariables.of(first, second);

		assertThat(variables.toString()).isEqualTo("{/foo}{?bar}");
	}

	/**
	 * @see #137
	 */
	@Test
	public void concatsVariables() {

		TemplateVariables variables = TemplateVariables.of(TemplateVariable.of("foo", SEGMENT));
		variables = variables.concat(TemplateVariable.of("bar", REQUEST_PARAM));

		assertThat(variables.toString()).isEqualTo("{/foo}{?bar}");
	}

	/**
	 * @see #137
	 */
	@Test
	public void combinesContinuedParamWithParam() {

		TemplateVariable first = TemplateVariable.of("foo", REQUEST_PARAM);
		TemplateVariable second = TemplateVariable.of("bar", REQUEST_PARAM_CONTINUED);

		TemplateVariables variables = TemplateVariables.of(first, second);

		assertThat(variables.toString()).isEqualTo("{?foo,bar}");
	}

	/**
	 * @see #137
	 */
	@Test
	public void combinesContinuedParameterWithParameter() {

		TemplateVariable first = TemplateVariable.of("foo", REQUEST_PARAM_CONTINUED);
		TemplateVariable second = TemplateVariable.of("bar", REQUEST_PARAM);

		TemplateVariables variables = TemplateVariables.of(first, second);

		assertThat(variables.toString()).isEqualTo("{&foo,bar}");
	}

	/**
	 * @see #198
	 */
	@Test
	public void dropsDuplicateTemplateVariable() {

		TemplateVariable variable = TemplateVariable.of("foo", REQUEST_PARAM);
		TemplateVariables variables = TemplateVariables.of(variable);

		List<TemplateVariable> result = variables.concat(variable).asList();

		assertThat(result).hasSize(1);
		assertThat(result).contains(variable);
	}

	/**
	 * @see #217
	 */
	@Test
	public void considersRequestParameterVariablesEquivalent() {

		TemplateVariable parameter = TemplateVariable.of("foo", REQUEST_PARAM);
		TemplateVariable continued = TemplateVariable.of("foo", REQUEST_PARAM_CONTINUED);
		TemplateVariable fragment = TemplateVariable.of("foo", FRAGMENT);

		assertThat(parameter.isEquivalent(continued)).isTrue();
		assertThat(continued.isEquivalent(parameter)).isTrue();
		assertThat(fragment.isEquivalent(continued)).isFalse();
	}

	/**
	 * @see #217
	 */
	@Test
	public void considersFragementVariable() {

		assertThat(TemplateVariable.of("foo", VariableType.FRAGMENT).isFragment()).isTrue();
		assertThat(TemplateVariable.of("foo", VariableType.REQUEST_PARAM).isFragment()).isFalse();
	}

	/**
	 * @see #217
	 */
	@Test
	public void doesNotAddEquivalentVariable() {

		TemplateVariable parameter = TemplateVariable.of("foo", VariableType.REQUEST_PARAM);
		TemplateVariable parameterContinued = TemplateVariable.of("foo", VariableType.REQUEST_PARAM_CONTINUED);

		List<TemplateVariable> result = TemplateVariables.of(parameter).concat(parameterContinued).asList();

		assertThat(result).hasSize(1);
		assertThat(result).contains(parameter);
	}

	/**
	 * @see #228
	 */
	@Test(expected = IllegalArgumentException.class)
	public void variableRejectsEmptyName() {
		TemplateVariable.of("", PATH_VARIABLE);
	}

	/**
	 * @see #228
	 */
	@Test(expected = IllegalArgumentException.class)
	public void variableRejectsNullName() {
		TemplateVariable.of(null, PATH_VARIABLE);
	}

	/**
	 * @see #228
	 */
	@Test(expected = IllegalArgumentException.class)
	public void variableRejectsNullType() {
		TemplateVariable.of("foo", null);
	}

	/**
	 * @see #228
	 */
	@Test(expected = IllegalArgumentException.class)
	public void variableRejectsNullDescription() {
		TemplateVariable.of("foo", PATH_VARIABLE, null);
	}
}
