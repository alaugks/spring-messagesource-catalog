// SPDX-License-Identifier: Apache-2.0
// Copyright 2024 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.catalog.record;

import io.github.alaugks.spring.messagesource.catalog.records.TranslationFile;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TranslationFileTest {

	@Test
	void test_record() throws IOException {
		byte[] content;
		try (InputStream inputStream = getClass().getClassLoader()
				.getResourceAsStream("translations_en_US/messages_en_US.txt")) {
			assertNotNull(inputStream);
			content = inputStream.readAllBytes();
		}

		TranslationFile translationFile = new TranslationFile(
				"my-domain",
				Locale.forLanguageTag("en-US"),
				content
		);
		assertEquals("my-domain", translationFile.domain());
		assertEquals(Locale.forLanguageTag("en-US"), translationFile.locale());
		assertEquals(content, translationFile.content());
	}

	@Test
	void test_equals() {
		TranslationFile a = new TranslationFile(
				"my-domain",
				Locale.forLanguageTag("en-US"),
				new byte[] {1, 2, 3}
		);
		TranslationFile b = new TranslationFile(
				"my-domain",
				Locale.forLanguageTag("en-US"),
				new byte[] {1, 2, 3}
		);

		assertEquals(a, b);
		assertEquals(a, a);
	}

	@Test
	void test_not_equals() {
		TranslationFile a = new TranslationFile(
				"my-domain",
				Locale.forLanguageTag("en-US"),
				new byte[] {1, 2, 3}
		);

		assertNotEquals(null, a);
		assertNotEquals(a, new TranslationFile(
				"other-domain",
				Locale.forLanguageTag("en-US"),
				new byte[] {1, 2, 3}
		));
		assertNotEquals(a, new TranslationFile(
				"my-domain",
				Locale.forLanguageTag("de-DE"),
				new byte[] {1, 2, 3}
		));
		assertNotEquals(a, new TranslationFile(
				"my-domain",
				Locale.forLanguageTag("en-US"),
				new byte[] {4, 5, 6}
		));
	}

	@Test
	void test_hash_code() {
		TranslationFile a = new TranslationFile(
				"my-domain",
				Locale.forLanguageTag("en-US"),
				new byte[] {1, 2, 3}
		);
		TranslationFile b = new TranslationFile(
				"my-domain",
				Locale.forLanguageTag("en-US"),
				new byte[] {1, 2, 3}
		);

		assertEquals(a.hashCode(), b.hashCode());
	}

	@Test
	void test_to_string() {
		TranslationFile translationFile = new TranslationFile(
				"my-domain",
				Locale.forLanguageTag("en-US"),
				new byte[] {1, 2, 3}
		);

        assertEquals("TranslationFile[domain=my-domain, locale=en_US, content=3 bytes]", translationFile.toString());
	}

	@Test
	void test_to_string_null_content() {
		TranslationFile translationFile = new TranslationFile(
				"my-domain",
				Locale.forLanguageTag("en-US"),
				null
		);

		assertEquals("TranslationFile[domain=my-domain, locale=en_US, content=null]", translationFile.toString());
	}
}
