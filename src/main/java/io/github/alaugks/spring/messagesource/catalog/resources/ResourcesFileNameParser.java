// SPDX-License-Identifier: Apache-2.0
// Copyright 2024 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.catalog.resources;

import io.github.alaugks.spring.messagesource.catalog.records.Filename;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.util.Assert;

/**
 * Parses translation resource file names into a {@link Filename} record.
 *
 * <p>Matches case-insensitively; {@code _} and {@code -} both work as separators.
 * The file extension is ignored — only the leading segments are inspected.
 *
 * <p>Examples (the extension is shown for context but ignored by the parser):
 * <ul>
 *   <li>{@code messages.ext} &rarr; domain={@code messages}</li>
 *   <li>{@code messages_de.ext} &rarr; domain={@code messages}, language={@code de}</li>
 *   <li>{@code messages_en-US.ext} &rarr; domain={@code messages}, language={@code en}, region={@code US}</li>
 *   <li>{@code payment.ext} &rarr; domain={@code payment}</li>
 *   <li>{@code payment_de.ext} &rarr; domain={@code payment}, language={@code de}</li>
 *   <li>{@code payment_en-US.ext} &rarr; domain={@code payment}, language={@code en}, region={@code US}</li>
 * </ul>
 */
public class ResourcesFileNameParser {

	private static final Pattern PATTERN = Pattern.compile(
		"^(?<domain>[a-z0-9]+)(?:[_.-](?<language>[a-z]+)(?:[_-](?<region>[a-z]+))?)?\\.[a-z0-9]+$",
		Pattern.CASE_INSENSITIVE
	);

	private final String filename;

	/**
	 * Creates a parser bound to the given file name.
	 *
	 * @param filename the file name to parse; must not be {@code null}
	 */
	public ResourcesFileNameParser(String filename) {
		Assert.notNull(filename, "Argument filename must not be null");

		this.filename = filename;
	}

	/**
	 * Parses the bound file name into a {@link Filename}.
	 *
	 * @return the parsed {@link Filename}, or {@code null} if the file name does not match
	 *         the expected pattern
	 */
	public Filename parse() {
		Matcher matcher = PATTERN.matcher(this.filename);

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
