package io.github.alaugks.spring.messagesource.catalog;

import java.util.List;
import java.util.Locale;

import io.github.alaugks.spring.messagesource.catalog.records.TransUnit;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings({"java:S4144"})
@TestMethodOrder(OrderAnnotation.class)
class CatalogMessageSourceBuilderTest {

	public final Locale locale = Locale.forLanguageTag("en");

	@Test
	void test_builder_withList() {
		List<TransUnit> transUnits = List.of(
				new TransUnit(this.locale, "key", "messages_value")
		);

		assertEquals(
				"messages_value",
				CatalogMessageSourceBuilder
						.builder(transUnits, this.locale)
						.build().getMessage("key", null, this.locale)
		);
	}

	@Test
	void test_builder_withCatalogInterface() {
		List<TransUnit> transUnits = List.of(
				new TransUnit(this.locale, "key", "messages_value")
		);

		assertEquals(
				"messages_value",
				CatalogMessageSourceBuilder
						.builder(new TransUnitsCatalog(transUnits), this.locale)
						.build()
						.getMessage("key", null, this.locale)
		);
	}

	@Test
	void test_withSetDefaultDomain() {
		List<TransUnit> transUnits = List.of(
				new TransUnit(this.locale, "key", "messages_value"),
				new TransUnit(this.locale, "key", "foo_value", "foo")
		);

		assertEquals(
				"foo_value",
				CatalogMessageSourceBuilder
						.builder(new TransUnitsCatalog(transUnits), this.locale)
						.defaultDomain("foo")
						.build()
						.getMessage("key", null, this.locale)
		);
	}
}
