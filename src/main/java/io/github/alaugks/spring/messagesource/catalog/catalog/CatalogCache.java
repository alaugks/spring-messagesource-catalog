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
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Use {@link Catalog()}
 * @deprecated
 */
@Deprecated(since = "0.6.0", forRemoval = true)
public final class CatalogCache extends AbstractCatalog {

	private final Map<Locale, Map<String, String>> cacheMap = new ConcurrentHashMap<>();
	private final String defaultDomain;

	public CatalogCache() {
		this(Catalog.DEFAULT_DOMAIN);
	}

	public CatalogCache(String defaultDomain) {
		this.defaultDomain = defaultDomain;
	}
	
	@Override
	public String resolveCode(String code, Locale locale) {
		if (locale.toString().isEmpty() || code.isEmpty()) {
			return null;
		}

		// Resolve in Cache
		Optional<String> value = this.resolveFromCacheMap(code, locale);
		if (value.isPresent()) {
			return value.get();
		}

		// Resolve in Catalog
		String resolvedValue = super.resolveCode(code, locale);

		// Put in cache
		this.put(locale, code, resolvedValue);

		return resolvedValue;
	}

	@Override
	public void build() {
		super.build();
		super.getTransUnits().forEach(t -> {
			if (Objects.equals(t.domain(), this.defaultDomain)) {
				this.put(t.locale(), t.code(), t.value());
			}
			this.put(t.locale(), concatCode(t.domain(), t.code()), t.value());
		});
	}

	private Optional<String> resolveFromCacheMap(String code, Locale locale) {
		Map<String, String> map = this.cacheMap.get(locale);

		if (map != null) {
			return Optional.ofNullable(map.get(code));
		}

		return Optional.empty();
	}

	private void put(Locale locale, String code, String targetValue) {
		if (targetValue != null) {
			this.cacheMap.computeIfAbsent(
					locale, l -> new ConcurrentHashMap<>()
			).put(code, targetValue);
		}
	}
}
