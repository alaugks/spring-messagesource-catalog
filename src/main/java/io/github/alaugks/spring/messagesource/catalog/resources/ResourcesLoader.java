// SPDX-License-Identifier: Apache-2.0
// Copyright 2024 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.catalog.resources;

import io.github.alaugks.spring.messagesource.catalog.exception.CatalogMessageSourceRuntimeException;
import io.github.alaugks.spring.messagesource.catalog.records.Filename;
import io.github.alaugks.spring.messagesource.catalog.records.TranslationFile;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.Assert;

/**
 * Discovers translation resources matching a {@link LocationPattern}, filters them by file
 * extension and loads each into a {@link TranslationFile} (domain + locale + raw bytes).
 *
 * <p>Used by sibling parser packages (XLIFF, JSON) as the file-loading stage that precedes
 * format-specific parsing.
 */
public class ResourcesLoader {

	/** Locale used when a file name carries no locale part. */
	private final Locale defaultLocale;

	/** Resource location patterns to scan for translation files. */
	private final Set<String> locationPatterns;

	/** Accepted file extensions, without leading dot. */
	private final List<String> fileExtensions;

	/**
	 * Creates a loader for the configured locations and file extensions.
	 *
	 * @param defaultLocale   the locale used when a file name carries no locale part;
	 *                        must not be {@code null}
	 * @param locationPattern the resource location patterns to scan
	 * @param fileExtensions  the file extensions to accept (without leading dot);
	 *                        must not be {@code null}
	 */
	public ResourcesLoader(Locale defaultLocale, LocationPattern locationPattern, List<String> fileExtensions) {
		Assert.notNull(defaultLocale, "Argument defaultLocale must not be null");
		Assert.notNull(fileExtensions, "Argument fileExtensions must not be null");

		this.defaultLocale = defaultLocale;
		this.locationPatterns = locationPattern.getLocationPattern();
		this.fileExtensions = fileExtensions;
	}

	/**
	 * Resolves every configured location pattern, keeps the resources whose extension is
	 * supported and whose file name parses successfully, and reads each into a
	 * {@link TranslationFile}.
	 *
	 * @return the loaded translation files
	 * @throws CatalogMessageSourceRuntimeException if resource resolution or reading fails
	 */
	public List<TranslationFile> getTranslationFiles() {
		try {
			List<TranslationFile> files = new ArrayList<>();
			PathMatchingResourcePatternResolver resourceLoader = new PathMatchingResourcePatternResolver();
			for (String locationPattern : this.locationPatterns) {
				Resource[] resources = resourceLoader.getResources(locationPattern);
				for (Resource resource : resources) {
					if (this.isFileExtensionSupported(resource)) {
						TranslationFile translationFile = this.parseFileName(resource);
						if (translationFile != null) {
							files.add(translationFile);
						}
					}
				}
			}

			return files;
		}
		catch (IOException e) {
			throw new CatalogMessageSourceRuntimeException(e);
		}
	}

	/**
	 * Builds a TranslationFile by parsing the resource file name and reading its bytes; returns
	 * null when the name does not match.
	 */
	private TranslationFile parseFileName(Resource resource) throws IOException {
		Filename filename = new ResourcesFileNameParser(resource.getFilename()).parse();

		if (filename != null) {
			try (InputStream inputStream = resource.getInputStream()) {
				return new TranslationFile(
						filename.domain(),
						filename.hasLocale()
								? filename.locale()
								: this.defaultLocale,
						inputStream.readAllBytes()
				);
			}
		}

		return null;
	}

	/**
	 * Checks whether the resource's file extension is in the configured allow-list.
	 */
	private boolean isFileExtensionSupported(Resource resource) {
		String fileName = resource.getFilename();
		return fileName != null && this.fileExtensions.contains(fileName.substring(fileName.lastIndexOf(".") + 1));
	}
}
