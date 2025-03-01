package io.github.alaugks.spring.messagesource.catalog.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.github.alaugks.spring.messagesource.catalog.records.Filename;
import org.junit.jupiter.api.Test;

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
