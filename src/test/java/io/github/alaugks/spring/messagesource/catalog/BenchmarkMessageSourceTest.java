package io.github.alaugks.spring.messagesource.catalog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Stream;

import io.github.alaugks.spring.messagesource.catalog.catalog.CatalogInterface;
import io.github.alaugks.spring.messagesource.catalog.records.TransUnit;
import io.github.alaugks.spring.messagesource.catalog.records.TransUnitInterface;
import io.github.alaugks.spring.messagesource.catalog.records.TranslationFile;
import io.github.alaugks.spring.messagesource.catalog.resources.ResourcesLoader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
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

	static ResourceBundleMessageSource resourceBundleMessageSource;

	static ReloadableResourceBundleMessageSource reloadableResourceBundleMessageSource;

	static Locale defaultLocale = Locale.forLanguageTag("en");

	@BeforeAll
	static void beforeAll() throws IOException {
		catalogMessageSourceBuilder = CatalogMessageSourceBuilder
				.builder(createTransUnitListFromMessagesPropertiesFiles(), defaultLocale)
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
	@MethodSource("dataProvider_examples")
	void test_CatalogMessageSource(String locale, String code, Object[] args, Object expected) {
		assertEquals(expected, catalogMessageSourceBuilder.getMessage(
				code,
				args,
				Locale.forLanguageTag(locale)
		));
	}

	@Test
	void test_CatalogMessageSource_abstractMethodSetter() {
		var messageSource = CatalogMessageSourceBuilder
				.builder(new TransUnitsCatalog(new ArrayList<>()), defaultLocale)
				.build();

		messageSource.setParentMessageSource(new ParentMessageSource());
		messageSource.setAlwaysUseMessageFormat(true);

		assertEquals("Parent MessageSource", messageSource.getMessage(
				"code",
				new Object[] {},
				Locale.forLanguageTag("en")
		));
	}

	@ParameterizedTest()
	@MethodSource("dataProvider_examples")
	void test_ResourceBundleMessageSource(String locale, String code, Object[] args, Object expected) {
		assertEquals(expected, resourceBundleMessageSource.getMessage(
				this.concatCode(code),
				args,
				Locale.forLanguageTag(locale)
		));
	}

	@ParameterizedTest()
	@MethodSource("dataProvider_examples")
	void test_ReloadableResourceBundleMessageSource(String locale, String code, Object[] args, Object expected) {
		assertEquals(expected, reloadableResourceBundleMessageSource.getMessage(
				this.concatCode(code),
				args,
				Locale.forLanguageTag(locale)
		));
	}

	private static Stream<Arguments> dataProvider_examples() {
		return Stream.of(
				Arguments.of("en", "headline", null, "Headline (en)"),
				Arguments.of("en", "messages.headline", null, "Headline (en)"),
				Arguments.of("en", "text", null, "Text (en)"),
				Arguments.of("en", "messages.text", null, "Text (en)"),
				Arguments.of("en", "notice", null, "Notice (en)"),
				Arguments.of("en", "messages.notice", null, "Notice (en)"),
				Arguments.of("en", "payment.headline", null, "Payment (en)"),
				Arguments.of("en", "payment.text", null, "Payment Text (en)"),
				Arguments.of("en", "list_files", new Object[] {10000}, "There are 10,000 files."),
				Arguments.of("en", "messages.list_files", new Object[] {10000}, "There are 10,000 files."),

				Arguments.of("de", "headline", null, "Headline (de)"),
				Arguments.of("de", "messages.headline", null, "Headline (de)"),
				Arguments.of("de", "text", null, "Text (de)"),
				Arguments.of("de", "messages.text", null, "Text (de)"),
				Arguments.of("de", "notice", null, "Notice (en)"),
				Arguments.of("de", "messages.notice", null, "Notice (en)"),
				Arguments.of("de", "payment.headline", null, "Payment (de)"),
				Arguments.of("de", "payment.text", null, "Payment Text (de)"),
				Arguments.of("de", "list_files", new Object[] {10000}, "Es gibt 10.000 Dateien."),
				Arguments.of("de", "messages.list_files", new Object[] {10000}, "Es gibt 10.000 Dateien."),

				Arguments.of("en-US", "headline", null, "Headline (en)"),
				Arguments.of("en-US", "messages.headline", null, "Headline (en)"),
				Arguments.of("en-US", "text", null, "Text (en-US)"),
				Arguments.of("en-US", "messages.text", null, "Text (en-US)"),
				Arguments.of("en-US", "notice", null, "Notice (en)"),
				Arguments.of("en-US", "messages.notice", null, "Notice (en)"),
				Arguments.of("en-US", "payment.headline", null, "Payment (en-US)"),
				Arguments.of("en-US", "payment.text", null, "Payment Text (en)"),

				Arguments.of("es", "headline", null, "Headline (es)"),
				Arguments.of("es", "messages.headline", null, "Headline (es)"),
				Arguments.of("es", "text", null, "Text (es)"),
				Arguments.of("es", "messages.text", null, "Text (es)"),
				Arguments.of("es", "notice", null, "Notice (en)"),
				Arguments.of("es", "messages.notice", null, "Notice (en)"),
				Arguments.of("es", "payment.headline", null, "Payment (es)"),
				Arguments.of("es", "payment.text", null, "Payment Text (es)"),

				Arguments.of("es-CR", "headline", null, "Headline (es-CR)"),
				Arguments.of("es-CR", "messages.headline", null, "Headline (es-CR)"),
				Arguments.of("es-CR", "text", null, "Text (es)"),
				Arguments.of("es-CR", "messages.text", null, "Text (es)"),
				Arguments.of("es-CR", "notice", null, "Notice (en)"),
				Arguments.of("es-CR", "messages.notice", null, "Notice (en)"),
				Arguments.of("es-CR", "payment.headline", null, "Payment (es-CR)"),
				Arguments.of("es-CR", "payment.text", null, "Payment Text (es)"),

				Arguments.of("jp", "headline", null, "Headline (en)"),
				Arguments.of("jp", "messages.headline", null, "Headline (en)"),
				Arguments.of("jp", "payment.headline", null, "Payment (en)"),
				Arguments.of("jp", "payment.text", null, "Payment Text (en)"),
				Arguments.of("jp", "list_files", new Object[] {10000}, "There are 10,000 files."),
				Arguments.of("jp", "messages.list_files", new Object[] {10000}, "There are 10,000 files.")
		);
	}

	private static CatalogInterface createTransUnitListFromMessagesPropertiesFiles() throws IOException {
		List<TransUnitInterface> transUnits = new ArrayList<>();

		var resourcesLoader = new ResourcesLoader(
				Locale.forLanguageTag("en"),
				new HashSet<>(List.of("messages/messages*")),
				List.of("properties")
		);

		for (TranslationFile translationFile : resourcesLoader.getTranslationFiles()) {
			Properties properties = new Properties();
			properties.load(translationFile.inputStream());
			for (Entry<Object, Object> property : properties.entrySet()) {

				var key = property.getKey();
				var posDomainDelimiter = key.toString().lastIndexOf(".");

				transUnits.add(new TransUnit(
						translationFile.locale(),
						key.toString().substring(
								posDomainDelimiter + 1
						),
						property.getValue().toString(),
						key.toString().substring(
								0,
								posDomainDelimiter
						)
				));
			}
		}

		return new TransUnitsCatalog(transUnits);
	}

	private String concatCode(String code) {
		if (!code.startsWith("messages.") && !code.startsWith("payment.")) {
			code = "messages." + code;
		}

		return code;
	}

	static class ParentMessageSource implements MessageSource {

		@Override
		public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
			return "Parent MessageSource";
		}

		@Override
		public String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {
			return "Parent MessageSource";
		}

		@Override
		public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
			return "Parent MessageSource";
		}
	}
}
