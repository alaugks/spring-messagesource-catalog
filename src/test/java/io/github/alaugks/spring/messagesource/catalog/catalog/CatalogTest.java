package io.github.alaugks.spring.messagesource.catalog.catalog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.github.alaugks.spring.messagesource.catalog.TransUnitsCatalog;
import io.github.alaugks.spring.messagesource.catalog.records.TransUnit;
import io.github.alaugks.spring.messagesource.catalog.records.TransUnitInterface;
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
		catalog.nextCatalog(new TransUnitsCatalog(transUnits));
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

}
