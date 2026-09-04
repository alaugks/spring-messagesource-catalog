// SPDX-License-Identifier: Apache-2.0
// Copyright 2024 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.catalog.records;

import java.util.Locale;

/**
 * A single translation entry: a {@code (locale, code) -> value} tuple.
 */
public interface TransUnitInterface {

	/**
	 * {@return the locale this translation belongs to}
	 */
	Locale locale();

	/**
	 * {@return the message code}
	 */
	String code();

	/**
	 * {@return the translated text}
	 */
	String value();
}
