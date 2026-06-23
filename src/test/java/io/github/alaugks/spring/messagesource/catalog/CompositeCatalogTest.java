// SPDX-License-Identifier: Apache-2.0
// Copyright 2024 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.catalog;

import io.github.alaugks.spring.messagesource.catalog.catalog.AbstractCatalog;
import io.github.alaugks.spring.messagesource.catalog.records.TransUnit;
import io.github.alaugks.spring.messagesource.catalog.records.TransUnitInterface;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CompositeCatalogTest {

	private static final Locale EN = Locale.forLanguageTag("en");

	@Test
	void test_get_trans_units_concatenated_in_order() {
		CompositeCatalog composite = new CompositeCatalog(List.of(
				new TransUnitsCatalog(List.of(new TransUnit(EN, "code_a", "value_a"))),
				new TransUnitsCatalog(List.of(new TransUnit(EN, "code_b", "value_b")))
		));

		List<TransUnitInterface> all = composite.getTransUnits();

		assertEquals(2, all.size());
		assertEquals("value_a", all.get(0).value());
		assertEquals("value_b", all.get(1).value());
	}

	@Test
	void test_resolve_first_non_null_wins() {
		CompositeCatalog composite = new CompositeCatalog(List.of(
				new KeyCatalog("code", "first"),
				new KeyCatalog("code", "second")
		));

		assertEquals("first", composite.resolveTransUnit("code", EN).value());
	}

	@Test
	void test_resolve_falls_through_to_next_source() {
		CompositeCatalog composite = new CompositeCatalog(List.of(
				new KeyCatalog("code_a", "value_a"),
				new KeyCatalog("code_b", "value_b")
		));

		assertEquals("value_b", composite.resolveTransUnit("code_b", EN).value());
	}

	@Test
	void test_resolve_returns_null_when_no_source_answers() {
		CompositeCatalog composite = new CompositeCatalog(List.of(
				new KeyCatalog("code_a", "value_a")
		));

		assertNull(composite.resolveTransUnit("unknown", EN));
	}
}

class KeyCatalog extends AbstractCatalog {

	private final String code;

	private final String value;

	KeyCatalog(String code, String value) {
		this.code = code;
		this.value = value;
	}

	@Override
	public TransUnitInterface resolveTransUnit(String code, Locale locale) {
		return code.equals(this.code) ? new TransUnit(locale, code, this.value) : null;
	}
}
