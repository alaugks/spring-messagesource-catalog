package io.github.alaugks.spring.messagesource.catalog.record;

import java.io.InputStream;
import java.util.Locale;

import io.github.alaugks.spring.messagesource.catalog.records.TranslationFile;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class TranslationFileTest {

	@Test
	void test_record() {
		var translationFile = new TranslationFile(
				"my-domain",
				Locale.forLanguageTag("en-US"),
				getClass().getClassLoader().getResourceAsStream("translations_en_US/messages_en_US.txt")
		);
		assertEquals("my-domain", translationFile.domain());
		assertEquals(Locale.forLanguageTag("en-US"), translationFile.locale());
		assertInstanceOf(InputStream.class, translationFile.inputStream());
	}
}
