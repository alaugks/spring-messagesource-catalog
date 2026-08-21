// SPDX-License-Identifier: Apache-2.0
// Copyright 2024 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.catalog.resources;

import io.github.alaugks.spring.messagesource.catalog.records.Filename;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

/**
 * Parses translation resource file names into a {@link Filename} record.
 *
 * <p>Matches case-insensitively. The domain and the locale part may be separated by {@code _}, {@code -}
 * or {@code .}; language and region may be separated by {@code _} or {@code -}. The file extension is
 * ignored — only the leading segments are inspected.
 *
 * <p>Examples (any of the separator combinations above works the same way):
 * <ul>
 *   <li>{@code messages.ext} &rarr; domain={@code messages}</li>
 *   <li>{@code messages_de.ext} &rarr; domain={@code messages}, language={@code de}</li>
 *   <li>{@code messages-de.ext} &rarr; domain={@code messages}, language={@code de}</li>
 *   <li>{@code messages.de.ext} &rarr; domain={@code messages}, language={@code de}</li>
 *   <li>{@code messages_en_US.ext} &rarr; domain={@code messages}, language={@code en}, region={@code US}</li>
 *   <li>{@code messages_en-US.ext} &rarr; domain={@code messages}, language={@code en}, region={@code US}</li>
 *   <li>{@code messages-en_US.ext} &rarr; domain={@code messages}, language={@code en}, region={@code US}</li>
 *   <li>{@code messages-en-US.ext} &rarr; domain={@code messages}, language={@code en}, region={@code US}</li>
 *   <li>{@code messages.en_US.ext} &rarr; domain={@code messages}, language={@code en}, region={@code US}</li>
 *   <li>{@code messages.en-US.ext} &rarr; domain={@code messages}, language={@code en}, region={@code US}</li>
 * </ul>
 */
public class ResourceFileNameParser implements ResourceFileNameParserInterface {

	/** Matches domain, optional language and optional region in a resource file name. */
	private static final Pattern PATTERN = Pattern.compile(
		"^(?<domain>[a-z0-9]+)(?:[_.-](?<language>[a-z]+)(?:[_-](?<region>[a-z]+))?)?\\.[a-z0-9]+$",
		Pattern.CASE_INSENSITIVE
	);

	@Override
	public @Nullable Filename parse(Resource resource) {
		String filename = resource.getFilename();
		Assert.notNull(filename, "Argument filename must not be null");

		Matcher matcher = PATTERN.matcher(filename);

		if (matcher.find()) {
			return new Filename(
					matcher.group("domain"),
					matcher.group("language"),
					matcher.group("region")
			);
		}

		return null;
	}
}
