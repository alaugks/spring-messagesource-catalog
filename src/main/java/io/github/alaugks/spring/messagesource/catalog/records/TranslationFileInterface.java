// SPDX-License-Identifier: Apache-2.0
// Copyright 2024 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.catalog.records;

import io.github.alaugks.spring.messagesource.catalog.resources.ResourceLoaderInterface;
import java.util.Locale;

/**
 * A loaded translation file: locale and the raw file content.
 *
 * <p>Produced by a
 * {@link ResourceLoaderInterface}
 * implementation and consumed by format-specific parsers (XLIFF, JSON).
 * {@link TranslationFile} is the default implementation.
 */
public interface TranslationFileInterface {

	/**
	 * {@return the locale derived from the file name, or the default locale when the file
	 * name carries no locale part}
	 */
	Locale locale();

	/**
	 * {@return the raw file content}
	 */
	byte[] content();
}
