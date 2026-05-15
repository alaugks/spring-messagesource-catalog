// SPDX-License-Identifier: Apache-2.0
// Copyright 2024 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.catalog.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.alaugks.spring.messagesource.catalog.exception.CatalogMessageSourceRuntimeException;
import io.github.alaugks.spring.messagesource.catalog.records.TranslationFile;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class ResourcesLoaderTest {

	@Test
	void test_setLocationPatterns() {
		ResourcesLoader resourcesLoader = new ResourcesLoader(
			Locale.forLanguageTag("en"),
			new LocationPattern("translations/*"),
			List.of("txt")
		);

		assertEquals(5, resourcesLoader.getTranslationFiles().size());
	}

	@Test
	void test_setLocationPatterns_domainMessages() {
		ResourcesLoader resourcesLoader = new ResourcesLoader(
				Locale.forLanguageTag("en"),
				new LocationPattern("translations/messages*"),
				List.of("txt")
		);

		assertEquals(3, resourcesLoader.getTranslationFiles().size());
	}


	@Test
	void test_setLocationPatterns_languageDe() {
		ResourcesLoader resourcesLoader = new ResourcesLoader(
				Locale.forLanguageTag("en"),
				new LocationPattern("translations/*_de*"),
				List.of("txt")
		);

		assertEquals(2, resourcesLoader.getTranslationFiles().size());
	}

	@Test
	void test_setLocationPatternsPattern() {
		ResourcesLoader resourcesLoader = new ResourcesLoader(
				Locale.forLanguageTag("en"),
				new LocationPattern(List.of("translations_en/*", "translations_de/*")),
				List.of("txt")
		);

		assertEquals(4, resourcesLoader.getTranslationFiles().size());
	}

	@Test
	void test_Record() {
		ResourcesLoader resourcesLoader = new ResourcesLoader(
				Locale.forLanguageTag("en"),
				new LocationPattern(List.of("translations_en_US/*")),
				List.of("txt")
		);

		TranslationFile translationFile = resourcesLoader.getTranslationFiles().get(0);

		assertEquals("messages", translationFile.domain());
		assertEquals("en_US", translationFile.locale().toString());
		assertNotNull(translationFile.content());
	}

	@Test
	void test_parseFileName_null() {
		ResourcesLoader resourcesLoader = new ResourcesLoader(
				Locale.forLanguageTag("en"),
				new LocationPattern("translations/.txt"),
				List.of("txt")
		);

		assertTrue(resourcesLoader.getTranslationFiles().isEmpty());
	}

	@Test
	void test_Exception() {
		ResourcesLoader resourcesLoader = new ResourcesLoader(
			Locale.forLanguageTag("en"),
			new LocationPattern("translations/not-exists.txt"),
			List.of("txt")
		);

		Exception e = assertThrows(CatalogMessageSourceRuntimeException.class, resourcesLoader::getTranslationFiles);
		assertInstanceOf(IOException.class, e.getCause());
	}
}
