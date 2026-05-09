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

package io.github.alaugks.spring.messagesource.catalog.catalog;

import java.util.Locale;
import java.util.Locale.Builder;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.util.Assert;

public final class Catalog extends AbstractCatalog {

	public static final String DEFAULT_DOMAIN = "messages";

	private final Map<Locale, Map<String, String>> catalogMap;

	private final Locale defaultLocale;

	private final String defaultDomain;

	public Catalog(Locale defaultLocale) {
		this(defaultLocale, DEFAULT_DOMAIN);
	}

	public Catalog(Locale defaultLocale, String defaultDomain) {
		Assert.notNull(defaultLocale, "Argument defaultLocale must not be null");
		Assert.notNull(defaultDomain, "Argument defaultDomain must not be null");

		this.catalogMap = new ConcurrentHashMap<>();
		this.defaultLocale = defaultLocale;
		this.defaultDomain = defaultDomain;
	}

	@Override
	public String resolveCode(String code, Locale locale) {
		if (locale.getLanguage().isEmpty() || code.isEmpty()) {
			return null;
		}

		// Resolve in CatalogMap
		Optional<String> value = this.resolveFromCatalogMap(code, locale);
		if (value.isPresent()) {
			return value.get();
		}

		String nextValue = super.resolveCode(code, locale);
		if (nextValue != null) {
			this.put(locale, code, nextValue, null);
			return nextValue;
		}

		return null;
	}

	public void build() {
		super.getTransUnits().forEach(t -> this.put(t.locale(), t.code(), t.value(), t.domain()));
	}

	private void put(Locale locale, String code, String value, String domain) {
		if (locale.getLanguage().isEmpty()) {
			return;
		}

		Map<String, String> bucket = this.catalogMap.computeIfAbsent(
				locale, l -> new ConcurrentHashMap<>()
		);

		if (Objects.equals(domain, this.defaultDomain)) {
			bucket.putIfAbsent(code, value);
		}
		bucket.putIfAbsent(concatCode(domain, code), value);
	}

	private Optional<String> resolveFromCatalogMap(String code, Locale locale) {
		return this.getTargetValue(code, locale)
				.or(() -> this.getTargetValue(concatCode(this.defaultDomain, code), locale))
				.or(() -> this.getTargetValue(code, this.buildLocaleWithoutRegion(locale)))
				.or(() -> this.getTargetValue(concatCode(this.defaultDomain, code), this.buildLocaleWithoutRegion(locale)))
				.or(() -> this.getTargetValue(code, this.defaultLocale))
				.or(() -> this.getTargetValue(concatCode(this.defaultDomain, code), this.defaultLocale));
	}

	private Optional<String> getTargetValue(String code, Locale locale) {
		return Optional.ofNullable(this.catalogMap.get(locale)).flatMap(
				localeCatalog -> Optional.ofNullable(localeCatalog.get(code))
		);
	}

	private Locale buildLocaleWithoutRegion(Locale locale) {
		return new Builder().setLanguage(locale.getLanguage()).build();
	}
}
