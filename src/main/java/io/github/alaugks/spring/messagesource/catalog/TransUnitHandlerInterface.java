// SPDX-License-Identifier: Apache-2.0
// Copyright 2024 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.catalog;

import io.github.alaugks.spring.messagesource.catalog.records.TransUnitInterface;
import java.util.Locale;
import java.util.concurrent.ConcurrentMap;

/**
 * Strategy for storing a resolved {@link TransUnitInterface} into the catalog map.
 *
 * <p>A trans unit is stored under its domain-qualified code (built via {@link ResolverInterface}),
 * plus an alias under its plain code when its domain matches the default domain.
 */
public interface TransUnitHandlerInterface {

	/**
	 * Stores {@code transUnit} into {@code catalogMap}, creating the locale's bucket if needed. A trans unit whose
	 * locale has no language (e.g. {@link Locale#ROOT}) is ignored.
	 *
	 * @param catalogMap the catalog map to store into, keyed by locale and then by code
	 * @param resolver   the resolver used to build the domain-qualified code
	 * @param transUnit  the trans unit to store
	 */
	void put(
			ConcurrentMap<Locale, ConcurrentMap<String, String>> catalogMap,
			ResolverInterface resolver,
			TransUnitInterface transUnit
	);
}
