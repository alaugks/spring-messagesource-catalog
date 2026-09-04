// SPDX-License-Identifier: Apache-2.0
// Copyright 2024 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.catalog.resources;

import io.github.alaugks.spring.messagesource.catalog.records.TranslationFile;
import io.github.alaugks.spring.messagesource.catalog.records.TranslationFileInterface;
import java.util.List;

/**
 * Loads translation resources and parses each into a {@link TranslationFile}
 * (locale + raw bytes).
 *
 * <p>Used by sibling parser packages (XLIFF, JSON) as the file-loading stage that precedes
 * format-specific parsing.
 */
public interface ResourceLoaderInterface {

	/**
	 * {@return the loaded translation files}
	 */
	List<TranslationFileInterface> getTranslationFiles();
}
