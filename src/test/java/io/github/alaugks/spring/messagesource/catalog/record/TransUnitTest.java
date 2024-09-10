package io.github.alaugks.spring.messagesource.catalog.record;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.github.alaugks.spring.messagesource.catalog.records.TransUnit;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class TransUnitTest {

	@Test
	void test_withoutDomain() {
		var transUnit = new TransUnit(Locale.forLanguageTag("en"), "the-code", "the-value");

		assertEquals(Locale.forLanguageTag("en"), transUnit.locale());
		assertEquals("the-code", transUnit.code());
		assertEquals("the-value", transUnit.value());
		assertNull(transUnit.domain());
	}

	@Test
	void test_witDomain() {
		var transUnit = new TransUnit(Locale.forLanguageTag("en"), "the-code", "the-value", "my-domain");

		assertEquals(Locale.forLanguageTag("en"), transUnit.locale());
		assertEquals("the-code", transUnit.code());
		assertEquals("the-value", transUnit.value());
		assertEquals("my-domain", transUnit.domain());
	}
}
