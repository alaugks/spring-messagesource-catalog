// SPDX-License-Identifier: Apache-2.0
// Copyright 2024 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.catalog.resources;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.util.Assert;

/**
 * Holds one or more Spring resource location patterns (e.g. {@code classpath:/translations/*})
 * used by {@link ResourcesLoader} to discover translation files. Duplicates are eliminated by
 * storing the patterns in a {@link Set}.
 */
public class LocationPattern {

	private final Set<String> locationPatterns;

	/**
	 * Convenience constructor for a single location pattern.
	 *
	 * @param locationPattern the location pattern; must not be {@code null}
	 */
	public LocationPattern(String locationPattern) {
		this(List.of(locationPattern));
	}

	/**
	 * Creates a {@link LocationPattern} from the given list; duplicate entries are eliminated.
	 *
	 * @param locationPatterns the location patterns; must not be {@code null}
	 */
	public LocationPattern(List<String> locationPatterns) {
		Assert.notNull(locationPatterns, "Argument locationPatterns must not be null");
		this.locationPatterns = new HashSet<>(locationPatterns);
	}

	/**
	 * Returns the configured location patterns.
	 *
	 * @return the configured location patterns (deduplicated)
	 */
	public Set<String> getLocationPattern() {
		return locationPatterns;
	}
}
