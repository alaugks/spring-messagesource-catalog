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

import io.github.alaugks.spring.messagesource.catalog.records.TransUnitInterface;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public abstract class AbstractCatalog implements CatalogInterface {

	private volatile CatalogInterface nextCatalog;

	public CatalogInterface nextCatalog(CatalogInterface catalog) {
		this.nextCatalog = catalog;
		return catalog;
	}

	public List<TransUnitInterface> getTransUnits() {
		if (this.nextCatalog == null) {
			return new ArrayList<>();
		}

		return this.nextCatalog.getTransUnits();
	}

	public String resolveCode(String code, Locale locale) {
		if (this.nextCatalog == null) {
			return null;
		}

		return this.nextCatalog.resolveCode(code, locale);
	}

	protected static String concatCode(String domain, String code) {
		return Optional.ofNullable(domain).orElse(Catalog.DEFAULT_DOMAIN) + "." + code;
	}
}
