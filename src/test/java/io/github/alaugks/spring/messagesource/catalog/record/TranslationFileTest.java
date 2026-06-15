// SPDX-License-Identifier: Apache-2.0
// Copyright 2024 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.catalog.record;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.github.alaugks.spring.messagesource.catalog.records.TranslationFile;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import org.junit.jupiter.api.Test;

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
}
