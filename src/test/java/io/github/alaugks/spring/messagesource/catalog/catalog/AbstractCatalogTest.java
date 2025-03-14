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

package io.github.alaugks.spring.messagesource.catalog.catalog;

import java.util.ArrayList;
import java.util.Locale;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

class AbstractCatalogTest {

	@Test
	void test_set() {
		assertInstanceOf(NextCatalog.class, new MyCatalog().nextCatalog(new NextCatalog()));
	}

	@Test
	void test_getTransUnits() {
		assertEquals(new ArrayList<>(), new MyCatalog().getTransUnits());
	}

	@Test
	void test_resolveCode() {
		assertNull(new MyCatalog().resolveCode("foo", Locale.forLanguageTag("en")));
	}
}


class MyCatalog extends AbstractCatalog {

}

class NextCatalog extends AbstractCatalog {

}
