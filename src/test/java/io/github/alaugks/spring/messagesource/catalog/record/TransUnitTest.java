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

import io.github.alaugks.spring.messagesource.catalog.records.TransUnit;
import java.util.Locale;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TransUnitTest {

	@Test
	void test_withoutDomain() {
		TransUnit transUnit = new TransUnit(Locale.forLanguageTag("en"), "the-code", "the-value");

		assertEquals(Locale.forLanguageTag("en"), transUnit.locale());
		assertEquals("the-code", transUnit.code());
		assertEquals("the-value", transUnit.value());
		assertNull(transUnit.domain());
	}

	@Test
	void test_witDomain() {
		TransUnit transUnit = new TransUnit(Locale.forLanguageTag("en"), "the-code", "the-value", "my-domain");

		assertEquals(Locale.forLanguageTag("en"), transUnit.locale());
		assertEquals("the-code", transUnit.code());
		assertEquals("the-value", transUnit.value());
		assertEquals("my-domain", transUnit.domain());
	}
}
