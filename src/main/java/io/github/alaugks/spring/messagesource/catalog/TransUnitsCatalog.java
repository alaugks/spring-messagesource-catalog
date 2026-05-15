// SPDX-License-Identifier: Apache-2.0
// Copyright 2024 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.catalog;

import io.github.alaugks.spring.messagesource.catalog.catalog.AbstractCatalog;
import io.github.alaugks.spring.messagesource.catalog.records.TransUnitInterface;
import java.util.List;

/**
 * Eager catalog source backed by a pre-built list of trans units.
 */
public class TransUnitsCatalog extends AbstractCatalog {

	List<TransUnitInterface> transUnits;

	/**
	 * Creates a catalog backed by the given trans units.
	 *
	 * @param transUnits the trans units to serve; used directly without copying
	 */
	public TransUnitsCatalog(List<TransUnitInterface> transUnits) {
		this.transUnits = transUnits;
	}

	@Override
	public List<TransUnitInterface> getTransUnits() {
		return this.transUnits;
	}

}
