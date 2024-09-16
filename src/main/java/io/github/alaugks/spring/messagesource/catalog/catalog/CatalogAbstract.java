package io.github.alaugks.spring.messagesource.catalog.catalog;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import io.github.alaugks.spring.messagesource.catalog.records.TransUnit;

public abstract class CatalogAbstract implements CatalogInterface {

	protected CatalogInterface nextHandler;

	public CatalogInterface nextHandler(CatalogInterface handler) {
		this.nextHandler = handler;
		return handler;
	}

	public List<TransUnit> getTransUnits() {
		if (this.nextHandler == null) {
			return Collections.emptyList();
		}

		return this.nextHandler.getTransUnits();
	}

	public String resolveCode(Locale locale, String code) {
		if (this.nextHandler == null) {
			return null;
		}

		return this.nextHandler.resolveCode(locale, code);
	}

	public void build() {
		if (this.nextHandler != null) {
			this.nextHandler.build();
		}
	}

	protected static String concatCode(String domain, String code) {
		return Optional.ofNullable(domain).orElse(Catalog.DEFAULT_DOMAIN) + "." + code;
	}
}
