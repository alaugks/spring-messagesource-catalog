package io.github.alaugks.spring.messagesource.catalog;

import org.jspecify.annotations.Nullable;

public interface DomainCodeConcatInterface {

	String generate(@Nullable String domain, String code);
}
