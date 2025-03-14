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

package io.github.alaugks.spring.messagesource.catalog.resources;

import io.github.alaugks.spring.messagesource.catalog.records.Filename;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ResourcesFilenameParserTest {

	@Test
	void test_domain_withoutLocale() {
		Filename filename = new ResourcesFileNameParser("message.ext").parse();
		assertEquals("message", filename.domain());
		assertNull(filename.language());
		assertNull(filename.region());
	}

	@Test
	void test_domain_en() {
		Filename filename = new ResourcesFileNameParser("message_en.ext").parse();
		assertEquals("message", filename.domain());
		assertEquals("en", filename.language());
		assertNull(filename.region());
		assertEquals("en", filename.locale().toString());
	}

	@Test
	void test_domain_en_withDash() {
		Filename filename = new ResourcesFileNameParser("message-en.ext").parse();
		assertEquals("message", filename.domain());
		assertEquals("en", filename.language());
		assertNull(filename.region());
		assertEquals("en", filename.locale().toString());
	}

	@Test
	void test_domain_enGB() {
		Filename filename = new ResourcesFileNameParser("message_en_GB.ext").parse();
		assertEquals("message", filename.domain());
		assertEquals("en", filename.language());
		assertEquals("GB", filename.region());
		assertEquals("en_GB", filename.locale().toString());
	}

	@Test
	void test_domain_enGB_withDash() {
		Filename filename = new ResourcesFileNameParser("message-en-GB.ext").parse();
		assertEquals("message", filename.domain());
		assertEquals("en", filename.language());
		assertEquals("GB", filename.region());
		assertEquals("en_GB", filename.locale().toString());
	}

	@Test
	void test_not() {
		assertNull(new ResourcesFileNameParser(".ext").parse());
	}
}
