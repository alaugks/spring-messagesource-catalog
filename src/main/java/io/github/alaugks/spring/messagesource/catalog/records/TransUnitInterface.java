// SPDX-License-Identifier: Apache-2.0
// Copyright 2024 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.catalog.records;

import java.util.Locale;
import org.jspecify.annotations.Nullable;

/**
 * A single translation entry: a {@code (locale, domain, code) -> value} tuple.
 */
public interface TransUnitInterface {

	/**
	 * {@return the locale this translation belongs to}
	 */
	Locale locale();

	/**
	 * {@return the message code (without domain prefix)}
	 */
	String code();

	/**
	 * {@return the translated text}
	 */
	String value();

	/**
	 * {@return the domain this trans unit belongs to, or {@code null} for the default domain}
	 */
	@Nullable String domain();
}
