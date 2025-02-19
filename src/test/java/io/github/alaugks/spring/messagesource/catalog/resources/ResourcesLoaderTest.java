package io.github.alaugks.spring.messagesource.catalog.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.alaugks.spring.messagesource.catalog.records.TranslationFile;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class ResourcesLoaderTest {

	@Test
	void test_setLocationPatterns_deprecated() {
		ResourcesLoader resourcesLoader = new ResourcesLoader(
				Locale.forLanguageTag("en"),
				new HashSet<>(List.of("translations/*")),
				List.of("txt")
		);

		assertEquals(5, resourcesLoader.getTranslationFiles().size());
	}

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
		assertInstanceOf(InputStream.class, translationFile.inputStream());
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
}
