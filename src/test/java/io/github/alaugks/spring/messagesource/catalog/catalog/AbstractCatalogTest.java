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

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.alaugks.spring.messagesource.catalog.records.TransUnit;
import io.github.alaugks.spring.messagesource.catalog.records.TransUnitInterface;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class AbstractCatalogTest {

	@Test
	void test_functional() {
		RootCatalog catalog = new RootCatalog();
		catalog.nextCatalog(new FirstCatalog()).nextCatalog(new SecondCatalog());
		catalog.getTransUnits();

		Locale en = Locale.forLanguageTag("en");
		assertEquals("first_value_a", catalog.resolveTransUnit("first_key_a", en).value());
		assertEquals("first_value_b", catalog.resolveTransUnit("first_key_b", en).value());
		assertEquals("second_value_a", catalog.resolveTransUnit("second_key_a", en).value());
		assertEquals("second_value_b", catalog.resolveTransUnit("second_key_b", en).value());
	}
}

class RootCatalog extends AbstractCatalog {
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
	public TransUnitInterface resolveTransUnit(String code, Locale locale) {
		if (code.equals("first_key_b")) {
			return new TransUnit(locale, code, "first_value_b");
		}

		return this.transUnits
			.stream()
			.filter(t -> code.equals(t.code()))
			.findFirst()
			.orElse(super.resolveTransUnit(code, locale));
	}
}

class SecondCatalog extends AbstractCatalog {

	@Override
	public TransUnitInterface resolveTransUnit(String code, Locale locale) {
		if (code.equals("second_key_b")) {
			return new TransUnit(locale, code, "second_value_b");
		}

		return super.resolveTransUnit(code, locale);
	}

	@Override
	public List<TransUnitInterface> getTransUnits() {
		List<TransUnitInterface> transUnits = super.getTransUnits();
		transUnits.add(new TransUnit(Locale.forLanguageTag("en"), "second_key_a", "second_value_a"));
		return transUnits;
	}
}
