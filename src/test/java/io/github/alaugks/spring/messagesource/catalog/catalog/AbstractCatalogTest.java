// SPDX-License-Identifier: Apache-2.0
// Copyright 2024 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.catalog.catalog;

import io.github.alaugks.spring.messagesource.catalog.records.TransUnit;
import io.github.alaugks.spring.messagesource.catalog.records.TransUnitInterface;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AbstractCatalogTest {

	private static final Locale EN = Locale.forLanguageTag("en");

	@Test
	void test_defaults_contribute_nothing() {
		AbstractCatalog catalog = new NoopCatalog();

		assertTrue(catalog.getTransUnits().isEmpty());
		assertNull(catalog.resolveTransUnit("any_key", EN));
	}

	@Test
	void test_eager_override() {
		AbstractCatalog catalog = new EagerCatalog();

		assertEquals("eager_value", catalog.getTransUnits().get(0).value());
		assertNull(catalog.resolveTransUnit("any_key", EN));
	}

	@Test
	void test_lazy_override_answers_or_returns_null() {
		AbstractCatalog catalog = new LazyCatalog();

		assertEquals("lazy_value", catalog.resolveTransUnit("lazy_key", EN).value());
		assertNull(catalog.resolveTransUnit("unknown_key", EN));
		assertTrue(catalog.getTransUnits().isEmpty());
	}
}

class NoopCatalog extends AbstractCatalog {
}

class EagerCatalog extends AbstractCatalog {

	@Override
	public List<TransUnitInterface> getTransUnits() {
		return List.of(new TransUnit(Locale.forLanguageTag("en"), "eager_key", "eager_value"));
	}
}

class LazyCatalog extends AbstractCatalog {

	@Override
	public TransUnitInterface resolveTransUnit(String code, Locale locale) {
		if (code.equals("lazy_key")) {
			return new TransUnit(locale, code, "lazy_value");
		}

		return null;
	}
}
