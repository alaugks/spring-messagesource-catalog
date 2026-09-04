// SPDX-License-Identifier: Apache-2.0
// Copyright 2024 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.catalog;

import io.github.alaugks.spring.messagesource.catalog.catalog.CatalogInterface;
import io.github.alaugks.spring.messagesource.catalog.records.TransUnitInterface;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.jspecify.annotations.Nullable;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * Spring {@link org.springframework.context.MessageSource} backed by one or more
 * {@link CatalogInterface} sources.
 *
 * <p>Translation units from all configured sources are aggregated into an in-memory map
 * during construction. Codes are resolved from that map first; on a miss, the configured
 * sources are consulted in order via {@link CatalogInterface#resolveTransUnit(String, Locale)}
 * for late-binding sources, and the result is cached.
 *
 * <p>Use {@link #builder(Locale, List)} or {@link #builder(Locale, CatalogInterface)}
 * to obtain a {@link Builder} and configure additional sources via
 * {@link Builder#addSource(CatalogInterface)}.
 */
public class CatalogMessageSourceBuilder implements MessageSource {

	/** Default domain used when none is configured: {@value}. */
	public static final String DEFAULT_DOMAIN = "messages";

	/** Internal base name for the ResourceBundle that serves the catalog. */
	private static final String BUNDLE_BASE_NAME = CatalogMessageSourceBuilder.class.getPackageName();

	/** Internal bundle format name handled by CatalogControl. */
	private static final String BUNDLE_FORMAT = "catalog";

	/** Loads and merges the locale's catalog buckets into a bundle. */
	private final ResourceBundle.Control control = new CatalogControl();

	/** Per-instance cache of resolved bundles, keyed by locale. */
	private final ConcurrentMap<Locale, ResourceBundle> bundles = new ConcurrentHashMap<>();

	/** Resolved messages, keyed by locale and then by code. */
	private final ConcurrentMap<Locale, ConcurrentMap<String, String>> catalogMap;

	/** Locale used as fallback when a code cannot be resolved for the requested locale. */
	private final Locale defaultLocale;

	/** Aggregated source providing the trans units for the catalog. */
	private final CatalogInterface catalog;

	/** Whether messages are formatted with ICU4J. */
	private final boolean useICU4j;

	/** Optional parent consulted when a code cannot be resolved locally. */
    private final @Nullable MessageSource parentMessageSource;

	/**
	 * Aggregates trans units into the catalog map and composes the sources for
	 * late-binding fallback.
	 */
	private CatalogMessageSourceBuilder(
			List<CatalogInterface> sources,
			Locale defaultLocale,
		boolean useICU4j,
			@Nullable MessageSource parentMessageSource
	) {
		this.defaultLocale = defaultLocale;
		this.useICU4j = useICU4j;
        this.parentMessageSource = parentMessageSource;
		this.catalogMap = new ConcurrentHashMap<>();
		this.catalog = new CompositeCatalog(sources);

		this.catalog.getTransUnits().forEach(t -> this.put(t.locale(), t.code(), t.value()));
	}

	/**
	 * Creates a new {@link Builder} from a list of trans units. The list is wrapped in a {@link TransUnitsCatalog} and
	 * used as the initial source.
	 *
	 * @param defaultLocale the locale used as a fallback when a code cannot be resolved for the requested locale; must
	 *                      not be {@code null}
	 * @param transUnits    the trans units to use as the initial source; must not be {@code null}
	 * @return a new {@link Builder} instance
	 */
	public static Builder builder(Locale defaultLocale, List<TransUnitInterface> transUnits) {
		Assert.notNull(transUnits, "Argument transUnits must not be null");

		return builder(defaultLocale, new TransUnitsCatalog(transUnits));
	}

	/**
	 * Creates a new {@link Builder} from a {@link CatalogInterface} source.
	 *
	 * @param defaultLocale the locale used as a fallback when a code cannot be resolved for
	 *                      the requested locale; must not be {@code null}
	 * @param catalogSource the initial source; must not be {@code null}
	 * @return a new {@link Builder} instance
	 */
	public static Builder builder(Locale defaultLocale, CatalogInterface catalogSource) {
		Assert.notNull(catalogSource, "Argument catalogSource must not be null");

		return new Builder(defaultLocale, catalogSource);
	}

	@Override
	public final @Nullable String getMessage(String code, Object @Nullable [] args, @Nullable String defaultMessage, @Nullable Locale locale){
		String msg = this.getMessageInternal(code, args, locale);
		if (msg != null) {
			return msg;
		}

		return defaultMessage;
	}

	@Override
	public final String getMessage(String code, Object @Nullable [] args, @Nullable Locale locale) throws NoSuchMessageException {
		String msg = this.getMessageInternal(code, args, locale);
		if (msg != null) {
			return msg;
		}

		throw new NoSuchMessageException(code, Objects.requireNonNullElse(locale, this.defaultLocale));
	}

	@Override
	public final String getMessage(MessageSourceResolvable resolvable, @Nullable Locale locale) throws NoSuchMessageException {
		String[] codes = resolvable.getCodes();
		if (codes != null) {
			for (String code : codes) {
				String message = this.getMessageInternal(code, resolvable.getArguments(), locale);
				if (message != null) {
					return message;
				}
			}
		}

		String defaultMessage = resolvable.getDefaultMessage();
		if (defaultMessage != null) {
			return defaultMessage;
		}

		String code = !ObjectUtils.isEmpty(codes) ? codes[codes.length - 1] : "";
		throw new NoSuchMessageException(code, Objects.requireNonNullElse(locale, this.defaultLocale));
	}

	/**
	 * Resolves the given code for the requested locale and formats it with the given arguments.
	 *
	 * <p>Lookup order: the in-memory catalog (locale fallback delegated to the JDK via
	 * {@link ResourceBundle}), then the late-binding sources in order,
	 * then the parent message source. Resolved values from the sources are cached for subsequent calls.
	 *
	 * @param code the message code to resolve
	 * @param args the arguments to format the message with, or {@code null} for none
	 * @param locale the locale to resolve for
	 * @return the resolved message, or {@code null} if the code cannot be resolved
	 */
	protected @Nullable String getMessageInternal(@Nullable String code, Object @Nullable [] args, @Nullable Locale locale) {
		if (code == null) {
			return null;
		}
		if (locale == null) {
			locale = this.defaultLocale;
		}

		String value = this.resolveFromCatalog(code, locale, args);

		if (ObjectUtils.isEmpty(args) || value == null) {
			return value;
		}

		if (this.useICU4j) {
			com.ibm.icu.text.MessageFormat messageFormat = new com.ibm.icu.text.MessageFormat(value, locale);

			if (args.length == 1 && args[0] instanceof java.util.Map<?, ?> map) {
				@SuppressWarnings("unchecked")
				java.util.Map<String, Object> namedArgs = (java.util.Map<String, Object>) map;
				return messageFormat.format(namedArgs);
			}

			return messageFormat.format(args);
		}

		return new MessageFormat(value, locale).format(args);
	}

	/**
	 * Resolves the code through the JDK-driven bundle (locale fallback applied), then falls back
	 * to the late-binding sources and finally the parent message source.
	 */
	private @Nullable String resolveFromCatalog(String code, Locale locale, Object @Nullable [] args) {
		if (locale.getLanguage().isEmpty() || code.isEmpty()) {
			return null;
		}

		String value = this.resolveFromBundle(code, locale);
		if (value != null) {
			return value;
		}

		TransUnitInterface tu = this.catalog.resolveTransUnit(code, locale);
		if (tu != null) {
			this.put(tu.locale(), tu.code(), tu.value());
			return tu.value();
		}

		if (this.parentMessageSource != null) {
			return this.parentMessageSource.getMessage(code, args, null, locale);
		}

		return null;
	}

	/**
	 * Stores a translation under its code.
	 */
	private void put(Locale locale, String code, String value) {
		if (locale.getLanguage().isEmpty()) {
			return;
		}

		ConcurrentMap<String, String> bucket = this.catalogMap.computeIfAbsent(
				locale, l -> new ConcurrentHashMap<>()
		);

		bucket.putIfAbsent(code, value);
	}

	/**
	 * Resolves the code against the in-memory catalog using the JDK locale fallback.
	 */
	private @Nullable String resolveFromBundle(String code, Locale locale) {
		ResourceBundle bundle = this.getResourceBundle(locale);

		if (bundle.containsKey(code)) {
			return bundle.getString(code);
		}
		return null;
	}

	/**
	 * Returns the bundle for the locale from the per-instance cachedBundles cache, building it
	 * once on a miss.
	 */
	private ResourceBundle getResourceBundle(Locale locale) {
		ResourceBundle cached = this.bundles.get(locale);
		if (cached != null) {
			return cached;
		}

		ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_BASE_NAME, locale, this.control);
		this.bundles.put(locale, bundle);
		return bundle;
	}

	/**
	 * A ResourceBundle.Control that serves the in-memory catalogMap instead of .properties/.class
	 * files, delegating the locale fallback to the JDK. The default candidate-locale chain is kept
	 * (region → language → root); root is mapped to the configured default locale, so the chain
	 * bottoms out there — preserving the builder's configurable defaultLocale fallback. The JDK's
	 * own bundle cache is disabled (TTL_DONT_CACHE) so instances never collide in the shared,
	 * class-loader-scoped cache and nothing leaks into it; caching is done per instance in
	 * cachedBundles. newBundle creates the locale's catalog bucket eagerly (never returns null) and
	 * the CatalogResourceBundle holds it by live reference, so late-binding entries added later via
	 * put stay visible to the cached bundle chain.
	 */
	private final class CatalogControl extends ResourceBundle.Control {

		@Override
		public List<String> getFormats(String baseName) {
			return List.of(BUNDLE_FORMAT);
		}

		@Override
		public Locale getFallbackLocale(String baseName, Locale locale) {
			return CatalogMessageSourceBuilder.this.defaultLocale;
		}

		@Override
		public long getTimeToLive(String baseName, Locale locale) {
			return TTL_DONT_CACHE;
		}

		@Override
		public ResourceBundle newBundle(
				String baseName,
				Locale locale,
				String format,
				ClassLoader loader,
				boolean reload
		) {
			Locale bucketLocale = locale.equals(Locale.ROOT)
					? CatalogMessageSourceBuilder.this.defaultLocale
					: locale;
			ConcurrentMap<String, String> bucket = CatalogMessageSourceBuilder.this.catalogMap.computeIfAbsent(
					bucketLocale, l -> new ConcurrentHashMap<>()
			);

			return new CatalogResourceBundle(bucket);
		}
	}

	/**
	 * A ResourceBundle over a single locale's catalog bucket. handleGetObject returns null on a
	 * miss (rather than throwing) so the JDK locale fallback is not short-circuited. The bucket is
	 * referenced live, so entries added later are picked up.
	 */
	private static final class CatalogResourceBundle extends ResourceBundle {

		private final Map<String, String> entries;

		CatalogResourceBundle(Map<String, String> entries) {
			this.entries = entries;
		}

		@Override
		protected @Nullable Object handleGetObject(String code) {
			return this.entries.get(code);
		}

		@Override
		public Enumeration<String> getKeys() {
			return Collections.enumeration(this.entries.keySet());
		}
	}

	/**
	 * Fluent builder for {@link CatalogMessageSourceBuilder}. Holds the configured sources
	 * and the default locale until {@link #build()} is called.
	 */
	public static final class Builder extends AbstractCatalogMessageSourceBuilder<Builder> {

		/**
		 * Creates a new builder seeded with an initial source.
		 */
		private Builder(Locale defaultLocale, CatalogInterface catalogSource) {
			super(defaultLocale);
			Assert.notNull(catalogSource, "Argument catalogSource must not be null");

			this.addSource(catalogSource);
		}

		/**
		 * Builds a {@link CatalogMessageSourceBuilder} from the configured sources and default
		 * locale. Trans units are aggregated and the sources are composed
		 * at this point; subsequent mutations of the builder have no effect on the
		 * returned instance.
		 *
		 * @return a new {@link CatalogMessageSourceBuilder} instance
		 */
		public CatalogMessageSourceBuilder build() {
			return new CatalogMessageSourceBuilder(
					this.getSources(),
					this.getDefaultLocale(),
					this.isICU4jEnabled(),
					this.getParentMessageSource()
			);
		}
	}
}
