package io.github.alaugks.spring.messagesource.catalog.records;

import java.util.Locale;

public interface TransUnitInterface {
	Locale locale();

	String code();

	String value();

	String domain();
}
