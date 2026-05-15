// SPDX-License-Identifier: Apache-2.0
// Copyright 2024 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.catalog.exception;

/**
 * Unchecked wrapper for failures inside the catalog pipeline (resource loading, locale
 * parsing, etc.). Thrown so callers do not need to handle checked exceptions from internal
 * I/O or parsing operations.
 */
public class CatalogMessageSourceRuntimeException extends RuntimeException {

	/**
	 * Wraps the given throwable as an unchecked catalog exception.
	 *
	 * @param cause the underlying checked exception being wrapped
	 */
	public CatalogMessageSourceRuntimeException(Throwable cause) {
		super(cause);
	}
}
