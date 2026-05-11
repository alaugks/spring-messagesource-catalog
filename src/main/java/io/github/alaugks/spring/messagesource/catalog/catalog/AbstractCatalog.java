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

/**
 * Convenient base class for {@link CatalogInterface} implementations.
 *
 * <p>Provides the chain plumbing ({@link #nextCatalog(CatalogInterface)}) and no-op
 * defaults for both data methods. Subclasses contribute by overriding exactly one of them:
 *
 * <ul>
 *   <li><b>Eager</b> — override {@link #getTransUnits()} to return the trans units the
 *       source contributes at construction time.</li>
 *   <li><b>Lazy</b> — override {@link #resolveTransUnit(String, Locale)} to resolve a
 *       single trans unit on demand.</li>
 * </ul>
 *
 * <p>Once the builder has wired the chain, the unchanged defaults delegate to the next
 * catalog, so a non-overriding subclass behaves as a transparent link in the chain.
 */
public abstract class AbstractCatalog implements CatalogInterface {

	private CatalogInterface nextCatalog;

	/**
	 * Stores the next catalog in the chain and returns it, so calls can be chained fluently.
	 *
	 * @param catalog the next catalog
	 * @return the same {@code catalog}, to allow fluent chaining
	 */
	public CatalogInterface nextCatalog(CatalogInterface catalog) {
		this.nextCatalog = catalog;
		return catalog;
	}

	/**
	 * Default eager source: returns an empty list, or — once a next catalog has been wired —
	 * delegates to it. Subclasses override to contribute trans units.
	 *
	 * @return the trans units from the next catalog, or an empty list when this is the last link
	 */
	public List<TransUnitInterface> getTransUnits() {
		if (this.nextCatalog == null) {
			return new ArrayList<>();
		}

		return this.nextCatalog.getTransUnits();
	}

	/**
	 * Default lazy lookup: returns {@code null}, or — once a next catalog has been wired —
	 * delegates to it. Subclasses override to resolve on demand.
	 *
	 * <p>See {@link CatalogInterface#resolveTransUnit(String, Locale)} for the {@code code}
	 * format (bare or domain-qualified).
	 *
	 * @param code the message code; bare or domain-qualified ({@code "<domain>.<code>"})
	 * @param locale the locale to resolve for
	 * @return the resolved trans unit from the next catalog, or {@code null} when this is
	 *         the last link or the next catalog cannot resolve it
	 */
	public TransUnitInterface resolveTransUnit(String code, Locale locale) {
		if (this.nextCatalog == null) {
			return null;
		}

		return this.nextCatalog.resolveTransUnit(code, locale);
	}
}
