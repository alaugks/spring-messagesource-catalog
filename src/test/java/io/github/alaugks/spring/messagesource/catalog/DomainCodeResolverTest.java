// SPDX-License-Identifier: Apache-2.0
// Copyright 2024 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.catalog;

import java.util.ArrayList;
import java.util.List;
import java.util.ListResourceBundle;
import java.util.Map;
import java.util.ResourceBundle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DomainCodeResolverTest {

	private final DomainCodeResolver resolver = new DomainCodeResolver();

	@Test
	void test_resolve_plain_code() {
		ResourceBundle bundle = bundleOf(Map.of("headline", "Headline (en)"));

		assertEquals("Headline (en)", resolver.resolve(bundle, "headline"));
	}

	@Test
	void test_resolve_falls_back_to_default_domain_code() {
		ResourceBundle bundle = bundleOf(Map.of("messages.headline", "Headline (en)"));

		assertEquals("Headline (en)", resolver.resolve(bundle, "headline"));
	}

	@Test
	void test_resolve_prefers_plain_code_over_default_domain_code() {
		ResourceBundle bundle = bundleOf(Map.of(
				"headline", "Plain",
				"messages.headline", "Domain"
		));

		assertEquals("Plain", resolver.resolve(bundle, "headline"));
	}

	@Test
	void test_resolve_returns_null_when_not_found() {
		ResourceBundle bundle = bundleOf(Map.of("other", "Other"));

		assertNull(resolver.resolve(bundle, "headline"));
	}

	@Test
	void test_resolve_uses_default_domain_code_from_custom_domain_code_concat() {
		DomainCodeConcatInterface domainCodeConcat = (domain, code) -> "custom-" + code;
		DomainCodeResolver resolver = new DomainCodeResolver(domainCodeConcat);
		ResourceBundle bundle = bundleOf(Map.of("custom-headline", "Headline (custom)"));

		assertEquals("Headline (custom)", resolver.resolve(bundle, "headline"));
	}

	@Test
	void test_resolve_passes_default_domain_and_code_to_domain_code_concat() {
		List<String> capturedArgs = new ArrayList<>();
		DomainCodeConcatInterface domainCodeConcat = (domain, code) -> {
			capturedArgs.add(domain);
			capturedArgs.add(code);
			return "irrelevant";
		};
		DomainCodeResolver resolver = new DomainCodeResolver(domainCodeConcat);
		ResourceBundle bundle = bundleOf(Map.of());

		resolver.resolve(bundle, "headline");

		assertEquals(List.of("messages", "headline"), capturedArgs);
	}

	private static ResourceBundle bundleOf(Map<String, String> entries) {
		Object[][] contents = entries.entrySet().stream()
				.map(entry -> new Object[] {entry.getKey(), entry.getValue()})
				.toArray(Object[][]::new);

		return new ListResourceBundle() {
			@Override
			protected Object[][] getContents() {
				return contents;
			}
		};
	}
}
