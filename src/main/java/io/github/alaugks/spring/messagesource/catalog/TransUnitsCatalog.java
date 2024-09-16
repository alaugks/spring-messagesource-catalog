package io.github.alaugks.spring.messagesource.catalog;

import java.util.List;

import io.github.alaugks.spring.messagesource.catalog.catalog.CatalogAbstract;
import io.github.alaugks.spring.messagesource.catalog.records.TransUnit;

public class TransUnitsCatalog extends CatalogAbstract {

	List<TransUnit> transUnits;

	public TransUnitsCatalog(List<TransUnit> transUnits) {
		this.transUnits = transUnits;
	}

	@Override
	public List<TransUnit> getTransUnits() {
		return this.transUnits;
	}

}
