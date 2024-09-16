package io.github.alaugks.spring.messagesource.catalog.catalog;

import java.util.List;
import java.util.Locale;

import io.github.alaugks.spring.messagesource.catalog.records.TransUnit;

public interface CatalogInterface {

	CatalogInterface nextHandler(CatalogInterface handler);

	List<TransUnit> getTransUnits();

	String resolveCode(Locale locale, String code);

	void build();
}
