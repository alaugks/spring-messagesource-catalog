package io.github.alaugks.spring.messagesource.catalog.fixtures;


import io.github.alaugks.spring.messagesource.catalog.catalog.CatalogAbstract;
import io.github.alaugks.spring.messagesource.catalog.records.TransUnit;
import java.util.List;
import java.util.Locale;

public class MockCatalog extends CatalogAbstract {

	List<TransUnit> transUnits = List.of();
	String resolveCode = null;

	@Override
	public List<TransUnit> getTransUnits() {
		return transUnits;
	}

	public MockCatalog setTransUnits(List<TransUnit> transUnits) {
		this.transUnits = transUnits;
		return this;
	}

	@Override
	public String resolveCode(Locale locale, String code) {
		return this.resolveCode;
	}

	public MockCatalog setResolveCode(String resolveCode) {
		this.resolveCode = resolveCode;
		return this;
	}

	@Override
	public void build() {

	}
}
