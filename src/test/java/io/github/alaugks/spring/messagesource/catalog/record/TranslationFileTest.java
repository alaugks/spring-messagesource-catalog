/*
 * Copyright 2024-2025 André Laugks <alaugks@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
	void test_hashCode() {
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
	void test_toString() {
		TranslationFile translationFile = new TranslationFile(
				"my-domain",
				Locale.forLanguageTag("en-US"),
				new byte[] {1, 2, 3}
		);

        assertEquals("TranslationFile[domain=my-domain, locale=en_US, content=3 bytes]", translationFile.toString());
	}
}
