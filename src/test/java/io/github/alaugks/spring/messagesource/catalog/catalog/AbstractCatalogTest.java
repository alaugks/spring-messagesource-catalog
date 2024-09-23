package io.github.alaugks.spring.messagesource.catalog.catalog;

import java.util.ArrayList;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

class AbstractCatalogTest {

	@Test
	void test_set() {
		assertInstanceOf(NextCatalog.class, new MyCatalog().nextCatalog(new NextCatalog()));
	}

	@Test
	void test_getTransUnits() {
		assertEquals(new ArrayList<>(), new MyCatalog().getTransUnits());
	}

	@Test
	void test_resolveCode() {
		assertNull(new MyCatalog().resolveCode(Locale.forLanguageTag("en"), "foo"));
	}
}


class MyCatalog extends AbstractCatalog {

}

class NextCatalog extends AbstractCatalog {

}
