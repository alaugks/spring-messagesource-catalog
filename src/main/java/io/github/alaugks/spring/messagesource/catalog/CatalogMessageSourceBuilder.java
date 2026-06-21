// SPDX-License-Identifier: Apache-2.0
// Copyright 2024 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.catalog;

import io.github.alaugks.spring.messagesource.catalog.catalog.CatalogInterface;
import io.github.alaugks.spring.messagesource.catalog.records.TransUnitInterface;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * Spring {@link org.springframework.context.MessageSource} backed by one or more
 * {@link CatalogInterface} sources.
 *
 * <p>Translation units from all configured sources are aggregated into an in-memory map
 * during construction. Codes are resolved from that map first; on a miss, the configured
 * source chain is consulted via {@link CatalogInterface#resolveTransUnit(String, Locale)}
 * for late-binding sources, and the result is cached.
 *
 * <p>Use {@link #builder(List, Locale)} or {@link #builder(CatalogInterface, Locale)}
 * to obtain a {@link Builder} and configure additional sources via
 * {@link Builder#addSource(CatalogInterface)}.
 */
public class CatalogMessageSourceBuilder implements MessageSource {

	/** Default domain used when none is configured: {@value}. */
	public static final String DEFAULT_DOMAIN = "messages";

	/** Internal base name for the {@link ResourceBundle} that serves the catalog. */
	private static final String BUNDLE_BASE_NAME = CatalogMessageSourceBuilder.class.getPackageName();

	/**Internal bundle format name handled by {@link CatalogControl}. */
	private static final String BUNDLE_FORMAT = "catalog";

	private final ResourceBundle.Control control = new CatalogControl();

	/**
	 * Per-instance cache of resolved bundles, keyed by the requested locale. Mirrors the own cache
	 * of Spring's {@code ResourceBundleMessageSource}: the bundle (and its locale-fallback chain) is
	 * built once per locale and reused, rather than rebuilt on every lookup. The JDK's own
	 * {@link ResourceBundle} cache is left disabled ({@link CatalogControl#getTimeToLive}) so that
	 * instances never collide in the shared, class-loader-scoped cache and no instance state leaks
	 * into it. The cached bundles hold their catalog buckets by live reference, so late-binding
	 * entries added later via {@link #put} stay visible.
	 */
	private final ConcurrentMap<Locale, ResourceBundle> cachedBundles = new ConcurrentHashMap<>();

	private final ConcurrentMap<Locale, ConcurrentMap<String, String>> catalogMap;

	private final Locale defaultLocale;

	private final String defaultDomain;

	private final CatalogInterface chainHead;

	private final boolean useICU4j;

    private final MessageSource parentMessageSource;

	private final String domainDivider;

	/**
	 * Aggregates trans units into the catalog map and wires the source chain
	 * for late-binding fallback.
	 */
	private CatalogMessageSourceBuilder(
			List<CatalogInterface> sources,
			Locale defaultLocale,
			String defaultDomain,
        	boolean useICU4j,
			MessageSource parentMessageSource,
			String domainDivider
	) {
		this.defaultLocale = defaultLocale;
		this.defaultDomain = defaultDomain;
		this.useICU4j = useICU4j;
        this.parentMessageSource = parentMessageSource;
		this.domainDivider = domainDivider;
		this.catalogMap = new ConcurrentHashMap<>();

		for (CatalogInterface source : sources) {
			source.getTransUnits().forEach(t -> this.put(t.locale(), t.code(), t.value(), t.domain()));
		}

		Iterator<CatalogInterface> it = sources.iterator();
		this.chainHead = it.next();
		CatalogInterface current = this.chainHead;
		while (it.hasNext()) {
			CatalogInterface next = it.next();
			current.nextCatalog(next);
			current = next;
		}
	}

	/**
	 * Creates a new {@link Builder} from a list of trans units. The list is wrapped in a
	 * {@link TransUnitsCatalog} and used as the initial source.
	 *
	 * @param transUnits the trans units to use as the initial source; must not be {@code null}
	 * @param defaultLocale the locale used as a fallback when a code cannot be resolved for
	 *                      the requested locale; must not be {@code null}
	 * @return a new {@link Builder} instance
	 */
	public static Builder builder(List<TransUnitInterface> transUnits, Locale defaultLocale) {
		Assert.notNull(transUnits, "Argument transUnits must not be null");

		return builder(new TransUnitsCatalog(transUnits), defaultLocale);
	}

	/**
	 * Creates a new {@link Builder} from a {@link CatalogInterface} source.
	 *
	 * @param catalogSource the initial source; must not be {@code null}
	 * @param defaultLocale the locale used as a fallback when a code cannot be resolved for
	 *                      the requested locale; must not be {@code null}
	 * @return a new {@link Builder} instance
	 */
	public static Builder builder(CatalogInterface catalogSource, Locale defaultLocale) {
		Assert.notNull(catalogSource, "Argument catalogSource must not be null");

		return new Builder(catalogSource, defaultLocale);
	}

	@Nullable
	@Override
	public final String getMessage(String code, @Nullable Object[] args, @Nullable String defaultMessage, @Nullable Locale locale){
		String msg = this.getMessageInternal(code, args, locale);
		if (msg != null) {
			return msg;
		}

		return defaultMessage;
	}

	@Override
	public final String getMessage(String code, @Nullable Object[] args, @Nullable Locale locale) throws NoSuchMessageException {
		String msg = this.getMessageInternal(code, args, locale);
		if (msg != null) {
			return msg;
		}

		if (locale == null) {
			throw new NoSuchMessageException(code);
		}
		else {
			throw new NoSuchMessageException(code, locale);
		}
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
		if (locale == null ) {
			throw new NoSuchMessageException(code);
		}
		else {
			throw new NoSuchMessageException(code, locale);
		}
	}

	/**
	 * Resolves the given code for the requested locale and formats it with the given arguments.
	 *
	 * <p>Lookup order: the in-memory catalog (locale fallback delegated to the JDK via
	 * {@link ResourceBundle}, default-domain prefix probed), then the late-binding source chain,
	 * then the parent message source. Resolved values from the chain are cached for subsequent calls.
	 *
	 * @param code the message code to resolve
	 * @param args the arguments to format the message with, or {@code null} for none
	 * @param locale the locale to resolve for
	 * @return the resolved message, or {@code null} if the code cannot be resolved
	 */
	@Nullable
	protected String getMessageInternal(@Nullable String code, @Nullable Object[] args, @Nullable Locale locale) {
		if (code == null) {
			return null;
		}
		if (locale == null) {
			locale = Locale.getDefault();
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
	 * to the late-binding source chain and finally the parent message source.
	 */
	private String resolveFromCatalog(String code, Locale locale, @Nullable Object[] args) {
		if (locale.getLanguage().isEmpty() || code.isEmpty()) {
			return null;
		}

		String value = this.resolveFromBundle(code, locale);
		if (value != null) {
			return value;
		}

		TransUnitInterface tu = this.chainHead.resolveTransUnit(code, locale);
		if (tu != null) {
			this.put(tu.locale(), tu.code(), tu.value(), tu.domain());
			return tu.value();
		}

		if (this.parentMessageSource != null) {
			return this.parentMessageSource.getMessage(code, args, null, locale);
		}

		return null;
	}

	/**
	 * Stores a translation under its key with the domain prefix, plus an alias
	 * without the prefix when the domain matches the default.
	 */
	private void put(Locale locale, String code, String value, String domain) {
		if (locale.getLanguage().isEmpty()) {
			return;
		}

		ConcurrentMap<String, String> bucket = this.catalogMap.computeIfAbsent(
				locale, l -> new ConcurrentHashMap<>()
		);

		if (Objects.equals(domain, this.defaultDomain)) {
			bucket.putIfAbsent(code, value);
		}
		bucket.putIfAbsent(this.concatCode(domain, code), value);
	}

	/**
	 * Resolves the code against the in-memory catalog using the JDK locale fallback: a
	 * {@link ResourceBundle} is loaded through {@link CatalogControl}, so the candidate-locale
	 * chain (region → language → root, where root serves the configured default locale) and the
	 * per-locale bundle chaining are handled by
	 * {@link ResourceBundle#getBundle(String, Locale, ResourceBundle.Control)}. The code is probed
	 * both without and with the default-domain prefix.
	 */
	private String resolveFromBundle(String code, Locale locale) {
		ResourceBundle bundle = this.getResourceBundle(locale);
		if (bundle == null) {
			return null;
		}

		String domainCode = this.concatCode(this.defaultDomain, code);
		if (bundle.containsKey(code)) {
			return bundle.getString(code);
		}
		if (bundle.containsKey(domainCode)) {
			return bundle.getString(domainCode);
		}

		return null;
	}

	/**
	 * Returns the bundle for the locale from the per-instance {@link #cachedBundles} cache, building
	 * it once via {@link ResourceBundle#getBundle(String, Locale, ResourceBundle.Control)} on a miss.
	 */
	private ResourceBundle getResourceBundle(Locale locale) {
		ResourceBundle cached = this.cachedBundles.get(locale);
		if (cached != null) {
			return cached;
		}

		try {
			ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_BASE_NAME, locale, this.control);
			this.cachedBundles.put(locale, bundle);
			return bundle;
		} catch (MissingResourceException e) {
			return null;
		}
	}

	/**
	 * Joins {@code domain} and {@code code} with a dot, defaulting to
	 * {@link #DEFAULT_DOMAIN} when {@code domain} is {@code null}.
	 */
	private String concatCode(String domain, String code) {
		return Optional.ofNullable(domain).orElse(DEFAULT_DOMAIN) + this.domainDivider + code;
	}

	/**
	 * A {@link java.util.ResourceBundle.Control} that serves the in-memory {@link #catalogMap}
	 * instead of {@code .properties}/{@code .class} files, delegating the locale fallback to the
	 * JDK. The default {@link #getCandidateLocales(String, Locale)} is kept (it yields
	 * region → language → {@link Locale#ROOT}); {@link Locale#ROOT} is mapped to the configured
	 * default locale, so the chain bottoms out there — preserving the builder's configurable
	 * {@code defaultLocale} fallback. The JDK's own bundle cache is disabled
	 * ({@link #getTimeToLive} returns {@link #TTL_DONT_CACHE}) so instances never collide in the
	 * shared, class-loader-scoped {@code ResourceBundle} cache and nothing leaks into it; caching is
	 * done per instance in {@link #cachedBundles}. {@link #newBundle} creates the locale's catalog
	 * bucket eagerly (never returns {@code null}) and the {@link CatalogResourceBundle} holds it by
	 * live reference, so late-binding entries added later via {@link #put} stay visible to the cached
	 * bundle chain.
	 */
	private final class CatalogControl extends ResourceBundle.Control {

		@Override
		public List<String> getFormats(String baseName) {
			return List.of(BUNDLE_FORMAT);
		}

		@Override
		public Locale getFallbackLocale(String baseName, Locale locale) {
			return null;
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
	 * A {@link ResourceBundle} over a single locale's catalog bucket. {@code handleGetObject}
	 * returns {@code null} on a miss (rather than throwing) so the JDK locale fallback is not
	 * short-circuited. The bucket is referenced live, so entries added later are picked up.
	 */
	private static final class CatalogResourceBundle extends ResourceBundle {

		private final Map<String, String> entries;

		CatalogResourceBundle(Map<String, String> entries) {
			this.entries = entries;
		}

		@Override
		protected Object handleGetObject(String key) {
			return this.entries.get(key);
		}

		@Override
		public Enumeration<String> getKeys() {
			return Collections.enumeration(this.entries.keySet());
		}
	}

	/**
	 * Fluent builder for {@link CatalogMessageSourceBuilder}. Holds the configured sources,
	 * the default locale, and the default domain until {@link #build()} is called.
	 */
	public static final class Builder extends AbstractCatalogMessageSourceBuilder<Builder> {

		private final List<CatalogInterface> sources = new ArrayList<>();

		/**
		 * Creates a new builder seeded with an initial source.
		 *
		 * @param catalogSource the initial source; must not be {@code null}
		 * @param defaultLocale the locale used as a fallback when a code cannot be resolved
		 *                      for the requested locale; must not be {@code null}
		 */
		public Builder(CatalogInterface catalogSource, Locale defaultLocale) {
			super(defaultLocale);
			Assert.notNull(catalogSource, "Argument catalogSource must not be null");

			this.sources.add(catalogSource);
		}

		/**
		 * Appends another source. Sources are aggregated additively at {@link #build()};
		 * their {@code resolveTransUnit} late-binding methods are also chained in the order
		 * they were added.
		 *
		 * @param source the source to append; must not be {@code null}
		 * @return this builder
		 */
		public Builder addSource(CatalogInterface source) {
			Assert.notNull(source, "Argument source must not be null");

			this.sources.add(source);

			return this;
		}

		/**
		 * Convenience overload of {@link #addSource(CatalogInterface)} that wraps the given
		 * trans units in a {@link TransUnitsCatalog}.
		 *
		 * @param transUnits the trans units to append as a new source; must not be {@code null}
		 * @return this builder
		 */
		public Builder addSource(List<TransUnitInterface> transUnits) {
			Assert.notNull(transUnits, "Argument transUnits must not be null");

			return this.addSource(new TransUnitsCatalog(transUnits));
		}

		/**
		 * Builds a {@link CatalogMessageSourceBuilder} from the configured sources, default
		 * locale, and default domain. Trans units are aggregated and the source chain is
		 * wired up at this point; subsequent mutations of the builder have no effect on the
		 * returned instance.
		 *
		 * @return a new {@link CatalogMessageSourceBuilder} instance
		 */
		public CatalogMessageSourceBuilder build() {
			return new CatalogMessageSourceBuilder(
					this.sources,
					this.getDefaultLocale(),
					this.getDefaultDomain(),
					this.isICU4jEnabled(),
					this.getParentMessageSource(),
					this.getDomainDivider()
				);
		}
	}
}
