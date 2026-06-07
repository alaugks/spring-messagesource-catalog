// SPDX-License-Identifier: Apache-2.0
// Copyright 2024 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.catalog;

import io.github.alaugks.spring.messagesource.catalog.catalog.CatalogInterface;
import io.github.alaugks.spring.messagesource.catalog.records.TransUnitInterface;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.lang.NonNull;
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

	private final ConcurrentMap<Locale, ConcurrentMap<String, String>> catalogMap;

	private final Locale defaultLocale;

	private final String defaultDomain;

	private final CatalogInterface chainHead;

	private final boolean useCodeAsDefaultMessage;

	/**
	 * Aggregates trans units into the catalog map and wires the source chain
	 * for late-binding fallback.
	 */
	private CatalogMessageSourceBuilder(
			List<CatalogInterface> sources,
			Locale defaultLocale,
			String defaultDomain,
			boolean useCodeAsDefaultMessage
	) {
		this.defaultLocale = defaultLocale;
		this.defaultDomain = defaultDomain;
		this.useCodeAsDefaultMessage = useCodeAsDefaultMessage;
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

	@Override
	@Nullable
	public final String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
		String msg = getMessageInternal(code, args, locale);
		if (msg != null) {
			return msg;
		}

		if (defaultMessage == null) {
			return getDefaultMessage(code);
		}

		return null;
	}

	@Override
	public final String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {
		String msg = getMessageInternal(code, args, locale);
		if (msg != null) {
			return msg;
		}
		String fallback = getDefaultMessage(code);
		if (fallback != null) {
			return fallback;
		}
        throw new NoSuchMessageException(code, locale);
    }

	@Override
	public final String getMessage(MessageSourceResolvable resolvable, @Nullable Locale locale) throws NoSuchMessageException {
		String[] codes = resolvable.getCodes();
		if (codes != null) {
			for (String code : codes) {
				String message = getMessageInternal(code, resolvable.getArguments(), locale);
				if (message != null) {
					return message;
				}
			}
		}
//		String defaultMessage = getDefaultMessage(resolvable, locale);
//		if (defaultMessage != null) {
//			return defaultMessage;
//		}
		String code = !ObjectUtils.isEmpty(codes) ? codes[codes.length - 1] : "";
		if (locale == null ) {
			throw new NoSuchMessageException(code);
		}
		else {
			throw new NoSuchMessageException(code, locale);
		}
	}

	/**
	 * Whether a message code is returned as the default message when it cannot be resolved.
	 *
	 * @return {@code true} if the code is used as the default message
	 */
	public boolean isUseCodeAsDefaultMessage() {
		return this.useCodeAsDefaultMessage;
	}

	/**
	 * Returns the default message to use when a code cannot be resolved: the code itself when
	 * {@link #isUseCodeAsDefaultMessage()} is enabled, otherwise {@code null}.
	 */
	@Nullable
	protected String getDefaultMessage(String code) {
		if (this.isUseCodeAsDefaultMessage()) {
			return code;
		}
		return null;
	}


	/**
	 * Resolves the given code to a {@link MessageFormat} for the requested locale.
	 *
	 * <p>Lookup order: the in-memory catalog map (with domain, no-region, and default-locale
	 * fallbacks), then the late-binding source chain. Resolved values from the chain are
	 * cached for subsequent calls.
	 *
	 * @param code the message code to resolve
	 * @param locale the locale to resolve for
	 * @return the {@link MessageFormat}, or {@code null} if the code cannot be resolved
	 */
	@Nullable
	protected String getMessageInternal(@NonNull String code, Object[] args, @NonNull Locale locale) {
		String value = this.resolveFromCatalog(code, locale);

		if(args == null) {
			return value;
		}

		return new com.ibm.icu.text.MessageFormat(value, locale).format(args);
			//return new MessageFormat(value, locale).format(args);



//		if (args[0] instanceof java.util.Map) {
//			return new com.ibm.icu.text.MessageFormat(value, locale).format(args);
//		}
//
//		return null;


//		if (args == null || args.length == 0) {
			//return value;
//		}

//		//if (args.length == 1 && !args[0] instanceof java.util.Map) {
//			MessageFormat fmt = new java.text.MessageFormat(code, locale);
//			return fmt.format(args);
//		//}

//		if (args.length == 1 && args[0] instanceof java.util.Map) {
//			MessageFormat fmt = new MessageFormat(code, locale);
//			return fmt.format(args[0]);
//		}

		//return null;
	}

	/**
	 * Looks up the code in the catalog map, falling back to the source chain
	 * and caching the result.
	 */
	public String resolveFromCatalog(String code, Locale locale) {
		if (locale.getLanguage().isEmpty() || code.isEmpty()) {
			return null;
		}

		Optional<String> value = this.resolveFromCatalogMap(code, locale);
		if (value.isPresent()) {
			return value.get();
		}

		TransUnitInterface tu = this.chainHead.resolveTransUnit(code, locale);
		if (tu != null) {
			this.put(tu.locale(), tu.code(), tu.value(), tu.domain());
			return tu.value();
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
	 * Walks the locale and code-key fallbacks (region → language → default
	 * locale, code without domain prefix → code with domain prefix).
	 */
	private Optional<String> resolveFromCatalogMap(String code, Locale locale) {
		String domainCode = this.concatCode(this.defaultDomain, code);

		String value = this.lookup(locale, code, domainCode);
		if (value != null) {
			return Optional.of(value);
		}

		Locale languageOnly = this.buildLocaleWithoutRegion(locale);
		if (!languageOnly.equals(locale)) {
			value = this.lookup(languageOnly, code, domainCode);
			if (value != null) {
				return Optional.of(value);
			}
		}

		if (!this.defaultLocale.equals(locale) && !this.defaultLocale.equals(languageOnly)) {
			value = this.lookup(this.defaultLocale, code, domainCode);
			if (value != null) {
				return Optional.of(value);
			}
		}

		return Optional.empty();
	}

	/**
	 * Reads the bucket for {@code locale} once and probes the code both
	 * without and with the domain prefix.
	 */
	private String lookup(Locale locale, String code, String domainCode) {
		ConcurrentMap<String, String> bucket = this.catalogMap.get(locale);
		if (bucket == null) {
			return null;
		}
		String value = bucket.get(code);
		return value != null ? value : bucket.get(domainCode);
	}

	/** Returns a language-only {@link Locale} (region and variant stripped). */
	private Locale buildLocaleWithoutRegion(Locale locale) {
		return new Locale.Builder().setLanguage(locale.getLanguage()).build();
	}

	/**
	 * Joins {@code domain} and {@code code} with a dot, defaulting to
	 * {@link #DEFAULT_DOMAIN} when {@code domain} is {@code null}.
	 */
	private String concatCode(String domain, String code) {
		return Optional.ofNullable(domain).orElse(DEFAULT_DOMAIN) + "." + code;
	}

	/**
	 * Fluent builder for {@link CatalogMessageSourceBuilder}. Holds the configured sources,
	 * the default locale, and the default domain until {@link #build()} is called.
	 */
	public static final class Builder {

		private final Locale defaultLocale;

		private final List<CatalogInterface> sources = new ArrayList<>();

		private String defaultDomain = DEFAULT_DOMAIN;

		private boolean useCodeAsDefaultMessage = false;

		/**
		 * Creates a new builder seeded with an initial source.
		 *
		 * @param catalogSource the initial source; must not be {@code null}
		 * @param defaultLocale the locale used as a fallback when a code cannot be resolved
		 *                      for the requested locale; must not be {@code null}
		 */
		public Builder(CatalogInterface catalogSource, Locale defaultLocale) {
			Assert.notNull(catalogSource, "Argument catalogSource must not be null");
			Assert.notNull(defaultLocale, "Argument defaultLocale must not be null");

			this.defaultLocale = defaultLocale;
			this.sources.add(catalogSource);
		}

		/**
		 * Sets the default domain. Codes stored under this domain are also accessible via
		 * their name without the domain prefix; codes stored under any other domain require the
		 * {@code <domain>.<code>} prefix.
		 *
		 * @param defaultDomain the default domain; must not be {@code null}
		 * @return this builder
		 */
		public Builder defaultDomain(String defaultDomain) {
			Assert.notNull(defaultDomain, "Argument defaultDomain must not be null");

			this.defaultDomain = defaultDomain;

			return this;
		}

		/**
		 * Sets whether a message code is returned as the default message when it cannot be
		 * resolved. Defaults to {@code false}.
		 *
		 * @param useCodeAsDefaultMessage {@code true} to use the code as the default message
		 * @return this builder
		 */
		public Builder setUseCodeAsDefaultMessage(boolean useCodeAsDefaultMessage) {
			this.useCodeAsDefaultMessage = useCodeAsDefaultMessage;

			return this;
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
					this.sources, this.defaultLocale, this.defaultDomain, this.useCodeAsDefaultMessage
			);
		}
	}
}
