// SPDX-License-Identifier: Apache-2.0
// Copyright 2024 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.catalog;

import io.github.alaugks.spring.messagesource.catalog.catalog.CatalogInterface;
import io.github.alaugks.spring.messagesource.catalog.records.TransUnitInterface;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A {@link CatalogInterface} that composes an ordered list of sources.
 *
 * <p>Eager units from all sources are concatenated in source order; on a lazy lookup the
 * sources are consulted in order and the first non-{@code null} result wins. This replaces
 * the former chain-of-responsibility wiring: sources no longer reference one another, the
 * composite owns the iteration.
 */
final class CompositeCatalog implements CatalogInterface {

	/** Sources consulted in order, copied defensively at construction. */
	private final List<CatalogInterface> sources;

	CompositeCatalog(List<CatalogInterface> sources) {
		this.sources = List.copyOf(sources);
	}

	@Override
	public List<TransUnitInterface> getTransUnits() {
		List<TransUnitInterface> transUnits = new ArrayList<>();
		for (CatalogInterface source : this.sources) {
			transUnits.addAll(source.getTransUnits());
		}
		return transUnits;
	}

	@Override
	public TransUnitInterface resolveTransUnit(String code, Locale locale) {
		for (CatalogInterface source : this.sources) {
			TransUnitInterface transUnit = source.resolveTransUnit(code, locale);
			if (transUnit != null) {
				return transUnit;
			}
		}
		return null;
	}
}
