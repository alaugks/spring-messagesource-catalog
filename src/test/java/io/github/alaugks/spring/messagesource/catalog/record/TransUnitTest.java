// SPDX-License-Identifier: Apache-2.0
// Copyright 2024 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.catalog.record;

import io.github.alaugks.spring.messagesource.catalog.fxitures.TransUnitDomain;
import io.github.alaugks.spring.messagesource.catalog.records.TransUnit;
import java.util.Locale;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TransUnitTest {

	@Test
	void test_without_domain() {
		TransUnit transUnit = new TransUnit(Locale.forLanguageTag("en"), "the-code", "the-value");

		assertEquals(Locale.forLanguageTag("en"), transUnit.locale());
		assertEquals("the-code", transUnit.code());
		assertEquals("the-value", transUnit.value());
	}

	@Test
	void test_with_domain() {
		TransUnitDomain transUnit = new TransUnitDomain(Locale.forLanguageTag("en"), "the-code", "the-value", "my-domain");

		assertEquals(Locale.forLanguageTag("en"), transUnit.locale());
		assertEquals("the-code", transUnit.code());
		assertEquals("the-value", transUnit.value());
		assertEquals("my-domain", transUnit.domain());
	}
}
