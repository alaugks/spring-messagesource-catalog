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

package io.github.alaugks.spring.messagesource.catalog.exception;

/**
 * Unchecked wrapper for failures inside the catalog pipeline (resource loading, locale
 * parsing, etc.). Thrown so callers do not need to handle checked exceptions from internal
 * I/O or parsing operations.
 */
public class CatalogMessageSourceRuntimeException extends RuntimeException {

	/**
	 * @param cause the underlying checked exception being wrapped
	 */
	public CatalogMessageSourceRuntimeException(Throwable cause) {
		super(cause);
	}
}
