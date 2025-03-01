package io.github.alaugks.spring.messagesource.catalog.catalog;

import java.util.Locale;
import java.util.Locale.Builder;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.util.Assert;

public final class Catalog extends AbstractCatalog {

	public static final String DEFAULT_DOMAIN = "messages";

	private final Map<Locale, Map<String, String>> catalogMap;

	private final Locale defaultLocale;

	private final String defaultDomain;

	public Catalog(Locale defaultLocale) {
		this(defaultLocale, DEFAULT_DOMAIN);
	}

	public Catalog(Locale defaultLocale, String defaultDomain) {
		Assert.notNull(defaultLocale, "Argument defaultLocale must not be null");
		Assert.notNull(defaultDomain, "Argument defaultDomain must not be null");

		this.catalogMap = new ConcurrentHashMap<>();
		this.defaultLocale = defaultLocale;
		this.defaultDomain = defaultDomain;
	}

	@Override
	public String resolveCode(String code, Locale locale) {
		if (locale.toString().isEmpty() || code.isEmpty()) {
			return null;
		}

		return this.resolveFromCatalogMap(code, locale).orElse(super.resolveCode(code, locale));
	}

	@Override
	public void build() {
		super.build();
		super.getTransUnits().forEach(t -> this.put(t.locale(), t.code(), t.value(), t.domain()));
	}

	private void put(Locale locale, String code, String value, String domain) {
		if (!locale.toString().isEmpty()) {
			this.catalogMap.computeIfAbsent(
					locale, l -> new ConcurrentHashMap<>()
			).putIfAbsent(concatCode(domain, code), value);
		}
	}

	private Optional<String> resolveFromCatalogMap(String code, Locale locale) {
		return this.getTargetValue(concatCode(this.defaultDomain, code), locale)
				.or(() -> this.getTargetValue(code, locale))
				.or(() -> this.getTargetValue(concatCode(this.defaultDomain, code), this.buildLocaleWithoutRegion(locale)))
				.or(() -> this.getTargetValue(code, this.buildLocaleWithoutRegion(locale)))
				.or(() -> this.getTargetValue(concatCode(this.defaultDomain, code), this.defaultLocale))
				.or(() -> this.getTargetValue(code, this.defaultLocale));
	}

	private Optional<String> getTargetValue(String code, Locale locale) {
		return Optional.ofNullable(this.catalogMap.get(locale)).flatMap(
				localeCatalog -> Optional.ofNullable(localeCatalog.get(code))
		);
	}

	private Locale buildLocaleWithoutRegion(Locale locale) {
		return new Builder().setLanguage(locale.getLanguage()).build();
	}
}
