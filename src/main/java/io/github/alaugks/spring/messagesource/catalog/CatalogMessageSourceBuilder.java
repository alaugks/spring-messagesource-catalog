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

package io.github.alaugks.spring.messagesource.catalog;

import io.github.alaugks.spring.messagesource.catalog.catalog.CatalogInterface;
import io.github.alaugks.spring.messagesource.catalog.records.TransUnitInterface;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

public class CatalogMessageSourceBuilder extends AbstractMessageSource {

	public static final String DEFAULT_DOMAIN = "messages";

	private final Map<Locale, Map<String, String>> catalogMap;

	private final Locale defaultLocale;

	private final String defaultDomain;

	private final CatalogInterface chainHead;

	private CatalogMessageSourceBuilder(List<CatalogInterface> sources, Locale defaultLocale, String defaultDomain) {
		this.defaultLocale = defaultLocale;
		this.defaultDomain = defaultDomain;
		this.catalogMap = new ConcurrentHashMap<>();

		for (CatalogInterface source : sources) {
			source.getTransUnits().forEach(t -> this.put(t.locale(), t.code(), t.value(), t.domain()));
		}

		for (int i = 0; i < sources.size() - 1; i++) {
			sources.get(i).nextCatalog(sources.get(i + 1));
		}
		this.chainHead = sources.get(0);
	}

	public static Builder builder(List<TransUnitInterface> transUnits, Locale defaultLocale) {
		Assert.notNull(transUnits, "Argument transUnits must not be null");

		return builder(new TransUnitsCatalog(transUnits), defaultLocale);
	}

	public static Builder builder(CatalogInterface catalogSource, Locale defaultLocale) {
		Assert.notNull(catalogSource, "Argument catalogSource must not be null");

		return new Builder(catalogSource, defaultLocale);
	}

	@Override
	@Nullable
	protected MessageFormat resolveCode(@NonNull String code, @NonNull Locale locale) {
		String value = this.resolveFromCatalog(code, locale);

		if (value != null) {
			return new MessageFormat(value, locale);
		}

		return null;
	}

	private String resolveFromCatalog(String code, Locale locale) {
		if (locale.getLanguage().isEmpty() || code.isEmpty()) {
			return null;
		}

		Optional<String> value = this.resolveFromCatalogMap(code, locale);
		if (value.isPresent()) {
			return value.get();
		}

		TransUnitInterface tu = this.chainHead.resolveTransUnit(code, locale);
		if (tu != null) {
			this.put(tu.locale(), tu.code(), tu.value(), tu.domain());
			return tu.value();
		}

		return null;
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
		return new Locale.Builder().setLanguage(locale.getLanguage()).build();
	}

	private String concatCode(String domain, String code) {
		return Optional.ofNullable(domain).orElse(DEFAULT_DOMAIN) + "." + code;
	}

	public static final class Builder {

		private final Locale defaultLocale;

		private final List<CatalogInterface> sources = new ArrayList<>();

		private String defaultDomain = DEFAULT_DOMAIN;

		public Builder(CatalogInterface catalogSource, Locale defaultLocale) {
			Assert.notNull(defaultLocale, "Argument defaultLocale must not be null");

			this.defaultLocale = defaultLocale;
			this.sources.add(catalogSource);
		}

		public Builder defaultDomain(String defaultDomain) {
			Assert.notNull(defaultDomain, "Argument defaultDomain must not be null");

			this.defaultDomain = defaultDomain;

			return this;
		}

		public Builder addSource(CatalogInterface source) {
			Assert.notNull(source, "Argument source must not be null");

			this.sources.add(source);

			return this;
		}

		public Builder addSource(List<TransUnitInterface> transUnits) {
			Assert.notNull(transUnits, "Argument transUnits must not be null");

			return this.addSource(new TransUnitsCatalog(transUnits));
		}

		public CatalogMessageSourceBuilder build() {
			return new CatalogMessageSourceBuilder(this.sources, this.defaultLocale, this.defaultDomain);
		}
	}
}
