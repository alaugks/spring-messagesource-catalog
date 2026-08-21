// SPDX-License-Identifier: Apache-2.0
// Copyright 2024 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.catalog.resources;

import io.github.alaugks.spring.messagesource.catalog.exception.CatalogMessageSourceRuntimeException;
import io.github.alaugks.spring.messagesource.catalog.records.Filename;
import io.github.alaugks.spring.messagesource.catalog.records.TranslationFile;
import io.github.alaugks.spring.messagesource.catalog.records.TranslationFileInterface;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.Assert;

/**
 * Discovers translation resources matching the configured location patterns (e.g.
 * {@code classpath:translations/*}), optionally filters them by file extension and loads each
 * into a {@link TranslationFile} (domain + locale + raw bytes). Duplicate patterns are
 * eliminated. Without configured file extensions all discovered resources are loaded.
 *
 * <p>The patterns are resolved with Spring's
 * {@link org.springframework.core.io.support.PathMatchingResourcePatternResolver}, so they
 * must follow that resolver's conventions, e.g. the {@code classpath*:} prefix to search
 * across all matching classpath locations and Ant-style wildcards ({@code *}, {@code **},
 * {@code ?}) for path matching.
 *
 * <p>Use {@link #builder(Locale, List)} to obtain a {@link Builder} and configure the
 * optional file extension filter and a custom file name parser.
 *
 * <p>Used by sibling parser packages (XLIFF, JSON) as the file-loading stage that precedes
 * format-specific parsing.
 */
public class ResourceLoaderBuilder implements ResourceLoaderInterface {

	/** Locale used when a file name carries no locale part. */
	private final Locale defaultLocale;

	/** Location patterns to scan, deduplicated. */
	private final Set<String> locationPatterns;

	/** Accepted file extensions, without leading dot; null disables the filter. */
	private final @Nullable List<String> fileExtensions;

	/** Parser that derives domain and locale from a resource file name. */
	private final ResourceFileNameParserInterface fileNameParser;

	private ResourceLoaderBuilder(
			Locale defaultLocale,
			Set<String> locationPatterns,
			ResourceFileNameParserInterface fileNameParser,
			@Nullable List<String> fileExtensions
	) {
		this.defaultLocale = defaultLocale;
		this.locationPatterns = locationPatterns;
		this.fileExtensions = fileExtensions;
		this.fileNameParser = fileNameParser;
	}

	/**
	 * Creates a new {@link Builder}.
	 *
	 * @param defaultLocale   the locale used when a file name carries no locale part; must not be {@code null}
	 * @param locationPattern the resource location pattern to scan; must not be {@code null}
	 * @return a new {@link Builder} instance
	 */
	public static Builder builder(Locale defaultLocale, String locationPattern) {
		return new Builder(defaultLocale, List.of(locationPattern));
	}

	/**
	 * Creates a new {@link Builder}.
	 *
	 * @param defaultLocale    the locale used when a file name carries no locale part; must not be {@code null}
	 * @param locationPatterns the resource location patterns to scan; must not be {@code null}
	 * @return a new {@link Builder} instance
	 */
	public static Builder builder(Locale defaultLocale, List<String> locationPatterns) {
		return new Builder(defaultLocale, locationPatterns);
	}

	/**
	 * Resolves the location patterns into resources.
	 */
	private Resource[] getResources(Set<String> locationPatterns) {
		try {
			ResourcePatternResolver resourceLoader = new PathMatchingResourcePatternResolver();
			List<Resource> resources = new ArrayList<>();
			for (String locationPattern : locationPatterns) {
				resources.addAll(Arrays.asList(resourceLoader.getResources(locationPattern)));
			}

			return resources.toArray(new Resource[0]);
		} catch (IOException e) {
			throw new CatalogMessageSourceRuntimeException(e);
		}
	}

	/**
	 * Keeps the resources whose extension is supported and whose file name parses, and reads
	 * each into a {@link TranslationFile}.
	 *
	 * @throws CatalogMessageSourceRuntimeException if a pattern cannot be resolved or a resource cannot be read
	 */
	@Override
	public List<TranslationFileInterface> getTranslationFiles() {
		try {
			List<TranslationFileInterface> files = new ArrayList<>();
			Resource[] resources = this.getResources(this.locationPatterns);
			for (Resource resource : resources) {
				if (this.isFileExtensionSupported(resource)) {
					TranslationFileInterface translationFile = this.parseFileName(resource);
					if (translationFile != null) {
						files.add(translationFile);
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
	private @Nullable TranslationFileInterface parseFileName(Resource resource) throws IOException {
		Filename filename = this.fileNameParser.parse(resource);

		if (filename != null) {
			try (InputStream inputStream = resource.getInputStream()) {
				Locale locale = filename.locale();
				return new TranslationFile(
						filename.domain(),
						locale != null ? locale : this.defaultLocale,
						inputStream.readAllBytes()
				);
			}
		}

		return null;
	}

	/**
	 * Checks whether the resource's file extension is in the configured allow-list; without
	 * a configured allow-list every resource passes.
	 */
	private boolean isFileExtensionSupported(Resource resource) {
		if (this.fileExtensions == null) {
			return true;
		}

		String fileName = resource.getFilename();
		return fileName != null && this.fileExtensions.contains(fileName.substring(fileName.lastIndexOf(".") + 1));
	}

	/**
	 * Fluent builder for {@link ResourceLoaderBuilder}. Holds the default locale and the location
	 * patterns; the file extension filter and a custom file name parser are optional.
	 */
	public static final class Builder {

		/** Locale used when a file name carries no locale part. */
		private final Locale defaultLocale;

		/** Location patterns to scan, deduplicated. */
		private final Set<String> locationPatterns;

		/** Accepted file extensions, without leading dot; null disables the filter. */
		private @Nullable List<String> fileExtensions;

		/** Parser that derives domain and locale from a resource file name. */
		private ResourceFileNameParserInterface fileNameParser = new ResourceFileNameParser();

		/**
		 * Creates a new builder.
		 */
		private Builder(Locale defaultLocale, List<String> locationPatterns) {
			Assert.notNull(defaultLocale, "Argument defaultLocale must not be null");
			Assert.notNull(locationPatterns, "Argument locationPatterns must not be null");

			this.defaultLocale = defaultLocale;
			this.locationPatterns = new HashSet<>(locationPatterns);
		}

		/**
		 * Restricts loading to resources with one of the given file extensions. Without this
		 * setting all discovered resources are loaded.
		 *
		 * @param fileExtensions the file extensions to accept (without leading dot);
		 *                       must not be {@code null}
		 * @return this builder instance
		 */
		public Builder fileExtensions(List<String> fileExtensions) {
			Assert.notNull(fileExtensions, "Argument fileExtensions must not be null");

			this.fileExtensions = fileExtensions;
			return this;
		}

		/**
		 * Sets a custom file name parser. Defaults to {@link ResourceFileNameParser}.
		 *
		 * @param fileNameParser the parser that derives domain and locale from a file name;
		 *                       must not be {@code null}
		 * @return this builder instance
		 */
		public Builder fileNameParser(ResourceFileNameParserInterface fileNameParser) {
			Assert.notNull(fileNameParser, "Argument fileNameParser must not be null");

			this.fileNameParser = fileNameParser;
			return this;
		}

		/**
		 * Builds a {@link ResourceLoaderBuilder} from the configured values. The location patterns
		 * are resolved on each call to {@link ResourceLoaderBuilder#getTranslationFiles()}.
		 *
		 * @return a new {@link ResourceLoaderBuilder} instance
		 */
		public ResourceLoaderBuilder build() {
			return new ResourceLoaderBuilder(
					this.defaultLocale,
					this.locationPatterns,
					this.fileNameParser,
					this.fileExtensions
			);
		}
	}
}
