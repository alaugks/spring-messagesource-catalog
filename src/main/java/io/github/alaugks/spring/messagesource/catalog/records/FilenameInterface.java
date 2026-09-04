// SPDX-License-Identifier: Apache-2.0
// Copyright 2024 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.catalog.records;

import io.github.alaugks.spring.messagesource.catalog.resources.ResourceFileNameParserInterface;
import java.util.Locale;
import org.jspecify.annotations.Nullable;

/**
 * Parsed components of a translation resource file name (e.g. {@code messages_en_GB}).
 *
 * <p>Produced by a
 * {@link ResourceFileNameParserInterface}
 * implementation. {@link Filename} is the default implementation.
 */
public interface FilenameInterface {

	/**
	 * {@return {@code true} when the file name contains a language part}
	 */
	boolean hasLocale();

	/**
	 * {@return the locale built from the language and region parts, or {@code null} when no
	 * language is present}
	 */
	@Nullable Locale locale();

	/**
	 * {@return the language part, or {@code null} when the file name carries no locale}
	 */
	@Nullable String language();

	/**
	 * {@return the region part, or {@code null} when no region is given}
	 */
	@Nullable String region();
}
