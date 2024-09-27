package io.github.alaugks.spring.messagesource.catalog.catalog;

import java.util.Locale;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

class CatalogCacheTest {

	@Test
	void test_get_paramValuesEmpty() {
		var catalog = new CatalogCache();

		assertNull(catalog.resolveCode("", Locale.forLanguageTag("en")));
		assertNull(catalog.resolveCode("messages.m_en_1", Locale.forLanguageTag("")));
	}

}
