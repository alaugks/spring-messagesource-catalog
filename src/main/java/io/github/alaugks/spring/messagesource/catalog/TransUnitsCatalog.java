package io.github.alaugks.spring.messagesource.catalog;

import java.util.List;

import io.github.alaugks.spring.messagesource.catalog.catalog.AbstractCatalog;
import io.github.alaugks.spring.messagesource.catalog.records.TransUnitInterface;

public class TransUnitsCatalog extends AbstractCatalog {

	List<TransUnitInterface> transUnits;

	public TransUnitsCatalog(List<TransUnitInterface> transUnits) {
		this.transUnits = transUnits;
	}

	@Override
	public List<TransUnitInterface> getTransUnits() {
		return this.transUnits;
	}

}
