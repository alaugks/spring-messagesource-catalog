package io.github.alaugks.spring.messagesource.catalog.catalog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import io.github.alaugks.spring.messagesource.catalog.records.TransUnitInterface;

public abstract class AbstractCatalog implements CatalogInterface {

	protected CatalogInterface nextCatalog;

	public CatalogInterface nextCatalog(CatalogInterface catalog) {
		this.nextCatalog = catalog;
		return catalog;
	}

	public List<TransUnitInterface> getTransUnits() {
		if (this.nextCatalog == null) {
			return new ArrayList<>();
		}

		return this.nextCatalog.getTransUnits();
	}

	public String resolveCode(String code, Locale locale) {
		if (this.nextCatalog == null) {
			return null;
		}

		return this.nextCatalog.resolveCode(code, locale);
	}

	public void build() {
		if (this.nextCatalog != null) {
			this.nextCatalog.build();
		}
	}

	protected static String concatCode(String domain, String code) {
		return Optional.ofNullable(domain).orElse(Catalog.DEFAULT_DOMAIN) + "." + code;
	}
}
