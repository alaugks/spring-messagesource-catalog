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
 * <p>Multiple sources are aggregated additively by the builder, which composes them in the
 * order they were added (see {@code CatalogMessageSourceBuilder.Builder#addSource}). Eager
 * units from all sources are concatenated; on a lazy lookup the sources are consulted in
 * order and the first non-{@code null} result wins. Each source therefore only answers for
 * what it knows and returns {@code null} otherwise — it does not delegate to other sources.
 */
public interface CatalogInterface {

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
	 * <p>The {@code code} parameter is passed through as-is from the caller. It may be
	 * given without a domain prefix (e.g. {@code "headline"}, implying the default domain)
	 * or with one (e.g. {@code "payment.headline"}). Implementations that route by domain
	 * must split on the {@code .} separator themselves.
	 *
	 * @param code the message code, with or without a domain prefix ({@code "<domain>.<code>"})
	 * @param locale the locale to resolve for
	 * @return the resolved trans unit, or {@code null} if this source cannot resolve it
	 */
	TransUnitInterface resolveTransUnit(String code, Locale locale);
}
