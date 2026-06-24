// SPDX-License-Identifier: Apache-2.0
// Copyright 2024 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.catalog.catalog;

import io.github.alaugks.spring.messagesource.catalog.records.TransUnitInterface;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Convenient base class for {@link CatalogInterface} implementations.
 *
 * <p>Provides no-op defaults for both data methods. Subclasses contribute by overriding
 * the one that fits:
 *
 * <ul>
 *   <li><b>Eager</b> — override {@link #getTransUnits()} to return the trans units the
 *       source contributes at construction time.</li>
 *   <li><b>Lazy</b> — override {@link #resolveTransUnit(String, Locale)} to resolve a
 *       single trans unit on demand.</li>
 * </ul>
 *
 * <p>A non-overriding subclass contributes nothing: it serves no eager units and resolves
 * nothing. Aggregating multiple sources is the builder's responsibility, not the source's.
 */
public abstract class AbstractCatalog implements CatalogInterface {

	/**
	 * Default constructor for use by subclasses.
	 */
	protected AbstractCatalog() {
	}

	/**
	 * Default eager source: returns an empty (mutable) list. Subclasses override to
	 * contribute trans units.
	 *
	 * @return an empty list
	 */
	public List<TransUnitInterface> getTransUnits() {
		return new ArrayList<>();
	}

	/**
	 * Default lazy lookup: resolves nothing. Subclasses override to resolve on demand and
	 * return {@code null} for codes they cannot answer.
	 *
	 * <p>See {@link CatalogInterface#resolveTransUnit(String, Locale)} for the {@code code}
	 * format (with or without a domain prefix).
	 *
	 * @param code the message code, with or without a domain prefix ({@code "<domain>.<code>"})
	 * @param locale the locale to resolve for
	 * @return always {@code null}
	 */
	public TransUnitInterface resolveTransUnit(String code, Locale locale) {
		return null;
	}
}
