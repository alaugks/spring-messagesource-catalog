// SPDX-License-Identifier: Apache-2.0
// Copyright 2024 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.catalog.records;

import org.jspecify.annotations.Nullable;

/**
 * A single translation entry: a {@code (locale, domain, code) -> value} tuple.
 */
public interface TransUnitDomainInterface {

	@Nullable String domain();
}
