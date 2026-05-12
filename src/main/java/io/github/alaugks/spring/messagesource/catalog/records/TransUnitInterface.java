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

import java.util.Locale;

/**
 * A single translation entry: a {@code (locale, domain, code) -> value} tuple.
 */
public interface TransUnitInterface {

	/**
	 * @return the locale this translation belongs to
	 */
	Locale locale();

	/**
	 * @return the message code (bare, without domain prefix)
	 */
	String code();

	/**
	 * @return the translated text
	 */
	String value();

	/**
	 * @return the domain this trans unit belongs to, or {@code null} for the default domain
	 */
	String domain();
}
