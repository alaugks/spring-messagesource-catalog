// SPDX-License-Identifier: Apache-2.0
// Copyright 2024 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.catalog;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DomainCodeConcatTest {

	@Test
	void test_generate_with_domain() {
		DomainCodeConcat concat = new DomainCodeConcat();

		assertEquals("payment.headline", concat.generate("payment", "headline"));
	}

	@Test
	void test_generate_without_domain_uses_default_domain() {
		DomainCodeConcat concat = new DomainCodeConcat();

		assertEquals("messages.headline", concat.generate(null, "headline"));
	}

	@Test
	void test_generate_with_custom_divider() {
		DomainCodeConcat concat = new DomainCodeConcat("_");

		assertEquals("payment_headline", concat.generate("payment", "headline"));
	}

	@Test
	void test_generate_without_domain_and_custom_divider_uses_default_domain() {
		DomainCodeConcat concat = new DomainCodeConcat("_");

		assertEquals("messages_headline", concat.generate(null, "headline"));
	}
}
