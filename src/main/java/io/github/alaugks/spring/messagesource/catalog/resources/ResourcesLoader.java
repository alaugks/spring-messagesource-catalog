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

package io.github.alaugks.spring.messagesource.catalog.resources;

import io.github.alaugks.spring.messagesource.catalog.exception.CatalogMessageSourceRuntimeException;
import io.github.alaugks.spring.messagesource.catalog.records.Filename;
import io.github.alaugks.spring.messagesource.catalog.records.TranslationFile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.Assert;

public class ResourcesLoader {

	private final Locale defaultLocale;

	private final Set<String> locationPatterns;

	private final List<String> fileExtensions;

	public ResourcesLoader(Locale defaultLocale, LocationPattern locationPattern, List<String> fileExtensions) {
		this(defaultLocale, locationPattern.getLocationPattern(), fileExtensions);
	}

	/**
	 * @deprecated
	 * Will replace with {@link ResourcesLoader#ResourcesLoader(Locale defaultLocale, LocationPattern locationPattern, List<String> fileExtensions)}
	 */
	@Deprecated(since = "0.7.0", forRemoval = true)
	public ResourcesLoader(Locale defaultLocale, Set<String> locationPatterns, List<String> fileExtensions) {
		Assert.notNull(defaultLocale, "Argument defaultLocale must not be null");
		Assert.notNull(locationPatterns, "Argument locationPatterns must not be null");
		Assert.notNull(fileExtensions, "Argument fileExtensions must not be null");

		this.defaultLocale = defaultLocale;
		this.locationPatterns = locationPatterns;
		this.fileExtensions = fileExtensions;
	}

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

	private TranslationFile parseFileName(Resource resource) throws IOException {
		Filename filename = new ResourcesFileNameParser(resource.getFilename()).parse();

		if (filename != null) {
			return new TranslationFile(
					filename.domain(),
					filename.hasLocale()
							? filename.locale()
							: this.defaultLocale,
					resource.getInputStream()
			);
		}

		return null;
	}

	private boolean isFileExtensionSupported(Resource resource) {
		String fileName = resource.getFilename();

		if (fileName == null) {
			return false;
		}

		return fileExtensions.contains(fileName.substring(fileName.lastIndexOf(".") + 1));
	}
}
