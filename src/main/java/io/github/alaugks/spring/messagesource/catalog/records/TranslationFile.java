// SPDX-License-Identifier: Apache-2.0
// Copyright 2024 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.catalog.records;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;import org.springframework.lang.NonNull;

/**
 * Raw, loaded translation resource: domain, locale and file bytes.
 *
 * <p>{@code equals}/{@code hashCode}/{@code toString} are overridden so the {@code byte[]}
 * content is compared by value rather than by identity.
 *
 * @param domain  the domain the file belongs to
 * @param locale  the locale parsed from the file name, or the default locale when none was given
 * @param content the raw file bytes
 */
public record TranslationFile(String domain, Locale locale, byte[] content) {

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof TranslationFile other)) {
			return false;
		}
		return Objects.equals(domain, other.domain)
				&& Objects.equals(locale, other.locale)
				&& Arrays.equals(content, other.content);
	}

	@Override
	public int hashCode() {
		return Objects.hash(domain, locale, Arrays.hashCode(content));
	}

	@Override
	@NonNull
	public String toString() {
		return "TranslationFile[domain=" + domain
				+ ", locale=" + locale
				+ ", content=" + (content == null ? "null" : content.length + " bytes")
				+ "]";
	}
}
