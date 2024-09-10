package io.github.alaugks.spring.messagesource.catalog.catalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.github.alaugks.spring.messagesource.catalog.TransUnitsCatalog;
import io.github.alaugks.spring.messagesource.catalog.records.TransUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CatalogTest {

	static Catalog catalog;

	@BeforeEach
	void BeforeEach() {

		List<TransUnit> transUnits = new ArrayList<>();

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
		catalog.nextHandler(new TransUnitsCatalog(transUnits));
		catalog.build();
	}

	@Test
	void test_fallback() {
		// Domain messages
		Locale locale = Locale.forLanguageTag("en");
		assertEquals("value_en_1", catalog.resolveCode(locale, "messages.key_1"));
		assertEquals("value_en_1", catalog.resolveCode(locale, "key_1"));
	}

	@Test
	void test_en() {
		// Domain messages
		Locale locale = Locale.forLanguageTag("en");
		assertEquals("value_en_1", catalog.resolveCode(locale, "messages.key_1"));
		// Domain foobar
		assertEquals("value_en_1", catalog.resolveCode(locale, "foobar.key_1"));
		// Domain messages
		assertEquals("value_en_2", catalog.resolveCode(locale, "messages.key_2"));
		// Domain foobar
		assertEquals("value_en_2", catalog.resolveCode(locale, "foobar.key_2"));

		// Domain foobar
		assertNull(catalog.resolveCode(locale, "foobar.key_3"));
		// Domain messages
		assertNull(catalog.resolveCode(locale, "messages.key_3"));
	}

	@Test
	void test_de() {
		// Domain messages
		Locale locale = Locale.forLanguageTag("de");
		assertEquals("value_de_1", catalog.resolveCode(locale, "messages.key_1"));
		assertEquals("value_de_2", catalog.resolveCode(locale, "messages.key_2"));
	}

	@Test
	void test_enUk_withRegion() {
		Locale locale = Locale.forLanguageTag("en-US");
		// Domain messages
		assertEquals("value_en_us_1", catalog.resolveCode(locale, "messages.key_1"));
	}

	@Test
	void test_get_paramValuesEmpty() {
		assertNull(catalog.resolveCode(Locale.forLanguageTag("en"), ""));
		assertNull(catalog.resolveCode(Locale.forLanguageTag(""), "messages.m_en_1"));
	}

}
