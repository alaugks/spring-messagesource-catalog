// SPDX-License-Identifier: Apache-2.0
// Copyright 2024 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.catalog.records;

import java.util.Locale;

/**
 * A single translation entry: a {@code (locale, domain, code) -> value} tuple.
 */
public interface TransUnitInterface {

	/**
	 * Returns the locale this translation applies to.
	 *
	 * @return the locale this translation belongs to
	 */
	Locale locale();

	/**
	 * Returns the message code.
	 *
	 * @return the message code (without domain prefix)
	 */
	String code();

	/**
	 * Returns the translated text.
	 *
	 * @return the translated text
	 */
	String value();

	/**
	 * Returns the domain this trans unit is registered under.
	 *
	 * @return the domain this trans unit belongs to, or {@code null} for the default domain
	 */
	String domain();
}
