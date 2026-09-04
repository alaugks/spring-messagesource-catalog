// SPDX-License-Identifier: Apache-2.0
// Copyright 2024 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.catalog.records;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Raw, loaded translation resource: locale and file bytes.
 *
 * <p>{@code equals}/{@code hashCode}/{@code toString} are overridden so the {@code byte[]}
 * content is compared by value rather than by identity.
 *
 * @param locale  the locale parsed from the file name, or the default locale when none was given
 * @param content the raw file bytes
 */
public record TranslationFile(Locale locale, byte[] content) implements TranslationFileInterface {

	@Override
	public boolean equals(@Nullable Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof TranslationFile other)) {
			return false;
		}
		return Objects.equals(this.locale, other.locale)
				&& Arrays.equals(this.content, other.content);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.locale, Arrays.hashCode(this.content));
	}

	@Override
	public String toString() {
		return "TranslationFile["
				+ "locale=" + this.locale
				+ ", content=" + (this.content == null ? "null" : this.content.length + " bytes")
				+ "]";
	}
}
