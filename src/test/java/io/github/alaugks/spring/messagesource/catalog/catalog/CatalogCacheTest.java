package io.github.alaugks.spring.messagesource.catalog.catalog;

import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Locale;
import org.junit.jupiter.api.Test;

class CatalogCacheTest {

	@Test
	void test_get_paramValuesEmpty() {
		CatalogCache catalog = new CatalogCache();

		assertNull(catalog.resolveCode("", Locale.forLanguageTag("en")));
		assertNull(catalog.resolveCode("messages.m_en_1", Locale.forLanguageTag("")));
	}

}
