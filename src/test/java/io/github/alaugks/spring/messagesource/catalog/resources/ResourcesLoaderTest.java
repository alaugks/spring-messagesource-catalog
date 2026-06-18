// SPDX-License-Identifier: Apache-2.0
// Copyright 2024 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.catalog.resources;

import io.github.alaugks.spring.messagesource.catalog.exception.CatalogMessageSourceRuntimeException;
import io.github.alaugks.spring.messagesource.catalog.records.TranslationFile;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourcesLoaderTest {

	static final Locale LOCALE_EN = Locale.forLanguageTag("en");

	@Test
	void test_set_location_patterns() {
		ResourcesLoader resourcesLoader = new ResourcesLoader(
			LOCALE_EN,
			new LocationPattern("translations/*"),
			List.of("txt")
		);

		assertEquals(5, resourcesLoader.getTranslationFiles().size());
	}

	@Test
	void test_set_location_patterns_domain_messages() {
		ResourcesLoader resourcesLoader = new ResourcesLoader(
				LOCALE_EN,
				new LocationPattern("translations/messages*"),
				List.of("txt")
		);

		assertEquals(3, resourcesLoader.getTranslationFiles().size());
	}


	@Test
	void test_set_location_patterns_language_de() {
		ResourcesLoader resourcesLoader = new ResourcesLoader(
				LOCALE_EN,
				new LocationPattern("translations/*_de*"),
				List.of("txt")
		);

		assertEquals(2, resourcesLoader.getTranslationFiles().size());
	}

	@Test
	void test_set_location_patterns_list() {
		ResourcesLoader resourcesLoader = new ResourcesLoader(
				LOCALE_EN,
				new LocationPattern(List.of("translations_en/*", "translations_de/*")),
				List.of("txt")
		);

		assertEquals(4, resourcesLoader.getTranslationFiles().size());
	}

	@Test
	void test_record() {
		ResourcesLoader resourcesLoader = new ResourcesLoader(
				LOCALE_EN,
				new LocationPattern(List.of("translations_en_US/*")),
				List.of("txt")
		);

		TranslationFile translationFile = resourcesLoader.getTranslationFiles().get(0);

		assertEquals("messages", translationFile.domain());
		assertEquals("en_US", translationFile.locale().toString());
		assertNotNull(translationFile.content());
	}

	@Test
	void test_parse_file_name_null() {
		ResourcesLoader resourcesLoader = new ResourcesLoader(
				LOCALE_EN,
				new LocationPattern("translations/.txt"),
				List.of("txt")
		);

		assertTrue(resourcesLoader.getTranslationFiles().isEmpty());
	}

	@Test
	void test_exception() {
		ResourcesLoader resourcesLoader = new ResourcesLoader(
			LOCALE_EN,
			new LocationPattern("translations/not-exists.txt"),
			List.of("txt")
		);

		Exception e = assertThrows(CatalogMessageSourceRuntimeException.class, resourcesLoader::getTranslationFiles);
		assertInstanceOf(IOException.class, e.getCause());
	}
}
