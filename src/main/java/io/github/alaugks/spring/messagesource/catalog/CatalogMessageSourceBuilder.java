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

import io.github.alaugks.spring.messagesource.catalog.catalog.Catalog;
import io.github.alaugks.spring.messagesource.catalog.catalog.CatalogCache;
import io.github.alaugks.spring.messagesource.catalog.catalog.CatalogInterface;
import io.github.alaugks.spring.messagesource.catalog.records.TransUnitInterface;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.util.Assert;

public class CatalogMessageSourceBuilder extends AbstractMessageSource {

	private final CatalogInterface catalog;

	private CatalogMessageSourceBuilder(CatalogInterface catalog) {
		this.catalog = catalog;
	}

	public static Builder builder(List<TransUnitInterface> transUnits, Locale defaultLocale) {
		Assert.notNull(transUnits, "Argument transUnits must not be null");

		return builder(new TransUnitsCatalog(transUnits), defaultLocale);
	}

	public static Builder builder(CatalogInterface catalogSource, Locale defaultLocale) {
		Assert.notNull(catalogSource, "Argument catalogSource must not be null");

		return new Builder(catalogSource, defaultLocale);
	}

	public static final class Builder {

		private final Locale defaultLocale;

		private final CatalogInterface catalogSource;

		private String defaultDomain = Catalog.DEFAULT_DOMAIN;

		public Builder(CatalogInterface catalogSource, Locale defaultLocale) {
			Assert.notNull(defaultLocale, "Argument defaultLocale must not be null");

			this.catalogSource = catalogSource;
			this.defaultLocale = defaultLocale;
		}

		public Builder defaultDomain(String defaultDomain) {
			Assert.notNull(defaultDomain, "Argument defaultDomain must not be null");

			this.defaultDomain = defaultDomain;

			return this;
		}

		public CatalogMessageSourceBuilder build() {
			CatalogInterface catalogChain = new CatalogCache();
			catalogChain
					.nextCatalog(new Catalog(this.defaultLocale, this.defaultDomain))
					.nextCatalog(catalogSource);
			catalogChain.build();

			return new CatalogMessageSourceBuilder(catalogChain);
		}
	}

	@Override
	protected MessageFormat resolveCode(String code, Locale locale) {
		String value = this.catalog.resolveCode(code, locale);

		if (value != null) {
			return new MessageFormat(value, locale);
		}

		return null;
	}
}
