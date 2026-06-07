// SPDX-License-Identifier: Apache-2.0
// Copyright 2024 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.catalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.alaugks.spring.messagesource.catalog.catalog.AbstractCatalog;
import io.github.alaugks.spring.messagesource.catalog.records.TransUnit;
import io.github.alaugks.spring.messagesource.catalog.records.TransUnitInterface;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.AbstractMessageSource;

@SuppressWarnings({"java:S4144"})
class CatalogMessageSourceBuilderTest {

	public final Locale locale = Locale.forLanguageTag("en");


	@Test
	void test_resolution_messageFormat() {
		Locale en = Locale.forLanguageTag("en");
		Locale de = Locale.forLanguageTag("de");
		Locale enUs = Locale.forLanguageTag("en-US");

		List<TransUnitInterface> transUnits = List.of(
			new TransUnit(en, "list_files", "Es gibt {0,choice,0#keine Datei|1#eine Datei|1<{0,number,integer} Dateien}"),
			new TransUnit(de, "list_files", "Es gibt {0,choice,0#keine Datei|1#eine Datei|1<{0,number,integer} Dateien}")
		);

		CatalogMessageSourceBuilder ms = CatalogMessageSourceBuilder
			.builder(transUnits, en)
			.build();

		assertEquals("Es gibt 10,000 Dateien", ms.getMessage("list_files", new Object[] {10000}, "", en));
		assertEquals("Es gibt 10.000 Dateien", ms.getMessage("list_files", new Object[] {10000}, "", de));
	}

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
	void test_resolution_multiDomain() {
		Locale en = Locale.forLanguageTag("en");
		Locale de = Locale.forLanguageTag("de");
		Locale enUs = Locale.forLanguageTag("en-US");

		List<TransUnitInterface> transUnits = List.of(
				new TransUnit(en, "key_1", "value_en_1"),
				new TransUnit(en, "key_2", "value_en_2"),
				new TransUnit(de, "key_1", "value_de_1"),
				new TransUnit(de, "key_2", "value_de_2"),
				new TransUnit(en, "key_1", "value_en_1", "foobar"),
				new TransUnit(en, "key_2", "value_en_2", "foobar"),
				new TransUnit(enUs, "key_1", "value_en_us_1", "messages")
		);

		CatalogMessageSourceBuilder ms = CatalogMessageSourceBuilder
				.builder(transUnits, en)
				.build();

		assertEquals("value_en_1", ms.getMessage("key_1", null, en));
		assertEquals("value_en_1", ms.getMessage("messages.key_1", null, en));
		assertEquals("value_en_2", ms.getMessage("messages.key_2", null, en));
		assertEquals("value_en_1", ms.getMessage("foobar.key_1", null, en));
		assertEquals("value_en_2", ms.getMessage("foobar.key_2", null, en));

		assertEquals("value_de_1", ms.getMessage("messages.key_1", null, de));
		assertEquals("value_de_2", ms.getMessage("messages.key_2", null, de));

		assertEquals("value_en_us_1", ms.getMessage("messages.key_1", null, enUs));
	}

	@Test
	void test_addSource_catalogInterface() {
		List<TransUnitInterface> first = List.of(
				new TransUnit(this.locale, "key_a", "value_a")
		);
		List<TransUnitInterface> second = List.of(
				new TransUnit(this.locale, "key_b", "value_b")
		);

		CatalogMessageSourceBuilder ms = CatalogMessageSourceBuilder
				.builder(first, this.locale)
				.addSource(new TransUnitsCatalog(second))
				.build();

		assertEquals("value_a", ms.getMessage("key_a", null, this.locale));
		assertEquals("value_b", ms.getMessage("key_b", null, this.locale));
	}

	@Test
	void test_addSource_transUnitsList() {
		List<TransUnitInterface> first = List.of(
				new TransUnit(this.locale, "key_a", "value_a")
		);
		List<TransUnitInterface> second = List.of(
				new TransUnit(this.locale, "key_b", "value_b")
		);

		CatalogMessageSourceBuilder ms = CatalogMessageSourceBuilder
				.builder(first, this.locale)
				.addSource(second)
				.build();

		assertEquals("value_a", ms.getMessage("key_a", null, this.locale));
		assertEquals("value_b", ms.getMessage("key_b", null, this.locale));
	}

	@Test
	void test_addSource_multipleAppendInOrder() {
		List<TransUnitInterface> first = List.of(
				new TransUnit(this.locale, "key_a", "value_a")
		);

		CatalogMessageSourceBuilder ms = CatalogMessageSourceBuilder
				.builder(first, this.locale)
				.addSource(List.of(new TransUnit(this.locale, "key_b", "value_b")))
				.addSource(new FooBarCatalog())
				.build();

		assertEquals("value_a", ms.getMessage("key_a", null, this.locale));
		assertEquals("value_b", ms.getMessage("key_b", null, this.locale));
		assertEquals("foobar_value", ms.getMessage("dummy", null, this.locale));
	}

	@Test
	void test_resolution_chainFallback() {
		List<TransUnitInterface> transUnits = List.of(
				new TransUnit(this.locale, "key_1", "value_en_1")
		);

		TransUnitsCatalog source = new TransUnitsCatalog(transUnits);
		source.nextCatalog(new FooBarCatalog());

		CatalogMessageSourceBuilder ms = CatalogMessageSourceBuilder
				.builder(source, this.locale)
				.build();

		assertEquals("value_en_1", ms.getMessage("key_1", null, this.locale));
		assertEquals("foobar_value", ms.getMessage("dummy", null, this.locale));
		assertEquals("foobar_value", ms.getMessage("messages.dummy", null, this.locale));
	}

	@Test
	void test_resolution_notResolved() {
		List<TransUnitInterface> transUnits = List.of(
				new TransUnit(this.locale, "key_1", "value_en_1")
		);

		CatalogMessageSourceBuilder ms = CatalogMessageSourceBuilder
				.builder(transUnits, this.locale)
				.build();

		assertThrows(NoSuchMessageException.class,
				() -> ms.getMessage("not_exists", null, this.locale));
		assertThrows(NoSuchMessageException.class,
				() -> ms.getMessage("messages.not_exists", null, this.locale));
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

//	@Test
//	void test_CatalogMessageSource_setParentMessageSource() {
//		CatalogMessageSourceBuilder messageSource = CatalogMessageSourceBuilder
//				.builder(new ArrayList<>(), locale)
//				.build();
//
//		messageSource.setParentMessageSource(new MockMessageSource());
//
//		assertEquals("code_value", messageSource.getMessage(
//				"code",
//				new Object[] {},
//				Locale.forLanguageTag("en")
//		));
//	}

	static class MockMessageSource extends AbstractMessageSource {

		@Override
		protected MessageFormat resolveCode(String code, Locale locale) {
			if (code.equals("code")) {
				return new MessageFormat("code_value");
			}
			return null;
		}

	}

	static class FooBarCatalog extends AbstractCatalog {

		@Override
		public TransUnitInterface resolveTransUnit(String code, Locale locale) {
			if (Objects.equals(code, "dummy") || Objects.equals(code, "messages.dummy")) {
				return new TransUnit(locale, code, "foobar_value");
			}
			return null;
		}
	}
}
