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

import io.github.alaugks.spring.messagesource.catalog.records.TranslationFile;
import java.io.InputStream;
import java.util.Locale;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class TranslationFileTest {

	@Test
	void test_record() {
		TranslationFile translationFile = new TranslationFile(
				"my-domain",
				Locale.forLanguageTag("en-US"),
				getClass().getClassLoader().getResourceAsStream("translations_en_US/messages_en_US.txt")
		);
		assertEquals("my-domain", translationFile.domain());
		assertEquals(Locale.forLanguageTag("en-US"), translationFile.locale());
		assertInstanceOf(InputStream.class, translationFile.inputStream());
	}
}
