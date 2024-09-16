package io.github.alaugks.spring.messagesource.catalog.ressources;

import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import io.github.alaugks.spring.messagesource.catalog.records.TranslationFile;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourcesLoaderTest {

	@Test
	void test_setLocationPatterns() {
		var resourcesLoader = new ResourcesLoader(
				Locale.forLanguageTag("en"),
				new HashSet<>(List.of("translations/*")),
				List.of("txt")
		);

		assertEquals(5, resourcesLoader.getTranslationFiles().size());
	}

	@Test
	void test_setLocationPatterns_domainMessages() {
		var resourcesLoader = new ResourcesLoader(
				Locale.forLanguageTag("en"),
				new HashSet<>(List.of("translations/messages*")),
				List.of("txt")
		);

		assertEquals(3, resourcesLoader.getTranslationFiles().size());
	}


	@Test
	void test_setLocationPatterns_languageDe() {
		var resourcesLoader = new ResourcesLoader(
				Locale.forLanguageTag("en"),
				new HashSet<>(List.of("translations/*_de*")),
				List.of("txt")
		);

		assertEquals(2, resourcesLoader.getTranslationFiles().size());
	}

	@Test
	void test_setLocationPatternsPattern() {
		var resourcesLoader = new ResourcesLoader(
				Locale.forLanguageTag("en"),
				new HashSet<>(List.of("translations_en/*", "translations_de/*")),
				List.of("txt")
		);

		assertEquals(4, resourcesLoader.getTranslationFiles().size());
	}

	@Test
	void test_Record() {
		var resourcesLoader = new ResourcesLoader(
				Locale.forLanguageTag("en"),
				new HashSet<>(List.of("translations_en_US/*")),
				List.of("txt")
		);

		TranslationFile translationFile = resourcesLoader.getTranslationFiles().get(0);

		assertEquals("messages", translationFile.domain());
		assertEquals("en_US", translationFile.locale().toString());
		assertInstanceOf(InputStream.class, translationFile.inputStream());
	}

	@Test
	void test_parseFileName_null() {
		var resourcesLoader = new ResourcesLoader(
				Locale.forLanguageTag("en"),
				new HashSet<>(List.of("translations/.txt")),
				List.of("txt")
		);

		assertTrue(resourcesLoader.getTranslationFiles().isEmpty());
	}
}
