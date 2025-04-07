/*
 * Copyright 2024-2025 André Laugks <alaugks@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.alaugks.spring.messagesource.catalog.catalog;

import io.github.alaugks.spring.messagesource.catalog.records.TransUnit;
import io.github.alaugks.spring.messagesource.catalog.records.TransUnitInterface;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AbstractCatalogTest {

	@Test
	void test_functional() {
		Catalog catalog = new Catalog(Locale.forLanguageTag("en"));
		catalog.nextCatalog(new FirstCatalog()).nextCatalog(new SecondCatalog());
		catalog.build();

		assertEquals("first_value_a", catalog.resolveCode("first_key_a", Locale.forLanguageTag("en")));
		assertEquals("first_value_b", catalog.resolveCode("first_key_b", Locale.forLanguageTag("en")));
		assertEquals("second_value_a", catalog.resolveCode("second_key_a", Locale.forLanguageTag("en")));
		assertEquals("second_value_b", catalog.resolveCode("second_key_b", Locale.forLanguageTag("en")));
	}
}

class FirstCatalog extends AbstractCatalog {

	private List<TransUnitInterface> transUnits;

	@Override
	public List<TransUnitInterface> getTransUnits() {
		this.transUnits = super.getTransUnits();
		this.transUnits.add(new TransUnit(Locale.forLanguageTag("en"), "first_key_a", "first_value_a"));
		return this.transUnits;
	}

	@Override
	public String resolveCode(String code, Locale locale) {
		if (code.equals("first_key_b")) {
			return "first_value_b";
		}

		return this.transUnits
			.stream()
			.filter(t -> code.equals(t.code()))
			.findFirst()
			.map(TransUnitInterface::value)
			.orElse(super.resolveCode(code, locale));
	}
}

class SecondCatalog extends AbstractCatalog {

	@Override
	public String resolveCode(String code, Locale locale) {
		if (code.equals("second_key_b")) {
			return "second_value_b";
		}

		return super.resolveCode(code, locale);
	}

	@Override
	public List<TransUnitInterface> getTransUnits() {
		List<TransUnitInterface> transUnits = super.getTransUnits();
		transUnits.add(new TransUnit(Locale.forLanguageTag("en"), "second_key_a", "second_value_a"));
		return transUnits;
	}
}
