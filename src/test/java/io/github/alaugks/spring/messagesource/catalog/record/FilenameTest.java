package io.github.alaugks.spring.messagesource.catalog.record;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.alaugks.spring.messagesource.catalog.exception.CatalogMessageSourceRuntimeException;
import io.github.alaugks.spring.messagesource.catalog.records.Filename;
import org.junit.jupiter.api.Test;

class FilenameTest {

	@Test
	void test_hasNoLocale() {
		Filename filename = new Filename("messages", null, null);

		assertFalse(filename.hasLocale());
	}

	@Test
	void test_hasLocale() {
		Filename filename = new Filename("messages", "en", null);

		assertTrue(filename.hasLocale());
	}

	@Test
	void test_formatedException() {
		Filename filename = new Filename("messages", "en", "bar");

		assertThrows(CatalogMessageSourceRuntimeException.class, filename::locale);
	}
}
