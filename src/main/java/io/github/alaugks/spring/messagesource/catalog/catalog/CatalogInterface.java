// SPDX-License-Identifier: Apache-2.0
// Copyright 2024 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.catalog.catalog;

import io.github.alaugks.spring.messagesource.catalog.records.TransUnitInterface;
import java.util.List;
import java.util.Locale;

/**
 * A source of translation units consumed by the {@code CatalogMessageSourceBuilder}.
 *
 * <p>Sources can contribute in two ways:
 * <ul>
 *   <li><b>Eager</b> — implement {@link #getTransUnits()}. The list is read once at
 *       construction time and merged into the in-memory catalog map.</li>
 *   <li><b>Lazy</b> — implement {@link #resolveTransUnit(String, Locale)}. Called only
 *       when the catalog map has no entry for the requested key; the returned trans unit
 *       is cached in the map, so subsequent lookups for the same key hit memory.</li>
 * </ul>
 *
 * <p>Sources are wired into a <em>Chain of Responsibility</em> via
 * {@link #nextCatalog(CatalogInterface)}: each link decides whether it can answer the request
 * and delegates to the next otherwise (a {@code null} return from
 * {@link #resolveTransUnit(String, Locale)} is the opt-out). {@link AbstractCatalog} provides
 * the chain plumbing and no-op defaults; most implementations extend it rather than
 * implementing this interface directly.
 */
public interface CatalogInterface {

	/**
	 * Sets the next catalog in the chain. Called by the builder when wiring sources together.
	 *
	 * @param handler the next catalog
	 * @return the same {@code handler}, to allow fluent chaining
	 */
	CatalogInterface nextCatalog(CatalogInterface handler);

	/**
	 * Returns the translation units this source contributes eagerly. Aggregated into the
	 * catalog map at construction time.
	 *
	 * @return the trans units contributed by this source; may be empty
	 */
	List<TransUnitInterface> getTransUnits();

	/**
	 * Resolves a single trans unit on demand. Called only when the catalog map has no
	 * entry for {@code code} under {@code locale}; the returned value is cached in the map.
	 *
	 * <p>The {@code code} parameter is passed through as-is from the caller. It may be a
	 * bare key (e.g. {@code "headline"}, implying the default domain) or domain-qualified
	 * (e.g. {@code "payment.headline"}). Implementations that route by domain must split
	 * on the {@code .} separator themselves.
	 *
	 * @param code the message code; bare or domain-qualified ({@code "<domain>.<code>"})
	 * @param locale the locale to resolve for
	 * @return the resolved trans unit, or {@code null} if this source cannot resolve it
	 */
	TransUnitInterface resolveTransUnit(String code, Locale locale);
}
