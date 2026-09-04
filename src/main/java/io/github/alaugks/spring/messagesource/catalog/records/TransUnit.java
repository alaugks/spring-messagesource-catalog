// SPDX-License-Identifier: Apache-2.0
// Copyright 2024 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.catalog.records;

import java.util.Locale;

/**
 * Immutable {@link TransUnitInterface} implementation.
 *
 * @param locale the locale this translation belongs to
 * @param code   the message code
 * @param value  the translated text
 */
public record TransUnit(Locale locale, String code, String value) implements TransUnitInterface {

}
