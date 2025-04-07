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

import io.github.alaugks.spring.messagesource.catalog.TransUnitsCatalog;
import io.github.alaugks.spring.messagesource.catalog.records.TransUnit;
import io.github.alaugks.spring.messagesource.catalog.records.TransUnitInterface;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CatalogTest {

	static Catalog catalog;

	@BeforeEach
	void BeforeEach() {

		List<TransUnitInterface> transUnits = new ArrayList<>();

		// Domain messages
		transUnits.add(new TransUnit(Locale.forLanguageTag("en"), "key_1", "value_en_1"));
		transUnits.add(new TransUnit(Locale.forLanguageTag("en"), "key_2", "value_en_2"));
		transUnits.add(new TransUnit(Locale.forLanguageTag("en"), "key_1", "value_en_3")); // Check overwrite

		// Domain messages
		transUnits.add(new TransUnit(Locale.forLanguageTag("de"), "key_1", "value_de_1"));
		transUnits.add(new TransUnit(Locale.forLanguageTag("de"), "key_2", "value_de_2"));
		transUnits.add(new TransUnit(Locale.forLanguageTag("de"), "key_1", "value_de_3")); // Check overwrite

		// Domain foobar
		transUnits.add(new TransUnit(Locale.forLanguageTag("en"), "key_1", "value_en_1", "foobar"));
		transUnits.add(new TransUnit(Locale.forLanguageTag("en"), "key_2", "value_en_2", "foobar"));
		transUnits.add(new TransUnit(Locale.forLanguageTag("en"), "key_1", "value_en_3", "foobar")); // Check overwrite

		// Domain messages
		transUnits.add(new TransUnit(Locale.forLanguageTag("en-US"), "key_1", "value_en_us_1", "messages"));
		transUnits.add(new TransUnit(Locale.forLanguageTag("en_US"), "key_2", "value_en_us_2", "messages"));

		catalog = new Catalog(Locale.forLanguageTag("en"));
		catalog.nextCatalog(new TransUnitsCatalog(transUnits)).nextCatalog(new FooBarCatalog());
		catalog.build();
	}

	@Test
	void test_fallback() {
		// Domain messages
		Locale locale = Locale.forLanguageTag("en");
		assertEquals("value_en_1", catalog.resolveCode("messages.key_1", locale));
		assertEquals("value_en_1", catalog.resolveCode("key_1", locale));
	}

	@Test
	void test_en() {
		// Domain messages
		Locale locale = Locale.forLanguageTag("en");
		assertEquals("value_en_1", catalog.resolveCode("messages.key_1", locale));
		// Domain foobar
		assertEquals("value_en_1", catalog.resolveCode("foobar.key_1", locale));
		// Domain messages
		assertEquals("value_en_2", catalog.resolveCode("messages.key_2", locale));
		// Domain foobar
		assertEquals("value_en_2", catalog.resolveCode("foobar.key_2", locale));

		// Domain foobar
		assertNull(catalog.resolveCode("foobar.key_3", locale));
		// Domain messages
		assertNull(catalog.resolveCode("messages.key_3", locale));
	}

	@Test
	void test_de() {
		// Domain messages
		Locale locale = Locale.forLanguageTag("de");
		assertEquals("value_de_1", catalog.resolveCode("messages.key_1", locale));
		assertEquals("value_de_2", catalog.resolveCode("messages.key_2", locale));
	}

	@Test
	void test_enUk_withRegion() {
		Locale locale = Locale.forLanguageTag("en-US");
		// Domain messages
		assertEquals("value_en_us_1", catalog.resolveCode("messages.key_1", locale));
	}

	@Test
	void test_get_paramValuesEmpty() {
		assertNull(catalog.resolveCode("", Locale.forLanguageTag("en")));
		assertNull(catalog.resolveCode("messages.m_en_1", Locale.forLanguageTag("")));
	}

	@Test
	void test_resolve_nextCatalog() {
		Locale locale = Locale.forLanguageTag("en");
		assertEquals("foobar_value", catalog.resolveCode("dummy", locale));
		assertEquals("foobar_value", catalog.resolveCode("messages.dummy", locale));
	}

	@Test
	void test_not_resolved() {
		Locale locale = Locale.forLanguageTag("en");
		assertNull(catalog.resolveCode("not_exists", locale));
		assertNull(catalog.resolveCode("messages.not_exists", locale));
	}
}

class FooBarCatalog extends AbstractCatalog {

	@Override
	public String resolveCode(String code, Locale locale) {

		if (Objects.equals(code, "dummy") || Objects.equals(code, "messages.dummy")) {
			return "foobar_value";
		}

		return null;
	}
}
