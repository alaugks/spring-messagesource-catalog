package io.github.alaugks.spring.messagesource.catalog.resources;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.util.Assert;

public class LocationPattern {

	private final Set<String> locationPatterns;

	public LocationPattern(String locationPattern) {
		this(List.of(locationPattern));
	}

	public LocationPattern(List<String> locationPatterns) {
		Assert.notNull(locationPatterns, "Argument locationPatterns must not be null");
		this.locationPatterns = new HashSet<>(locationPatterns);
	}

	public Set<String> getLocationPatterns() {
		return locationPatterns;
	}
}
