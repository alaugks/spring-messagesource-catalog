package io.github.alaugks.spring.messagesource.catalog.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class LocationPatternTest {

	@Test
	void test_string() {
		LocationPattern locationPattern = new LocationPattern("path/");

		assertEquals(Set.of("path/"), locationPattern.getLocationPatterns());
	}

	@Test
	void test_string_list() {
		LocationPattern locationPattern = new LocationPattern(List.of("path/", "path/", "other_path/"));

		assertEquals(Set.of("path/", "other_path/"), locationPattern.getLocationPatterns());
	}
}
