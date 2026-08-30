// SPDX-License-Identifier: Apache-2.0
// Copyright 2024 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.catalog;

import io.github.alaugks.spring.messagesource.catalog.records.TransUnitInterface;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.util.Assert;

/**
 * Default {@link TransUnitHandlerInterface} implementation.
 */
public class DomainCodeTransUnitHandler implements TransUnitHandlerInterface {

	private final String defaultDomain;

	private final DomainCodeConcatInterface domainCodeConcat;

	public DomainCodeTransUnitHandler(String defaultDomain) {
		Assert.notNull(defaultDomain, "defaultDomain must not be null");
		this.defaultDomain = defaultDomain;
		this.domainCodeConcat = new DomainCodeConcat();
	}

	public DomainCodeTransUnitHandler(String defaultDomain, DomainCodeConcatInterface domainCodeConcat) {
		Assert.notNull(defaultDomain, "defaultDomain must not be null");
		Assert.notNull(domainCodeConcat, "domainCodeConcat must not be null");

		this.defaultDomain = defaultDomain;
		this.domainCodeConcat = domainCodeConcat;
	}

	@Override
	public void put(
		ConcurrentMap<Locale, ConcurrentMap<String, String>> catalogMap,
		TransUnitInterface transUnit
	) {
		Locale locale = transUnit.locale();
		if (locale.getLanguage().isEmpty()) {
			return;
		}

		ConcurrentMap<String, String> bucket = catalogMap.computeIfAbsent(
			locale, l -> new ConcurrentHashMap<>()
		);

		String code = transUnit.code();
		String value = transUnit.value();
		String domain = transUnit.domain();

		if (Objects.equals(domain, this.defaultDomain)) {
			bucket.putIfAbsent(code, value);
		}
		bucket.putIfAbsent(this.domainCodeConcat.generate(domain, code), value);
	}
}
