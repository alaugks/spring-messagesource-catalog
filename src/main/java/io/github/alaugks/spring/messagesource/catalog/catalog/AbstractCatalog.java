// SPDX-License-Identifier: Apache-2.0
// Copyright 2024 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.catalog.catalog;

import io.github.alaugks.spring.messagesource.catalog.records.TransUnitInterface;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.jspecify.annotations.Nullable;

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
	 * Default eager source: contributes nothing.
	 *
	 * @return an empty (mutable) list
	 */
	public List<TransUnitInterface> getTransUnits() {
		return new ArrayList<>();
	}

	/**
	 * Default lazy lookup: resolves nothing.
	 *
	 * @return always {@code null}
	 */
	public @Nullable TransUnitInterface resolveTransUnit(String code, Locale locale) {
		return null;
	}
}
