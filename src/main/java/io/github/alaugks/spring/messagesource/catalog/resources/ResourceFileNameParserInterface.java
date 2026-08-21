// SPDX-License-Identifier: Apache-2.0
// Copyright 2024 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.catalog.resources;

import io.github.alaugks.spring.messagesource.catalog.records.Filename;
import org.jspecify.annotations.Nullable;
import org.springframework.core.io.Resource;

/**
 * Parses a translation resource file name into a {@link Filename} record.
 *
 * <p>Functional interface: a custom parser can be passed to {@link ResourceLoaderBuilder} as a
 * class implementation or as a lambda.
 */
@FunctionalInterface
public interface ResourceFileNameParserInterface {

	/**
	 * Parses the given file name into a {@link Filename}.
	 *
	 * @param resource the resource whose file name is parsed
	 * @return the parsed {@link Filename}, or {@code null} if the file name does not match the expected pattern
	 */
	@Nullable Filename parse(Resource resource);
}
