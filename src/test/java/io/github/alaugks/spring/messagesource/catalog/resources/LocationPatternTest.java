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

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LocationPatternTest {

	@Test
	void test_string() {
		LocationPattern locationPattern = new LocationPattern("path/");

		assertEquals(Set.of("path/"), locationPattern.getLocationPattern());
		assertEquals(Set.of("path/"), locationPattern.getLocationPatterns());
	}

	@Test
	void test_string_list() {
		LocationPattern locationPattern = new LocationPattern(List.of("path/", "path/", "other_path/"));

		assertEquals(Set.of("path/", "other_path/"), locationPattern.getLocationPattern());
		assertEquals(Set.of("path/", "other_path/"), locationPattern.getLocationPatterns());
	}
}
