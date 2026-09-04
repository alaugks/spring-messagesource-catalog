// SPDX-License-Identifier: Apache-2.0
// Copyright 2024 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.catalog.records;

import io.github.alaugks.spring.messagesource.catalog.exception.CatalogMessageSourceRuntimeException;
import io.github.alaugks.spring.messagesource.catalog.resources.ResourceFileNameParser;
import java.util.IllformedLocaleException;
import java.util.Locale;
import org.jspecify.annotations.Nullable;

/**
 * Parsed components of a translation resource file name (e.g. {@code messages_en_GB}), produced by
 * {@link ResourceFileNameParser}.
 *
 * @param language the language part, or {@code null} when the file name carries no locale
 * @param region   the region part, or {@code null} when no region is given
 */
public record Filename(@Nullable String language, @Nullable String region) implements FilenameInterface {

	@Override
	public boolean hasLocale() {
		return this.language != null;
	}

	/**
	 * @throws CatalogMessageSourceRuntimeException if the parsed parts do not form a valid locale
	 */
	@Override
	public @Nullable Locale locale() {
		try {
			if (this.language != null) {
				Locale.Builder localeBuilder = new Locale.Builder();
				localeBuilder.setLanguage(this.language);
				if (this.region != null) {
					localeBuilder.setRegion(this.region);
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
