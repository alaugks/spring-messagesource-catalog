// SPDX-License-Identifier: Apache-2.0
// Copyright 2024 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.catalog;

import io.github.alaugks.spring.messagesource.catalog.fxitures.TransUnitDomain;
import io.github.alaugks.spring.messagesource.catalog.records.TransUnit;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DomainCodeTransUnitHandlerTest {

	private static final Locale EN = Locale.forLanguageTag("en");

	@Test
	void test_put_transunit_without_domain_is_stored_under_default_domain_code() {
		DomainCodeTransUnitHandler handler = new DomainCodeTransUnitHandler("foobar");
		ConcurrentMap<Locale, ConcurrentMap<String, String>> catalogMap = new ConcurrentHashMap<>();

		handler.put(catalogMap, new TransUnit(EN, "headline", "Headline (en)"));

		Map<String, String> bucket = catalogMap.get(EN);
		assertEquals("Headline (en)", bucket.get("messages.headline"));
		assertFalse(bucket.containsKey("headline"));
	}

	@Test
	void test_constructor_rejects_null_default_domain() {
		assertThrows(IllegalArgumentException.class, () -> new DomainCodeTransUnitHandler(null));
	}

	@Test
	void test_constructor_rejects_null_default_domain_with_custom_domain_code_concat() {
		assertThrows(IllegalArgumentException.class, () -> new DomainCodeTransUnitHandler(null, new DomainCodeConcat()));
	}

	@Test
	void test_constructor_rejects_null_domain_code_concat() {
		assertThrows(IllegalArgumentException.class, () -> new DomainCodeTransUnitHandler("messages", null));
	}

	@Test
	void test_put_uses_custom_domain_code_concat() {
		DomainCodeTransUnitHandler handler = new DomainCodeTransUnitHandler("messages", new DomainCodeConcat("_"));
		ConcurrentMap<Locale, ConcurrentMap<String, String>> catalogMap = new ConcurrentHashMap<>();

		handler.put(catalogMap, new TransUnit(EN, "headline", "Headline (en)"));

		Map<String, String> bucket = catalogMap.get(EN);
		assertEquals("Headline (en)", bucket.get("messages_headline"));
	}

	@Test
	void test_put_transunit_with_matching_domain_is_aliased_to_plain_code() {
		DomainCodeTransUnitHandler handler = new DomainCodeTransUnitHandler("messages");
		ConcurrentMap<Locale, ConcurrentMap<String, String>> catalogMap = new ConcurrentHashMap<>();

		handler.put(catalogMap, new TransUnitDomain(EN, "headline", "Headline (en)", "messages"));

		Map<String, String> bucket = catalogMap.get(EN);
		assertEquals("Headline (en)", bucket.get("headline"));
		assertEquals("Headline (en)", bucket.get("messages.headline"));
	}

	@Test
	void test_put_transunit_with_non_default_domain_is_not_aliased_to_plain_code() {
		DomainCodeTransUnitHandler handler = new DomainCodeTransUnitHandler("messages");
		ConcurrentMap<Locale, ConcurrentMap<String, String>> catalogMap = new ConcurrentHashMap<>();

		handler.put(catalogMap, new TransUnitDomain(EN, "headline", "Payment Headline (en)", "payment"));

		Map<String, String> bucket = catalogMap.get(EN);
		assertEquals("Payment Headline (en)", bucket.get("payment.headline"));
		assertFalse(bucket.containsKey("headline"));
	}

	@Test
	void test_put_does_not_overwrite_existing_value() {
		DomainCodeTransUnitHandler handler = new DomainCodeTransUnitHandler("messages");
		ConcurrentMap<Locale, ConcurrentMap<String, String>> catalogMap = new ConcurrentHashMap<>();

		handler.put(catalogMap, new TransUnit(EN, "headline", "First"));
		handler.put(catalogMap, new TransUnit(EN, "headline", "Second"));

		assertEquals("First", catalogMap.get(EN).get("messages.headline"));
	}

	@Test
	void test_put_ignores_locale_without_language() {
		DomainCodeTransUnitHandler handler = new DomainCodeTransUnitHandler("messages");
		ConcurrentMap<Locale, ConcurrentMap<String, String>> catalogMap = new ConcurrentHashMap<>();

		handler.put(catalogMap, new TransUnit(Locale.ROOT, "headline", "Headline"));

		assertTrue(catalogMap.isEmpty());
	}

	@Test
	void test_put_reuses_bucket_for_same_locale() {
		DomainCodeTransUnitHandler handler = new DomainCodeTransUnitHandler("messages");
		ConcurrentMap<Locale, ConcurrentMap<String, String>> catalogMap = new ConcurrentHashMap<>();

		handler.put(catalogMap, new TransUnit(EN, "headline", "Headline (en)"));
		handler.put(catalogMap, new TransUnit(EN, "text", "Text (en)"));

		assertEquals(1, catalogMap.size());
		Map<String, String> bucket = catalogMap.get(EN);
		assertEquals("Headline (en)", bucket.get("messages.headline"));
		assertEquals("Text (en)", bucket.get("messages.text"));
	}
}
