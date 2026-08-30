// SPDX-License-Identifier: Apache-2.0
// Copyright 2024 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.catalog;

import java.util.ResourceBundle;
import org.jspecify.annotations.Nullable;

import static io.github.alaugks.spring.messagesource.catalog.CatalogMessageSourceBuilder.DEFAULT_DOMAIN;

/**
 * Default {@link ResolverInterface} implementation: joins domain and code with a
 * {@code "."} separator.
 */
public class DomainCodeResolver implements ResolverInterface {

	private final DomainCodeConcatInterface domainCodeConcat;

	public DomainCodeResolver() {
		this(new DomainCodeConcat());
	}

	public DomainCodeResolver(DomainCodeConcatInterface domainCodeConcat) {
		this.domainCodeConcat = domainCodeConcat;
	}

	@Override
	public @Nullable String resolve(ResourceBundle bundle, String code) {

		if (bundle.containsKey(code)) {
			return bundle.getString(code);
		}

		String domainCode = this.domainCodeConcat.generate(DEFAULT_DOMAIN, code);
		if (bundle.containsKey(domainCode)) {
			return bundle.getString(domainCode);
		}

		return null;
	}
}
