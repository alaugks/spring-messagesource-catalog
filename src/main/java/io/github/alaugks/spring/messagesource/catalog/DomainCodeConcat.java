package io.github.alaugks.spring.messagesource.catalog;

import java.util.Optional;
import org.jspecify.annotations.Nullable;

import static io.github.alaugks.spring.messagesource.catalog.CatalogMessageSourceBuilder.DEFAULT_DOMAIN;

public class DomainCodeConcat implements DomainCodeConcatInterface {

	private final String domainDivider;

	public DomainCodeConcat() {
		this(".");
	}

	public DomainCodeConcat(String domainDivider) {
		this.domainDivider = domainDivider;
	}

	@Override
	public String generate(@Nullable String domain, String code) {
		return Optional.ofNullable(domain).orElse(DEFAULT_DOMAIN) + this.domainDivider + code;
	}
}
