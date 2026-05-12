/*
 * Copyright 2024-2025 André Laugks <alaugks@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.alaugks.spring.messagesource.catalog.resources;

import io.github.alaugks.spring.messagesource.catalog.records.Filename;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.util.Assert;

/**
 * Parses translation resource file names into a {@link Filename} record.
 *
 * <p>Supported shapes (case-insensitive; {@code _} and {@code -} both work as separators):
 * <ul>
 *   <li>{@code domain}</li>
 *   <li>{@code domain_language}</li>
 *   <li>{@code domain_language_region}</li>
 * </ul>
 * The file extension is ignored — only the leading segments are inspected.
 */
public class ResourcesFileNameParser {

	private static final Pattern PATTERN = Pattern.compile(
			"^(?<domain>[a-z0-9]+)(?:([_-](?<language>[a-z]+))(?:[_-](?<region>[a-z]+))?)?",
			Pattern.CASE_INSENSITIVE
	);

	private final String filename;

	/**
	 * @param filename the file name to parse; must not be {@code null}
	 */
	public ResourcesFileNameParser(String filename) {
		Assert.notNull(filename, "Argument filename must not be null");

		this.filename = filename;
	}

	/**
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
