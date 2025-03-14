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

package io.github.alaugks.spring.messagesource.catalog;

import io.github.alaugks.spring.messagesource.catalog.records.TransUnit;
import io.github.alaugks.spring.messagesource.catalog.records.TransUnitInterface;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.AbstractMessageSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings({"java:S4144"})
class CatalogMessageSourceBuilderTest {

	public final Locale locale = Locale.forLanguageTag("en");

	@Test
	void test_builder_withList() {
		List<TransUnitInterface> transUnits = List.of(
				new TransUnit(this.locale, "key", "messages_value")
		);

		assertEquals(
				"messages_value",
				CatalogMessageSourceBuilder
						.builder(transUnits, this.locale)
						.build().getMessage("key", null, this.locale)
		);
	}

	@Test
	void test_builder_withCatalogInterface() {
		List<TransUnitInterface> transUnits = List.of(
				new TransUnit(this.locale, "key", "messages_value")
		);

		assertEquals(
				"messages_value",
				CatalogMessageSourceBuilder
						.builder(new TransUnitsCatalog(transUnits), this.locale)
						.build()
						.getMessage("key", null, this.locale)
		);
	}

	@Test
	void test_withSetDefaultDomain() {
		List<TransUnitInterface> transUnits = List.of(
				new TransUnit(this.locale, "key", "messages_value"),
				new TransUnit(this.locale, "key", "foo_value", "foo")
		);

		assertEquals(
				"foo_value",
				CatalogMessageSourceBuilder
						.builder(new TransUnitsCatalog(transUnits), this.locale)
						.defaultDomain("foo")
						.build()
						.getMessage("key", null, this.locale)
		);
	}

	@Test
	void test_CatalogMessageSource_asParentMessageSource() {
		List <TransUnitInterface> transUnits = new ArrayList<>();
		transUnits.add(new TransUnit(this.locale, "key", "key_value"));

		CatalogMessageSourceBuilder catalogMessageSourceBuilder = CatalogMessageSourceBuilder
				.builder(transUnits, locale)
				.build();

		MockMessageSource mockMessageSource = new MockMessageSource();
		mockMessageSource.setParentMessageSource(catalogMessageSourceBuilder);

		assertEquals("key_value", mockMessageSource.getMessage(
				"key",
				new Object[] {},
				Locale.forLanguageTag("en")
		));
	}

	@Test
	void test_CatalogMessageSource_setParentMessageSource() {
		CatalogMessageSourceBuilder messageSource = CatalogMessageSourceBuilder
				.builder(new ArrayList<>(), locale)
				.build();

		messageSource.setParentMessageSource(new MockMessageSource());

		assertEquals("code_value", messageSource.getMessage(
				"code",
				new Object[] {},
				Locale.forLanguageTag("en")
		));
	}

	static class MockMessageSource extends AbstractMessageSource {

		@Override
		protected MessageFormat resolveCode(String code, Locale locale) {
			if (code.equals("code")) {
				return new MessageFormat("code_value");
			}
			return null;
		}

	}
}
