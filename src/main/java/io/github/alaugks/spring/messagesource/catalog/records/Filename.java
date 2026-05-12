/*
 * Copyright 2024-2025 André Laugks <alaugks@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
