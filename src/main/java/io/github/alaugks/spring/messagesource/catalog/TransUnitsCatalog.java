package io.github.alaugks.spring.messagesource.catalog;

import java.util.List;

import io.github.alaugks.spring.messagesource.catalog.catalog.AbstractCatalog;
import io.github.alaugks.spring.messagesource.catalog.records.TransUnit;

public class TransUnitsCatalog extends AbstractCatalog {

	List<TransUnit> transUnits;

	public TransUnitsCatalog(List<TransUnit> transUnits) {
		this.transUnits = transUnits;
	}

	@Override
	public List<TransUnit> getTransUnits() {
		return this.transUnits;
	}

}
