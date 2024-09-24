package io.github.alaugks.spring.messagesource.catalog.records;

import java.util.Locale;

public record TransUnit(Locale locale, String code, String value, String domain) implements TransUnitInterface {

	public TransUnit(Locale locale, String code, String value) {
		this(locale, code, value, null);
	}
}
