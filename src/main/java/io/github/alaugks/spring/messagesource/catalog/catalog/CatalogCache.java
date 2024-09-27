package io.github.alaugks.spring.messagesource.catalog.catalog;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class CatalogCache extends AbstractCatalog {

	private final Map<Locale, Map<String, String>> cacheMap = new ConcurrentHashMap<>();

	@Override
	public String resolveCode(String code, Locale locale) {
		if (locale.toString().isEmpty() || code.isEmpty()) {
			return null;
		}

		// Resolve in Cache
		Optional<String> value = this.getTargetValue(locale, code);
		if (value.isPresent()) {
			return value.get();
		}

		// Resolve in Catalog
		String resolvedValue = super.resolveCode(code, locale);

		// Put in cache
		this.put(locale, code, resolvedValue);

		return resolvedValue;
	}

	@Override
	public void build() {
		super.build();
		super.getTransUnits().forEach(t -> {
			if (Objects.equals(t.domain(), Catalog.DEFAULT_DOMAIN)) {
				this.put(t.locale(), t.code(), t.value());
			}
			this.put(t.locale(), concatCode(t.domain(), t.code()), t.value());
		});
	}

	private Optional<String> getTargetValue(Locale locale, String code) {
		Map<String, String> map = this.cacheMap.get(locale);

		if (map != null) {
			return Optional.ofNullable(map.get(code));
		}

		return Optional.empty();
	}

	private void put(Locale locale, String code, String targetValue) {
		if (targetValue != null) {
			this.cacheMap.computeIfAbsent(
					locale, l -> new ConcurrentHashMap<>()
			).put(code, targetValue);
		}
	}
}
