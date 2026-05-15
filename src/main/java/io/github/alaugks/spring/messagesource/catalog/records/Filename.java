// SPDX-License-Identifier: Apache-2.0
// Copyright 2024 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.catalog.records;

import io.github.alaugks.spring.messagesource.catalog.exception.CatalogMessageSourceRuntimeException;
import java.util.IllformedLocaleException;
import java.util.Locale;

/**
 * Parsed components of a translation resource file name (e.g. {@code messages_en_GB}),
 * produced by {@link io.github.alaugks.spring.messagesource.catalog.resources.ResourcesFileNameParser}.
 *
 * @param domain   the domain part (always present)
 * @param language the language part, or {@code null} when the file name carries no locale
 * @param region   the region part, or {@code null} when no region is given
 */
public record Filename(String domain, String language, String region) {

	/**
	 * Reports whether the file name carried a language part.
	 *
	 * @return {@code true} when the file name contains a language part
	 */
	public boolean hasLocale() {
		return this.language != null;
	}

	/**
	 * Builds a {@link Locale} from {@link #language} and {@link #region}.
	 *
	 * @return the constructed locale, or {@code null} when no language is present
	 * @throws CatalogMessageSourceRuntimeException if the parsed parts do not form a valid locale
	 */
	public Locale locale() {
		try {
			if (language != null) {
				Locale.Builder localeBuilder = new Locale.Builder();
				localeBuilder.setLanguage(language);
				if (region != null) {
					localeBuilder.setRegion(region);
				}
				return localeBuilder.build();
			}
			return null;
		}
		catch (IllformedLocaleException e) {
			throw new CatalogMessageSourceRuntimeException(e);
		}
	}
}
