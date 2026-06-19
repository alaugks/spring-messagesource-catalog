// SPDX-License-Identifier: Apache-2.0
// Copyright 2024 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.catalog.resources;

import io.github.alaugks.spring.messagesource.catalog.records.Filename;
import java.util.Locale;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ResourcesFilenameParserTest {

	@ParameterizedTest
	@MethodSource("provider_filenames")
	void parse(String filename, String domain, String language, String region, String locale) {
		Filename result = new ResourcesFileNameParser(filename).parse();
		Locale resultLocale = result.locale();

		assertEquals(domain, result.domain());
		assertEquals(language, result.language());
		assertEquals(region, result.region());
		assertEquals(locale, resultLocale != null ? resultLocale.toString() : null);
	}

	private static Stream<Arguments> provider_filenames() {
		return Stream.of(
				// filename, domain, language, region, locale
				Arguments.of("message.ext", "message", null, null, null),
				Arguments.of("message_en.ext", "message", "en", null, "en"),
				Arguments.of("message.en.ext", "message", "en", null, "en"),
				Arguments.of("message-en.ext", "message", "en", null, "en"),
				Arguments.of("message_en_GB.ext", "message", "en", "GB", "en_GB"),
				Arguments.of("message.en_GB.ext", "message", "en", "GB", "en_GB"),
				Arguments.of("message-en-GB.ext", "message", "en", "GB", "en_GB"),
				Arguments.of("Message-En-Gb.ext", "Message", "En", "Gb", "en_GB")
		);
	}

	@Test
	void test_not() {
		assertNull(new ResourcesFileNameParser(".ext").parse());
	}
}
