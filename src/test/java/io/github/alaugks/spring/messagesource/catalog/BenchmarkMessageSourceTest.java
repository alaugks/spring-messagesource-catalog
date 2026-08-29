// SPDX-License-Identifier: Apache-2.0
// Copyright 2024 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.catalog;

import io.github.alaugks.spring.messagesource.catalog.catalog.CatalogInterface;
import io.github.alaugks.spring.messagesource.catalog.records.TransUnit;
import io.github.alaugks.spring.messagesource.catalog.records.TransUnitInterface;
import io.github.alaugks.spring.messagesource.catalog.records.TranslationFileInterface;
import io.github.alaugks.spring.messagesource.catalog.resources.ResourceLoaderBuilder;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * This test compares the logic when resolving the code of CatalogMessageSourceBuilder vs. ResourceBundleMessageSource
 * and ReloadableResourceBundleMessageSource. Behaviour must be equal.
 */
@SuppressWarnings({"java:S125"})
class BenchmarkMessageSourceTest {

	static CatalogMessageSourceBuilder catalogMessageSourceBuilder;

	static CatalogMessageSourceBuilder catalogMessageSourceBuilderICU4j;

	static ResourceBundleMessageSource resourceBundleMessageSource;

	static ReloadableResourceBundleMessageSource reloadableResourceBundleMessageSource;

	static Locale defaultLocale = Locale.forLanguageTag("en");

	@BeforeAll
	static void beforeAll() throws IOException {
		catalogMessageSourceBuilder = CatalogMessageSourceBuilder
				.builder(defaultLocale, createTransUnitListFromMessagesPropertiesFiles())
				.build();

		catalogMessageSourceBuilderICU4j = CatalogMessageSourceBuilder
				.builder(defaultLocale, createTransUnitListFromMessagesPropertiesFiles())
				.enableICU4j()
				.build();

		resourceBundleMessageSource = new ResourceBundleMessageSource();
		resourceBundleMessageSource.setBasename("messages/messages");
		resourceBundleMessageSource.setDefaultEncoding("UTF-8");
		resourceBundleMessageSource.setDefaultLocale(defaultLocale);

		reloadableResourceBundleMessageSource = new ReloadableResourceBundleMessageSource();
		reloadableResourceBundleMessageSource.setBasename("messages/messages");
		reloadableResourceBundleMessageSource.setDefaultEncoding("UTF-8");
		reloadableResourceBundleMessageSource.setDefaultLocale(defaultLocale);
	}

	@ParameterizedTest()
	@MethodSource("provider_examples")
	void test_catalog_message_source(String locale, String code, Object[] args, Object expected) {
		assertEquals(expected, catalogMessageSourceBuilder.getMessage(
				code,
				args,
				Locale.forLanguageTag(locale)
		));
	}

	@ParameterizedTest()
	@MethodSource("provider_examples_icu4j")
	void test_catalog_message_source_icu4j(String locale, String code, Object[] args, Object expected) {
		assertEquals(expected, catalogMessageSourceBuilderICU4j.getMessage(
			code,
			args,
			Locale.forLanguageTag(locale)
		));
	}

	@ParameterizedTest()
	@MethodSource("provider_examples")
	void test_resource_bundle_message_source(String locale, String code, Object[] args, Object expected) {
		assertEquals(expected, resourceBundleMessageSource.getMessage(
				this.concatCode(code),
				args,
				Locale.forLanguageTag(locale)
		));
	}

	@ParameterizedTest()
	@MethodSource("provider_examples")
	void test_reloadable_resource_bundle_message_source(String locale, String code, Object[] args, Object expected) {
		assertEquals(expected, reloadableResourceBundleMessageSource.getMessage(
				this.concatCode(code),
				args,
				Locale.forLanguageTag(locale)
		));
	}

	private static Stream<Arguments> provider_examples_icu4j() {
		return Stream.of(
				Arguments.of("de", "list_files_icu4j", new Object[] {Map.of("file_count", 10000L)}, "Sie haben 10.000 Dateien gelöscht.")
		);
	}

	private static Stream<Arguments> provider_examples() {
		return Stream.of(
				Arguments.of("en", "headline", null, "Headline (en)"),
				Arguments.of("en", "text", null, "Text (en)"),
				Arguments.of("en", "notice", null, "Notice (en)"),
				Arguments.of("en", "list_files", new Object[] {10000}, "There are 10,000 files."),

				Arguments.of("de", "headline", null, "Headline (de)"),
				Arguments.of("de", "text", null, "Text (de)"),
				Arguments.of("de", "notice", null, "Notice (en)"),
				Arguments.of("de", "list_files", new Object[] {10000}, "Es gibt 10.000 Dateien."),

				Arguments.of("en-US", "headline", null, "Headline (en)"),
				Arguments.of("en-US", "text", null, "Text (en-US)"),
				Arguments.of("en-US", "notice", null, "Notice (en)"),

				Arguments.of("es", "headline", null, "Headline (es)"),
				Arguments.of("es", "text", null, "Text (es)"),
				Arguments.of("es", "notice", null, "Notice (en)"),

				Arguments.of("es-CR", "headline", null, "Headline (es-CR)"),
				Arguments.of("es-CR", "text", null, "Text (es)"),
				Arguments.of("es-CR", "notice", null, "Notice (en)"),

				Arguments.of("jp", "headline", null, "Headline (en)"),
				Arguments.of("jp", "list_files", new Object[] {10000}, "There are 10,000 files.")
		);
	}

	private static CatalogInterface createTransUnitListFromMessagesPropertiesFiles() throws IOException {
		List<TransUnitInterface> transUnits = new ArrayList<>();

		ResourceLoaderBuilder resourcesLoader = ResourceLoaderBuilder
				.builder(Locale.forLanguageTag("en"), List.of("messages/messages*"))
				.fileExtensions(List.of("properties"))
				.build();

		for (TranslationFileInterface translationFile : resourcesLoader.getTranslationFiles()) {
			Properties properties = new Properties();
			properties.load(new InputStreamReader(
					new ByteArrayInputStream(translationFile.content()),
					StandardCharsets.UTF_8
			));
			for (Entry<Object, Object> property : properties.entrySet()) {

				transUnits.add(new TransUnit(
						translationFile.locale(),
						property.getKey().toString(),
						property.getValue().toString()
				));
			}
		}

		return new TransUnitsCatalog(transUnits);
	}

	private String concatCode(String code) {
		if (!code.startsWith("") && !code.startsWith("payment.")) {
			code = "" + code;
		}

		return code;
	}
}
