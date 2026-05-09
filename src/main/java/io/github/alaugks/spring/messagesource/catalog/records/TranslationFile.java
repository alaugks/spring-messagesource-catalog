/*
 * Copyright 2024-2025 André Laugks <alaugks@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.alaugks.spring.messagesource.catalog.records;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;import org.springframework.lang.NonNull;

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
