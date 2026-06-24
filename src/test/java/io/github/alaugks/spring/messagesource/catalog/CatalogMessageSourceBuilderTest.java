// SPDX-License-Identifier: Apache-2.0
// Copyright 2024 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.catalog;

import io.github.alaugks.spring.messagesource.catalog.fxitures.FooBarCatalog;
import io.github.alaugks.spring.messagesource.catalog.fxitures.ParentMessageSource;
import io.github.alaugks.spring.messagesource.catalog.records.TransUnit;
import io.github.alaugks.spring.messagesource.catalog.records.TransUnitInterface;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.DefaultMessageSourceResolvable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings({"java:S4144"})
class CatalogMessageSourceBuilderTest {

	static final Locale LOCALE_EN = Locale.forLanguageTag("en");
	static final Locale LOCALE_DE = Locale.forLanguageTag("de");
	static final Locale LOCALE_EN_US = Locale.forLanguageTag("en-US");
	
	@Test
	void test_resolution_message_format() {
		List<TransUnitInterface> transUnits = List.of(
			new TransUnit(LOCALE_EN, "list_files", "Es gibt {0,choice,0#keine Datei|1#eine Datei|1<{0,number,integer} Dateien}"),
			new TransUnit(LOCALE_DE, "list_files", "Es gibt {0,choice,0#keine Datei|1#eine Datei|1<{0,number,integer} Dateien}")
		);

		CatalogMessageSourceBuilder ms = CatalogMessageSourceBuilder
			.builder(transUnits, LOCALE_EN)
			.build();

		assertEquals("Es gibt 10,000 Dateien", ms.getMessage("list_files", new Object[] {10000}, "", LOCALE_EN));
		assertEquals("Es gibt 10.000 Dateien", ms.getMessage("list_files", new Object[] {10000}, "", LOCALE_DE));
		assertEquals("Es gibt 10,000 Dateien", ms.getMessage("list_files", new Object[] {10000}, "", LOCALE_EN_US));
	}

	// README "Message formatting": default java.text.MessageFormat with a positional argument.
	@Test
	void test_readme_default_format_java_text_messageformat_argument() {
		List<TransUnitInterface> transUnits = List.of(
			new TransUnit(LOCALE_EN, "files", "There are {0,number,integer} files."),
			new TransUnit(LOCALE_DE, "files", "Es gibt {0,number,integer} Dateien.")
		);

		CatalogMessageSourceBuilder ms = CatalogMessageSourceBuilder
			.builder(transUnits, LOCALE_EN)
			.build();

		assertEquals(
			"Es gibt 10.000 Dateien.",
			ms.getMessage("files", new Object[] {10000}, LOCALE_DE)
		);
	}

	@Test
	void test_readme_icu4j_plural() {
		List<TransUnitInterface> transUnits = List.of(
			new TransUnit(LOCALE_EN, "file_deleted",
				"{count, plural, =0 {You deleted no files.} =1 {You deleted one file.} other {You deleted {count} files.}}"),
			new TransUnit(LOCALE_DE, "file_deleted",
				"{count, plural, =0 {Sie haben keine Dateien gelöscht.} =1 {Sie haben eine Datei gelöscht.} other {Sie haben {count} Dateien gelöscht.}}")
		);

		CatalogMessageSourceBuilder ms = CatalogMessageSourceBuilder
			.builder(transUnits, LOCALE_EN)
			.enableICU4j()
			.build();

		assertEquals(
			"Sie haben 1.000 Dateien gelöscht.",
			ms.getMessage("file_deleted", new Object[] {Map.of("count", 1000)}, LOCALE_DE)
		);
	}

	@Test
	void test_set_use_icu4j_true_enables_icu4j() {
		List<TransUnitInterface> transUnits = List.of(
			new TransUnit(LOCALE_EN, "file_deleted",
				"{count, plural, =0 {You deleted no files.} =1 {You deleted one file.} other {You deleted {count} files.}}"),
			new TransUnit(LOCALE_DE, "file_deleted",
				"{count, plural, =0 {Sie haben keine Dateien gelöscht.} =1 {Sie haben eine Datei gelöscht.} other {Sie haben {count} Dateien gelöscht.}}")
		);

		CatalogMessageSourceBuilder ms = CatalogMessageSourceBuilder
			.builder(transUnits, LOCALE_EN)
			.setUseICU4j(true)
			.build();

		assertEquals(
			"Sie haben 1.000 Dateien gelöscht.",
			ms.getMessage("file_deleted", new Object[] {Map.of("count", 1000)}, LOCALE_DE)
		);
	}

	@Test
	void test_readme_icu4j_select() {
		List<TransUnitInterface> transUnits = List.of(
			new TransUnit(LOCALE_EN, "greeting",
				"{recipient_gender, select, feminine {How is she?} masculine {How is he?} other {How are they?}}"),
			new TransUnit(LOCALE_DE, "greeting",
				"{recipient_gender, select, feminine {Wie geht''s ihr?} masculine {Wie geht''s ihm?} other {Wie geht''s ihnen?}}")
		);

		CatalogMessageSourceBuilder ms = CatalogMessageSourceBuilder
			.builder(transUnits, LOCALE_EN)
			.enableICU4j()
			.build();

		assertEquals(
			"Wie geht's ihr?",
			ms.getMessage("greeting", new Object[] {Map.of("recipient_gender", "feminine")}, LOCALE_DE)
		);
	}

	@Test
	void test_builder_with_list() {
		List<TransUnitInterface> transUnits = List.of(
				new TransUnit(LOCALE_EN, "code", "messages_value")
		);

		assertEquals(
				"messages_value",
				CatalogMessageSourceBuilder
						.builder(transUnits, LOCALE_EN)
						.build().getMessage("code", null, LOCALE_EN)
		);
	}

	@Test
	void test_builder_with_catalog_interface() {
		List<TransUnitInterface> transUnits = List.of(
				new TransUnit(LOCALE_EN, "code", "messages_value")
		);

		assertEquals(
				"messages_value",
				CatalogMessageSourceBuilder
						.builder(new TransUnitsCatalog(transUnits), LOCALE_EN)
						.build()
						.getMessage("code", null, LOCALE_EN)
		);
	}

	@Test
	void test_with_set_default_domain() {
		List<TransUnitInterface> transUnits = List.of(
				new TransUnit(LOCALE_EN, "code", "messages_value"),
				new TransUnit(LOCALE_EN, "code", "foo_value", "foo")
		);

		assertEquals(
				"foo_value",
				CatalogMessageSourceBuilder
						.builder(new TransUnitsCatalog(transUnits), LOCALE_EN)
						.defaultDomain("foo")
						.build()
						.getMessage("code", null, LOCALE_EN)
		);
	}

	@Test
	void test_resolution_multi_domain() {
		List<TransUnitInterface> transUnits = List.of(
				new TransUnit(LOCALE_EN, "code_a", "value_en_a"),
				new TransUnit(LOCALE_EN, "code_b", "value_en_b"),
				new TransUnit(LOCALE_DE, "code_a", "value_de_1"),
				new TransUnit(LOCALE_DE, "code_b", "value_de_2"),
				new TransUnit(LOCALE_EN, "code_a", "value_en_a", "foobar"),
				new TransUnit(LOCALE_EN, "code_b", "value_en_b", "foobar"),
				new TransUnit(LOCALE_EN_US, "code_a", "value_en_us_1", "messages")
		);

		CatalogMessageSourceBuilder ms = CatalogMessageSourceBuilder
				.builder(transUnits, LOCALE_EN)
				.build();

		assertEquals("value_en_a", ms.getMessage("code_a", null, LOCALE_EN));
		assertEquals("value_en_a", ms.getMessage("messages.code_a", null, LOCALE_EN));
		assertEquals("value_en_b", ms.getMessage("messages.code_b", null, LOCALE_EN));
		assertEquals("value_en_a", ms.getMessage("foobar.code_a", null, LOCALE_EN));
		assertEquals("value_en_b", ms.getMessage("foobar.code_b", null, LOCALE_EN));

		assertEquals("value_de_1", ms.getMessage("messages.code_a", null, LOCALE_DE));
		assertEquals("value_de_2", ms.getMessage("messages.code_b", null, LOCALE_DE));

		assertEquals("value_en_us_1", ms.getMessage("messages.code_a", null, LOCALE_EN_US));
	}

	@Test
	void test_add_source_catalog_interface() {
		List<TransUnitInterface> first = List.of(
				new TransUnit(LOCALE_EN, "code_a", "value_a")
		);
		List<TransUnitInterface> second = List.of(
				new TransUnit(LOCALE_EN, "code_b", "value_b")
		);

		CatalogMessageSourceBuilder ms = CatalogMessageSourceBuilder
				.builder(first, LOCALE_EN)
				.addSource(new TransUnitsCatalog(second))
				.build();

		assertEquals("value_a", ms.getMessage("code_a", null, LOCALE_EN));
		assertEquals("value_b", ms.getMessage("code_b", null, LOCALE_EN));
	}

	@Test
	void test_add_source_trans_units_list() {
		List<TransUnitInterface> first = List.of(
				new TransUnit(LOCALE_EN, "code_a", "value_a")
		);
		List<TransUnitInterface> second = List.of(
				new TransUnit(LOCALE_EN, "code_b", "value_b")
		);

		CatalogMessageSourceBuilder ms = CatalogMessageSourceBuilder
				.builder(first, LOCALE_EN)
				.addSource(second)
				.build();

		assertEquals("value_a", ms.getMessage("code_a", null, LOCALE_EN));
		assertEquals("value_b", ms.getMessage("code_b", null, LOCALE_EN));
	}

	@Test
	void test_add_source_multiple_append_in_order() {
		List<TransUnitInterface> first = List.of(
				new TransUnit(LOCALE_EN, "code_a", "value_a")
		);

		CatalogMessageSourceBuilder ms = CatalogMessageSourceBuilder
				.builder(first, LOCALE_EN)
				.addSource(List.of(new TransUnit(LOCALE_EN, "code_b", "value_b")))
				.addSource(new FooBarCatalog())
				.build();

		assertEquals("value_a", ms.getMessage("code_a", null, LOCALE_EN));
		assertEquals("value_b", ms.getMessage("code_b", null, LOCALE_EN));
		assertEquals("foobar_value", ms.getMessage("dummy", null, LOCALE_EN));
	}

	@Test
	void test_resolution_chain_fallback() {
		List<TransUnitInterface> transUnits = List.of(
				new TransUnit(LOCALE_EN, "code_a", "value_en_a")
		);

		CatalogMessageSourceBuilder ms = CatalogMessageSourceBuilder
				.builder(new TransUnitsCatalog(transUnits), LOCALE_EN)
				.addSource(new FooBarCatalog())
				.build();

		assertEquals("value_en_a", ms.getMessage("code_a", null, LOCALE_EN));
		assertEquals("foobar_value", ms.getMessage("dummy", null, LOCALE_EN));
		assertEquals("foobar_value", ms.getMessage("messages.dummy", null, LOCALE_EN));
	}

	@Test
	void test_resolution_not_resolved() {
		List<TransUnitInterface> transUnits = List.of(
				new TransUnit(LOCALE_EN, "code_a", "value_en_a")
		);

		CatalogMessageSourceBuilder ms = CatalogMessageSourceBuilder
				.builder(transUnits, LOCALE_EN)
				.build();

		assertThrows(NoSuchMessageException.class,
				() -> ms.getMessage("not_exists", null, LOCALE_EN));
		assertThrows(NoSuchMessageException.class,
				() -> ms.getMessage("messages.not_exists", null, LOCALE_EN));
	}

	@Test
	void test_get_message_returns_default_message_when_code_not_resolved() {
		List<TransUnitInterface> transUnits = List.of(
				new TransUnit(LOCALE_EN, "code_a", "value_en_a")
		);

		CatalogMessageSourceBuilder ms = CatalogMessageSourceBuilder
				.builder(transUnits, LOCALE_EN)
				.build();

		assertEquals(
				"default_message",
				ms.getMessage("not_exists", null, "default_message", LOCALE_EN)
		);
		assertEquals(
				"default_message",
				ms.getMessage("messages.not_exists", new Object[] {}, "default_message", LOCALE_EN)
		);
	}

	@Test
	void test_resolvable_resolves_first_code() {
		List<TransUnitInterface> transUnits = List.of(
				new TransUnit(LOCALE_EN, "code_a", "value_en_a")
		);

		CatalogMessageSourceBuilder ms = CatalogMessageSourceBuilder
				.builder(transUnits, LOCALE_EN)
				.build();

		DefaultMessageSourceResolvable resolvable =
				new DefaultMessageSourceResolvable(new String[] {"code_a"});

		assertEquals("value_en_a", ms.getMessage(resolvable, LOCALE_EN));
	}

	@Test
	void test_resolvable_falls_back_to_next_code() {
		List<TransUnitInterface> transUnits = List.of(
				new TransUnit(LOCALE_EN, "code_b", "value_en_b")
		);

		CatalogMessageSourceBuilder ms = CatalogMessageSourceBuilder
				.builder(transUnits, LOCALE_EN)
				.build();

		DefaultMessageSourceResolvable resolvable =
				new DefaultMessageSourceResolvable(new String[] {"not_exists", "code_b"});

		assertEquals("value_en_b", ms.getMessage(resolvable, LOCALE_EN));
	}

	@Test
	void test_resolvable_applies_arguments() {
		List<TransUnitInterface> transUnits = List.of(
				new TransUnit(LOCALE_EN, "files", "There are {0,number,integer} files.")
		);

		CatalogMessageSourceBuilder ms = CatalogMessageSourceBuilder
				.builder(transUnits, LOCALE_EN)
				.build();

		DefaultMessageSourceResolvable resolvable =
				new DefaultMessageSourceResolvable(new String[] {"files"}, new Object[] {10000});

		assertEquals("There are 10,000 files.", ms.getMessage(resolvable, LOCALE_EN));
	}

	@Test
	void test_resolvable_not_resolved_with_locale_throws() {
		List<TransUnitInterface> transUnits = List.of(
				new TransUnit(LOCALE_EN, "code_a", "value_en_a")
		);

		CatalogMessageSourceBuilder ms = CatalogMessageSourceBuilder
				.builder(transUnits, LOCALE_EN)
				.build();

		DefaultMessageSourceResolvable resolvable =
				new DefaultMessageSourceResolvable(new String[] {"not_exists"});

		assertThrows(NoSuchMessageException.class, () -> ms.getMessage(resolvable, LOCALE_EN));
	}

	@Test
	void test_resolvable_not_resolved_null_locale_throws() {
		List<TransUnitInterface> transUnits = List.of(
				new TransUnit(LOCALE_EN, "code_a", "value_en_a")
		);

		CatalogMessageSourceBuilder ms = CatalogMessageSourceBuilder
				.builder(transUnits, LOCALE_EN)
				.build();

		DefaultMessageSourceResolvable resolvable =
				new DefaultMessageSourceResolvable(new String[] {"not_exists"});

		assertThrows(NoSuchMessageException.class, () -> ms.getMessage(resolvable, null));
	}

	@Test
	void test_resolvable_null_codes_with_locale_throws() {
		List<TransUnitInterface> transUnits = List.of(
				new TransUnit(LOCALE_EN, "code_a", "value_en_a")
		);

		CatalogMessageSourceBuilder ms = CatalogMessageSourceBuilder
				.builder(transUnits, LOCALE_EN)
				.build();

		MessageSourceResolvable resolvable = new MessageSourceResolvable() {
			@Override
			public String[] getCodes() {
				return null;
			}

			@Override
			public String getDefaultMessage() {
				return null;
			}
		};

		assertThrows(NoSuchMessageException.class, () -> ms.getMessage(resolvable, LOCALE_EN));
	}

	@Test
	void test_resolvable_empty_codes_null_locale_throws() {
		List<TransUnitInterface> transUnits = List.of(
				new TransUnit(LOCALE_EN, "code_a", "value_en_a")
		);

		CatalogMessageSourceBuilder ms = CatalogMessageSourceBuilder
				.builder(transUnits, LOCALE_EN)
				.build();

		DefaultMessageSourceResolvable resolvable =
				new DefaultMessageSourceResolvable(new String[] {});

		assertThrows(NoSuchMessageException.class, () -> ms.getMessage(resolvable, null));
	}

	@Test
	void test_empty_args_returns_raw_value_icu4j_named_pattern() {
		List<TransUnitInterface> transUnits = List.of(
				new TransUnit(LOCALE_EN, "greeting", "There are {count} files")
		);

		CatalogMessageSourceBuilder ms = CatalogMessageSourceBuilder
				.builder(transUnits, LOCALE_EN)
				.enableICU4j()
				.build();

		assertEquals(
				"There are {count} files",
				ms.getMessage("greeting", new Object[] {}, LOCALE_EN)
		);
	}

	@Test
	void test_get_message_returns_null_default_message_when_code_not_resolved() {
		List<TransUnitInterface> transUnits = List.of(
				new TransUnit(LOCALE_EN, "code_a", "value_en_a")
		);

		CatalogMessageSourceBuilder ms = CatalogMessageSourceBuilder
				.builder(transUnits, LOCALE_EN)
				.build();

		assertNull(ms.getMessage("not_exists", null, null, LOCALE_EN));
	}

	@Test
	void test_resolvable_returns_resolvable_default_message_when_not_resolved() {
		List<TransUnitInterface> transUnits = List.of(
				new TransUnit(LOCALE_EN, "code_a", "value_en_a")
		);

		CatalogMessageSourceBuilder ms = CatalogMessageSourceBuilder
				.builder(transUnits, LOCALE_EN)
				.build();

		DefaultMessageSourceResolvable resolvable =
				new DefaultMessageSourceResolvable(new String[] {"not_exists"}, null, "default_message");

		assertEquals("default_message", ms.getMessage(resolvable, LOCALE_EN));
	}

	@Test
	void test_get_message_null_locale_not_resolved_throws() {
		List<TransUnitInterface> transUnits = List.of(
				new TransUnit(LOCALE_EN, "code_a", "value_en_a")
		);

		CatalogMessageSourceBuilder ms = CatalogMessageSourceBuilder
				.builder(transUnits, LOCALE_EN)
				.build();

		assertThrows(NoSuchMessageException.class, () -> ms.getMessage("not_exists", null, null));
	}

	@Test
	void test_get_message_null_code_returns_default_message() {
		List<TransUnitInterface> transUnits = List.of(
				new TransUnit(LOCALE_EN, "code_a", "value_en_a")
		);

		CatalogMessageSourceBuilder ms = CatalogMessageSourceBuilder
				.builder(transUnits, LOCALE_EN)
				.build();

		assertEquals("default_message", ms.getMessage(null, null, "default_message", LOCALE_EN));
	}

	@Test
	void test_get_message_unresolved_with_args_returns_default_message() {
		List<TransUnitInterface> transUnits = List.of(
				new TransUnit(LOCALE_EN, "code_a", "value_en_a")
		);

		CatalogMessageSourceBuilder ms = CatalogMessageSourceBuilder
				.builder(transUnits, LOCALE_EN)
				.build();

		assertEquals("default_message", ms.getMessage("not_exists", new Object[] {"arg"}, "default_message", LOCALE_EN));
	}

	@Test
	void test_icu4j_single_java_text_messageformat_argument() {
		List<TransUnitInterface> transUnits = List.of(
				new TransUnit(LOCALE_EN, "value", "Foo {0}")
		);

		CatalogMessageSourceBuilder ms = CatalogMessageSourceBuilder
				.builder(transUnits, LOCALE_EN)
				.enableICU4j()
				.build();

		assertEquals("Foo Bar", ms.getMessage("value", new Object[] {"Bar"}, LOCALE_EN));
	}

	@Test
	void test_icu4j_multiple_java_text_messageformat_arguments() {
		List<TransUnitInterface> transUnits = List.of(
				new TransUnit(LOCALE_EN, "value", "{0} and {1}")
		);

		CatalogMessageSourceBuilder ms = CatalogMessageSourceBuilder
				.builder(transUnits, LOCALE_EN)
				.enableICU4j()
				.build();

		assertEquals("Foo and Bar", ms.getMessage("value", new Object[] {"Foo", "Bar"}, LOCALE_EN));
	}

	@Test
	void test_empty_code_not_resolved_throws() {
		List<TransUnitInterface> transUnits = List.of(
				new TransUnit(LOCALE_EN, "code_a", "value_en_a")
		);

		CatalogMessageSourceBuilder ms = CatalogMessageSourceBuilder
				.builder(transUnits, LOCALE_EN)
				.build();

		assertThrows(NoSuchMessageException.class, () -> ms.getMessage("", null, LOCALE_EN));
	}

	@Test
	void test_empty_language_locale_not_resolved_throws() {
		List<TransUnitInterface> transUnits = List.of(
				new TransUnit(LOCALE_EN, "code_a", "value_en_a")
		);

		CatalogMessageSourceBuilder ms = CatalogMessageSourceBuilder
				.builder(transUnits, LOCALE_EN)
				.build();

		assertThrows(NoSuchMessageException.class, () -> ms.getMessage("code_a", null, Locale.ROOT));
	}

	@Test
	void test_source_with_empty_language_locale_is_skipped() {
		List<TransUnitInterface> transUnits = List.of(
				new TransUnit(LOCALE_EN, "code_a", "value_en_a"),
				new TransUnit(Locale.ROOT, "code_root", "value_root")
		);

		CatalogMessageSourceBuilder ms = CatalogMessageSourceBuilder
				.builder(transUnits, LOCALE_EN)
				.build();

		assertEquals("value_en_a", ms.getMessage("code_a", null, LOCALE_EN));
		assertThrows(NoSuchMessageException.class, () -> ms.getMessage("code_root", null, LOCALE_EN));
	}

	@Test
	void test_resolution_falls_back_to_default_locale() {
		List<TransUnitInterface> transUnits = List.of(
				new TransUnit(LOCALE_EN, "code_a", "value_en_a")
		);

		CatalogMessageSourceBuilder ms = CatalogMessageSourceBuilder
				.builder(transUnits, LOCALE_EN)
				.build();

		assertEquals("value_en_a", ms.getMessage("code_a", null, Locale.forLanguageTag("fr-FR")));
	}

	@Test
	void test_resolution_default_locale_fallback_misses_throws() {
		List<TransUnitInterface> transUnits = List.of(
				new TransUnit(LOCALE_EN, "code_a", "value_en_a")
		);

		CatalogMessageSourceBuilder ms = CatalogMessageSourceBuilder
				.builder(transUnits, LOCALE_EN)
				.build();

		Locale locale = Locale.forLanguageTag("fr-FR");

		assertThrows(NoSuchMessageException.class, () -> ms.getMessage("not_exists", null, locale));
	}

	@Test
	void test_parent_message_source() {
		List<TransUnitInterface> transUnits = List.of(
			new TransUnit(LOCALE_EN, "code_a", "value_en_a")
		);

		CatalogMessageSourceBuilder ms = CatalogMessageSourceBuilder
			.builder(transUnits, LOCALE_EN)
			.parentMessageSource(new ParentMessageSource())
			.build();

		assertEquals("value_en_a", ms.getMessage("code_a", null, LOCALE_EN));
		assertEquals("ParentMessageSource with args: 1,234", ms.getMessage("parent-messagesource-code", new Object[]{1234}, LOCALE_EN));
		assertThrows(NoSuchMessageException.class, () -> ms.getMessage("not_exists", null, LOCALE_EN));
	}

	@Test
	void test_domain_divider() {
		List<TransUnitInterface> transUnits = List.of(
			new TransUnit(LOCALE_EN, "code_a", "value_en_a", "domain")
		);

		CatalogMessageSourceBuilder ms = CatalogMessageSourceBuilder
			.builder(transUnits, LOCALE_EN)
			.domainDivider("|")
			.build();

		assertEquals("value_en_a", ms.getMessage("domain|code_a", null, LOCALE_EN));
	}
}
