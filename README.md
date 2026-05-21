# Package to create a custom Spring MessageSource

This package extends the [AbstractMessageSource](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/context/support/AbstractMessageSource.html) and provides the [MessageSource interface](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/context/MessageSource.html).

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=alaugks_spring-messagesource-catalog&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=alaugks_spring-messagesource-catalog)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.alaugks/spring-messagesource-catalog.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.alaugks/spring-messagesource-catalog/0.6.0)

## Dependency

### Maven

```xml
<dependency>
    <groupId>io.github.alaugks</groupId>
    <artifactId>spring-messagesource-catalog</artifactId>
    <version>0.6.0</version>
</dependency>
```

### Gradle

```
implementation group: 'io.github.alaugks', name: 'spring-messagesource-catalog', version: '0.6.0'
```

## Packages that use the catalog as a base package

* [spring-messagesource-xliff](https://github.com/alaugks/spring-messagesource-xliff): Xliff MessageSource for Spring
* [spring-messagesource-json](https://github.com/alaugks/spring-messagesource-json): JSON MessageSource for Spring
* [spring-messagesource-db-example](https://github.com/alaugks/spring-messagesource-db-example): Example custom Spring MessageSource from database

## CatalogMessageSource Configuration

### Options

`builder(CatalogInterface catalogSource, Locale defaultLocale)` (required)

* Argument `CatalogInterface catalogSource`: Initial source.
* Argument `Locale defaultLocale`: Locale used as a fallback when a code cannot be resolved for the requested locale.

`builder(List<TransUnitInterface> transUnits, Locale defaultLocale)` (required, alternative)

* Argument `List<TransUnitInterface> transUnits`: Trans units used as the initial source (wrapped in a `TransUnitsCatalog`).
* Argument `Locale defaultLocale`: Locale used as a fallback when a code cannot be resolved for the requested locale.

`addSource(CatalogInterface source)`
* Appends another source. Sources are aggregated additively at `build()`; their lazy `resolveTransUnit` lookups are also chained in the order they were added.

`addSource(List<TransUnitInterface> transUnits)`
* Convenience overload that wraps the trans units in a `TransUnitsCatalog`.

`defaultDomain(String defaultDomain)`
* If the default domain is not set, the default is **messages**.

### TransUnit Record

If the `String domain` argument is not set, the default is the **messages** domain.

```java
TransUnit(Locale locale, String code, String value);

TransUnit(Locale locale, String code, String value, String domain);
```


### Configuration example

#### MessageConfig with List of TransUnits 

```java
import io.github.alaugks.spring.messagesource.catalog.CatalogMessageSourceBuilder;
import io.github.alaugks.spring.messagesource.catalog.records.TransUnit;
import io.github.alaugks.spring.messagesource.catalog.records.TransUnitInterface;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageConfig {
    
    private final List<TransUnitInterface> transUnits = new ArrayList<>() {{
        // en
        add(new TransUnit(Locale.forLanguageTag("en"), "headline", "Headline"));
        add(new TransUnit(Locale.forLanguageTag("en"), "postcode", "Postcode"));
        add(new TransUnit(Locale.forLanguageTag("en"), "headline", "Payment", "payment"));
        add(new TransUnit(Locale.forLanguageTag("en"), "expiry_date", "Expiry date", "payment"));

        // en-US
        add(new TransUnit(Locale.forLanguageTag("en-US"), "postcode", "Zip code"));
        add(new TransUnit(Locale.forLanguageTag("en-US"), "expiry_date", "Expiration date", "payment"));

        // de
        add(new TransUnit(Locale.forLanguageTag("de"), "headline", "Überschrift"));
        add(new TransUnit(Locale.forLanguageTag("de"), "postcode", "Postleitzahl"));
        add(new TransUnit(Locale.forLanguageTag("de"), "headline", "Zahlung", "payment"));
        add(new TransUnit(Locale.forLanguageTag("de"), "expiry_date", "Ablaufdatum", "payment"));
    }};

    @Bean
    public MessageSource messageSource() {
        return CatalogMessageSourceBuilder
            .builder(this.transUnits, Locale.forLanguageTag("en"))
            .build();
	}
}
```

#### Target values

The behaviour of resolving the target value based on the code is equivalent to the ResourceBundleMessageSource or ReloadableResourceBundleMessageSource.

<table>
  <thead>
  <tr>
    <th>code</th>
    <th>en</th>
    <th>en-US</th>
    <th>de</th>
    <th>jp***</th>
  </tr>
  </thead>
  <tbody>
  <tr>
    <td>headline*<br>messages.headline</td>
    <td>Headline</td>
    <td>Headline**</td>
    <td>Überschrift</td>
    <td>Headline</td>
  </tr>
  <tr>
    <td>postcode*<br>messages.postcode</td>
    <td>Postcode</td>
    <td>Zip code</td>
    <td>Postleitzahl</td>
    <td>Postcode</td>
  </tr>
  <tr>
    <td>payment.headline</td>
    <td>Payment</td>
    <td>Payment**</td>
    <td>Zahlung</td>
    <td>Payment</td>
  </tr>
  <tr>
    <td>payment.expiry_date</td>
    <td>Expiry date</td>
    <td>Expiration date</td>
    <td>Ablaufdatum</td>
    <td>Expiry date</td>
  </tr>
  </tbody>
</table>

> *Default domain is `messages`.
>
> **Example of a fallback from Language_Region (`en-US`) to Language (`en`). The `id` does not exist in `en-US`, so it tries to select the translation with locale `en`.
>
> ***There is no translation for Japanese (`jp`). The default locale transUnits (`en`) are selected.

### With custom CatalogInterface

A custom source typically extends `AbstractCatalog`, which provides the chain plumbing (`nextCatalog`) and no-op defaults for the two data methods. A source then chooses one of two patterns:

- **Eager** — override `getTransUnits()`. The list is read once at construction time and merged into the catalog map.
- **Lazy** — override `resolveTransUnit(code, locale)` to return a `TransUnitInterface`. Called only when the catalog map has no entry for the requested key. The returned trans unit is cached in the map (using its `domain`), so subsequent lookups for the same key hit the in-memory map.

#### Chain of Responsibility for lazy lookups

When the catalog map cannot resolve a key, the lazy path walks the configured sources as a **Chain of Responsibility**. Each `CatalogInterface` plays the role of a handler:

1. The current source inspects the incoming `code` (and `locale`).
2. If it can answer, it returns a `TransUnit`, the chain stops, and the result is cached in the in-memory catalog map.
3. If it cannot answer, it forwards by calling `super.resolveTransUnit(code, locale)` — the `AbstractCatalog` default delegates to the next source set by `nextCatalog(...)`. Returning `null` directly (without calling `super`) ends the chain at this source.
4. If no source claims the request, the message ends up unresolved.

A common opt-out strategy is to gate on the requested domain — a source that owns `"glossary"` declines anything that doesn't start with `"glossary."`. The `LazyCatalog` example below shows that pattern.

The three examples below illustrate the patterns. They are then combined in [Combining multiple sources](#combining-multiple-sources).

#### Custom catalog with a list of TransUnits

The trans units are passed in via the constructor.

```java
import io.github.alaugks.spring.messagesource.catalog.catalog.AbstractCatalog;
import io.github.alaugks.spring.messagesource.catalog.records.TransUnitInterface;
import java.util.List;

public class MyStaticCatalog extends AbstractCatalog {

    private final List<TransUnitInterface> transUnits;

    public MyStaticCatalog(List<TransUnitInterface> transUnits) {
        this.transUnits = transUnits;
    }

    @Override
    public List<TransUnitInterface> getTransUnits() {
        return this.transUnits;
    }
}
```

#### Custom catalog from a database table

The trans units are loaded from a database table at construction time and exposed via `getTransUnits()`.

```java
import io.github.alaugks.spring.messagesource.catalog.catalog.AbstractCatalog;
import io.github.alaugks.spring.messagesource.catalog.records.TransUnit;
import io.github.alaugks.spring.messagesource.catalog.records.TransUnitInterface;
import java.util.ArrayList;
import java.util.List;

public class GlossaryDbCatalog extends AbstractCatalog {

    private static final String DOMAIN = "glossary";

    private final GlossaryRepository glossaryRepository;

    public GlossaryDbCatalog(GlossaryRepository glossaryRepository) {
        this.glossaryRepository = glossaryRepository;
    }

    @Override
    public List<TransUnitInterface> getTransUnits() {
        List<TransUnitInterface> transUnits = new ArrayList<>();
        this.glossaryRepository.findAll().forEach(row -> transUnits.add(
            new TransUnit(row.getLocale(), row.getCode(), row.getValue(), DOMAIN)
        ));
        return transUnits;
    }
}
```

#### Custom catalog with lazy resolution

The trans units are not loaded up front. `resolveTransUnit(...)` is called only on a map miss; the resolved value is then cached in the catalog so that subsequent lookups for the same key hit the in-memory map.

Useful when the underlying source is large enough that eager loading is impractical (e.g. a glossary table with hundreds of thousands of rows, or an external API).

The `code` argument is passed through as-is from the caller, so it may be given without a domain prefix (e.g. `"headline"`) or with one (e.g. `"lazyglossary.headline"`). A source that owns a specific domain checks the prefix and strips it before looking up its backend; the returned `TransUnit` then carries the code without the prefix and the source's own domain, so the cache entry lives at `"<domain>.<code>"`.

```java
import io.github.alaugks.spring.messagesource.catalog.catalog.AbstractCatalog;
import io.github.alaugks.spring.messagesource.catalog.records.TransUnit;
import io.github.alaugks.spring.messagesource.catalog.records.TransUnitInterface;
import java.util.Locale;

public class LazyCatalog extends AbstractCatalog {

    private static final String DOMAIN = "lazyglossary";
    private static final String PREFIX = DOMAIN + ".";

    private final LazyCatalogRepository lazyCatalogRepository;

    public LazyCatalog(LazyCatalogRepository lazyCatalogRepository) {
        this.lazyCatalogRepository = lazyCatalogRepository;
    }

    @Override
    public TransUnitInterface resolveTransUnit(String code, Locale locale) {
        // code may be "<code>" (default domain) or "<domain>.<code>".
        // This source owns only DOMAIN; anything it cannot answer is forwarded
        // to the next source via super.resolveTransUnit(...).
        if (code.startsWith(PREFIX)) {
            String localCode = code.substring(PREFIX.length());
            String value = this.lazyCatalogRepository.findByCodeAndLocale(localCode, locale);
            if (value != null) {
                return new TransUnit(locale, localCode, value, DOMAIN);
            }
        }
        return super.resolveTransUnit(code, locale);
    }
}
```

#### Combining multiple sources

Several sources can be combined directly on the `CatalogMessageSourceBuilder`. The example below chains the three custom catalogs above.

Sources are added in order with `addSource(...)`. Lazy lookups walk this order as a **Chain of Responsibility** — each source decides whether it can resolve the request, otherwise it delegates to the next link, and the first non-`null` result wins. Eager sources, by contrast, are aggregated up front into the catalog map, where the first source wins on key conflicts (`putIfAbsent` semantics).

```java
import io.github.alaugks.spring.messagesource.catalog.CatalogMessageSourceBuilder;
import io.github.alaugks.spring.messagesource.catalog.records.TransUnit;
import io.github.alaugks.spring.messagesource.catalog.records.TransUnitInterface;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageConfig {

    private final GlossaryRepository glossaryRepository;
    private final LazyCatalogRepository lazyCatalogRepository;

    public MessageConfig(
        GlossaryRepository glossaryRepository,
        LazyCatalogRepository lazyCatalogRepository
    ) {
        this.glossaryRepository = glossaryRepository;
        this.lazyCatalogRepository = lazyCatalogRepository;
    }

    @Bean
    public MessageSource messageSource() {
        List<TransUnitInterface> staticTransUnits = new ArrayList<>() {{
            add(new TransUnit(Locale.forLanguageTag("en"), "headline", "Headline"));
            add(new TransUnit(Locale.forLanguageTag("de"), "headline", "Überschrift"));
        }};

        return CatalogMessageSourceBuilder
            .builder(new MyStaticCatalog(staticTransUnits), Locale.forLanguageTag("en"))
            .addSource(new GlossaryDbCatalog(this.glossaryRepository))
            .addSource(new LazyCatalog(this.lazyCatalogRepository))
            .build();
    }
}
```

## Javadoc

Build the Javadoc locally:

```
./mvnw javadoc:javadoc
```

The generated HTML is written to `target/reports/apidocs/index.html`.

## License

Licensed under the [Apache License, Version 2.0](LICENSE).

