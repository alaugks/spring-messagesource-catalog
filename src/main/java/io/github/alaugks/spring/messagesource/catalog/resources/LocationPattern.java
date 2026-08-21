// SPDX-License-Identifier: Apache-2.0
// Copyright 2024 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.catalog.resources;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.util.Assert;

/**
 * Formerly held the Spring resource location patterns (e.g. {@code classpath:translations/*})
 * used by {@link ResourceLoaderBuilder} to discover translation files. {@link ResourceLoaderBuilder} now
 * takes the patterns directly; this class is no longer accepted anywhere.
 *
 * <p>Migration:
 *
 * <pre>{@code
 * // before
 * new ResourcesLoader(
 *     locale,
 *     new LocationPattern("classpath:translations/*"),
 *     List.of("ext")
 * );
 *
 * // now
 * ResourceLoader
 *     .builder(locale, List.of("classpath:translations/*"))
 *     .fileExtensions(List.of("ext"))
 *     .build();
 * }</pre>
 *
 * @deprecated Pass the location patterns directly as
 * {@code List<String>} to {@link ResourceLoaderBuilder#builder(java.util.Locale, List)}.
 */
@Deprecated()
public class LocationPattern {

	private final Set<String> locationPatterns;

	/**
	 * Convenience constructor for a single location pattern.
	 *
	 * @param locationPattern the location pattern; must not be {@code null}
	 * @deprecated since 0.10.0, for removal. Pass {@code List.of(locationPattern)} directly
	 * to {@link ResourceLoaderBuilder#builder(java.util.Locale, List)} instead.
	 */
	public LocationPattern(String locationPattern) {
		this(List.of(locationPattern));
	}

	/**
	 * Creates a {@link LocationPattern} from the given list; duplicate entries are eliminated.
	 *
	 * @param locationPatterns the location patterns; must not be {@code null}
	 * @deprecated since 0.10.0, for removal. Pass the list directly to
	 * {@link ResourceLoaderBuilder#builder(java.util.Locale, List)} instead; duplicate entries are
	 * eliminated there.
	 */
	public LocationPattern(List<String> locationPatterns) {
		Assert.notNull(locationPatterns, "Argument locationPatterns must not be null");
		this.locationPatterns = new HashSet<>(locationPatterns);
	}

	/**
	 * {@return the configured location patterns (deduplicated)}
	 */
	public Set<String> getLocationPattern() {
		return this.locationPatterns;
	}

	/**
	 * {@return the configured location patterns as a list (deduplicated)}
	 */
	public List<String> getLocationPatterns() {
		return this.locationPatterns.stream().toList();
	}
}
