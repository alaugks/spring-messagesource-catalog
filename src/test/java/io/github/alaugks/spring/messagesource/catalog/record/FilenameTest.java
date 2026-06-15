// SPDX-License-Identifier: Apache-2.0
// Copyright 2024 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.catalog.record;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.alaugks.spring.messagesource.catalog.exception.CatalogMessageSourceRuntimeException;
import io.github.alaugks.spring.messagesource.catalog.records.Filename;
import org.junit.jupiter.api.Test;

class FilenameTest {

	@Test
	void test_has_no_locale() {
		Filename filename = new Filename("messages", null, null);

		assertFalse(filename.hasLocale());
	}

	@Test
	void test_has_locale() {
		Filename filename = new Filename("messages", "en", null);

		assertTrue(filename.hasLocale());
	}

	@Test
	void test_invalid_locale_exception() {
		Filename filename = new Filename("messages", "en", "bar");

		assertThrows(CatalogMessageSourceRuntimeException.class, filename::locale);
	}
}
