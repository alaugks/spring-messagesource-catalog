package io.github.alaugks.spring.messagesource.catalog.fxitures;

import io.github.alaugks.spring.messagesource.catalog.catalog.AbstractCatalog;
import io.github.alaugks.spring.messagesource.catalog.records.TransUnit;
import io.github.alaugks.spring.messagesource.catalog.records.TransUnitInterface;
import java.util.Locale;
import java.util.Objects;

public class FooBarCatalog extends AbstractCatalog {

    @Override
    public TransUnitInterface resolveTransUnit(String code, Locale locale) {
        if (Objects.equals(code, "dummy") || Objects.equals(code, "messages.dummy")) {
            return new TransUnit(locale, code, "foobar_value");
        }
        return null;
    }
}
