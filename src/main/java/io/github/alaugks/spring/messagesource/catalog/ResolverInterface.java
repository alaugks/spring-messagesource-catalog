// SPDX-License-Identifier: Apache-2.0
// Copyright 2024 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.catalog;

import java.util.Locale;
import java.util.ResourceBundle;
import org.jspecify.annotations.Nullable;

/**
 * Strategy for building and resolving domain-qualified message codes.
 *
 * <p>Domains and codes are combined into a single key (e.g. {@code "domain.code"});
 * implementations decide how that key is built and how a {@link ResourceBundle} is probed for
 * it.
 */
public interface ResolverInterface {

	/**
	 * Resolves {@code code} against the given bundle.
	 *
	 * @param bundle the bundle to resolve against
	 * @param code the message code, without a domain prefix
	 * @param locale the locale being resolved for
	 * @return the resolved value, or {@code null} if the code cannot be resolved
	 */
	@Nullable String resolve(ResourceBundle bundle, String code, Locale locale);
}
