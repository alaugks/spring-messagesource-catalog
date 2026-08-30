# Package to create a custom Spring MessageSource

This package provides the [MessageSource interface](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/context/MessageSource.html).

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=alaugks_spring-messagesource-catalog&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=alaugks_spring-messagesource-catalog)

> [!IMPORTANT]
> Until version 1.0.0, breaking changes (renaming of classes or methods, changes to method signatures) can occur in any release. The functionality itself
> will always be preserved.

## Table of Contents

- [Packages that use the catalog as a base package](#packages-that-use-the-catalog-as-a-base-package)
- [CatalogMessageSource Configuration](#catalogmessagesource-configuration)
  - [Options](#options)
  - [TransUnit Record](#transunit-record)
  - [Configuration example](#configuration-example)
  - [With custom CatalogInterface](#with-custom-cataloginterface)
- [Message formatting](#message-formatting)
  - [Default (java.text.MessageFormat)](#default-javatextmessageformat)
  - [ICU4J (com.ibm.icu.text.MessageFormat)](#icu4j-comibmicutextmessageformat)
    - [Plural](#plural)
    - [Select (and gender)](#select-and-gender)
- [Resource classes](#resource-classes)
  - [ResourceLoaderBuilder](#resourceloader)
  - [File name convention](#file-name-convention)
  - [Records: Filename and TranslationFile](#records-filename-and-translationfile)
- [Interfaces](#interfaces)
- [License](#license)

## Packages that use the catalog as a base package

* [spring-messagesource-xliff](https://github.com/alaugks/spring-messagesource-xliff): Xliff MessageSource for Spring
* [spring-messagesource-json](https://github.com/alaugks/spring-messagesource-json): JSON MessageSource for Spring
* [spring-messagesource-db-example](https://github.com/alaugks/spring-messagesource-db-example): Example custom Spring MessageSource from database

## CatalogMessageSource Configuration

### Options

| Method                                                               | Default    | Description                                                                                                                                                                                                                                                  |
|----------------------------------------------------------------------|------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `builder(Locale defaultLocale, CatalogInterface catalogSource)`      | —          | Entry point.<br><br>`defaultLocale` is the locale to fall back to when a code cannot be resolved for the requested locale.<br><br>`catalogSource` is the initial source.                                                                                     |
| `builder(Locale defaultLocale, List<TransUnitInterface> transUnits)` | —          | Entry point (alternative).<br><br>`defaultLocale` is the locale to fall back to when a code cannot be resolved for the requested locale.<br><br>`transUnits` are used as the initial source, wrapped in a `TransUnitsCatalog`.                               |
| `addSource(CatalogInterface source)`                                 | —          | Appends another source. Sources are aggregated additively at `build()`; their lazy `resolveTransUnit` lookups are consulted in the order they were added.                                                                                                    |
| `addSource(List<TransUnitInterface> transUnits)`                     | —          | Convenience overload of `addSource` that wraps the trans units in a `TransUnitsCatalog`.                                                                                                                                                                     |
| `defaultDomain(String defaultDomain)`                                | `messages` | The default domain. Codes stored under this domain are also accessible without the domain prefix; codes under any other domain require the `<domain>.<code>` prefix.                                                                                         |
| `domainDivider(String domainDivider)`                                | `.`        | The divider between domain and code in message codes (`<domain><divider><code>`).                                                                                                                                                                            |
| `enableICU4j()`                                                      | disabled   | Format messages with ICU4J's `com.ibm.icu.text.MessageFormat` instead of the default `java.text.MessageFormat`. Adds named arguments and ICU `plural`/`select` patterns. See [Message formatting](#message-formatting) for details and examples.             |
| `parentMessageSource(MessageSource parentMessageSource)`             | —          | Sets a parent [`MessageSource`](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/context/MessageSource.html) to delegate to. When a code cannot be resolved in the catalog, the lookup falls back to the parent source.  |
| `build()`                                                            | —          | Builds the `CatalogMessageSourceBuilder` from the configured sources, default locale and default domain. Trans units are aggregated and the sources are composed at this point; subsequent mutations of the builder have no effect on the returned instance. |

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
            .builder(Locale.forLanguageTag("en"), this.transUnits)
            .build();
    }
}
```

#### Target values

Resolving the target value based on the code behaves like the `ResourceBundleMessageSource` or `ReloadableResourceBundleMessageSource`.

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

A custom source typically extends `AbstractCatalog`. The base class provides no-op defaults for the two data methods. A source then chooses one of two patterns:

- **Eager** — override `getTransUnits()`. The list is read once at construction time and merged into the catalog map.
- **Lazy** — override `resolveTransUnit(code, locale)` to return a `TransUnitInterface`. Called only when the catalog map has no entry for the requested key. The returned trans unit is cached in the map (using its `domain`). Subsequent lookups for the same key hit the in-memory map.

#### Lazy lookups across multiple sources

When the catalog map cannot resolve a key, the lazy path consults the configured sources in the order they were added. For each source:

1. The source inspects the incoming `code` (and `locale`).
2. If it can answer, it returns a `TransUnit`, the lookup stops, and the result is cached in the in-memory catalog map.
3. If it cannot answer, it returns `null` and the next source is tried.
4. If no source claims the request, the message ends up unresolved.

A common opt-out strategy is to gate on the requested domain. A source that owns `"glossary"` returns `null` for anything that does not start with `"glossary."`. The `LazyCatalog` example below shows that pattern.

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

The trans units are not loaded up front. `resolveTransUnit(...)` is called only on a map miss. The resolved value is then cached in the catalog. Subsequent lookups for the same key hit the in-memory map.

Useful when the underlying source is large enough that eager loading is impractical (e.g. a glossary table with hundreds of thousands of rows, or an external API).

The `code` argument is passed through as-is from the caller. It may arrive without a domain prefix (e.g. `"headline"`) or with one (e.g. `"lazyglossary.headline"`). A source that owns a specific domain checks the prefix and strips it before looking up its backend. The returned `TransUnit` carries the code without the prefix and the source's own domain. The cache entry therefore lives at `"<domain>.<code>"`.

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
        // This source owns only DOMAIN; for anything it cannot answer it
        // returns null, and the builder consults the next source.
        if (code.startsWith(PREFIX)) {
            String localCode = code.substring(PREFIX.length());
            String value = this.lazyCatalogRepository.findByCodeAndLocale(localCode, locale);
            if (value != null) {
                return new TransUnit(locale, localCode, value, DOMAIN);
            }
        }
        return null;
    }
}
```

#### Combining multiple sources

Several sources can be combined directly on the `CatalogMessageSourceBuilder`. The example below combines the three custom catalogs above.

Sources are added in order with `addSource(...)`. On a map miss, the builder consults the sources in this order. The first non-`null` `resolveTransUnit` result wins; a source that cannot answer returns `null`. Eager sources, by contrast, are aggregated up front into the catalog map. On key conflicts the first source wins (`putIfAbsent` semantics).

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
            .builder(Locale.forLanguageTag("en"), new MyStaticCatalog(staticTransUnits))
            .addSource(new GlossaryDbCatalog(this.glossaryRepository))
            .addSource(new LazyCatalog(this.lazyCatalogRepository))
            .build();
    }
}
```

## Message formatting

A resolved value is formatted before it is returned. The arguments passed to `getMessage(...)` are applied
to the message pattern. Two formatters are available: the default `java.text.MessageFormat`, and
`com.ibm.icu.text.MessageFormat` once `enableICU4j()` is set.

> [!IMPORTANT]
> Named arguments and ICU `plural`/`select` patterns (e.g. `{count, plural, …}`) cannot be resolved by the default `java.text.MessageFormat`. They fail at `getMessage()` time. To use them you **must** enable ICU4J via `enableICU4j()`.
>
> ICU4J is the [`com.ibm.icu:icu4j`](https://central.sonatype.com/artifact/com.ibm.icu/icu4j) dependency. It is shipped transitively with this package; no extra dependency is required. Its `com.ibm.icu.text.MessageFormat` is a syntax superset of `java.text.MessageFormat`. Existing numeric-index patterns keep working.
>
> The two are not fully output-compatible. ICU4J uses Unicode CLDR locale data, so the formatted result for a given locale can differ from the JDK's. One example is the decimal and grouping separators in numbers (`.` vs `,`). Verify locale-sensitive output after enabling ICU4J.

### Default (java.text.MessageFormat)

Without `enableICU4j()`, values are formatted with [`java.text.MessageFormat`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/text/MessageFormat.html).
This is the same formatter Spring's `ResourceBundleMessageSource` uses. It only understands **numeric
argument indices** (`{0}`, `{1}`, …), passed positionally as an `Object[]`. Numbers are formatted
locale-aware; grouping separators differ per locale.

```java
new TransUnit(Locale.forLanguageTag("en"), "files", "There are {0,number,integer} files.");
new TransUnit(Locale.forLanguageTag("de"), "files", "Es gibt {0,number,integer} Dateien.");

messageSource.getMessage(
    "files",
    new Object[] { 10000 },
    Locale.forLanguageTag("de")
);
```

**Result:** `Es gibt 10.000 Dateien.`

#### Plural (`choice` format)

`java.text.MessageFormat` has no `plural` keyword. Its `choice` sub-format covers the common plural case
by mapping **numeric ranges** to variants. A limit followed by `#` matches values from that number up;
`<` matches values strictly greater. Each `|`-separated case is itself a pattern. To insert the number,
reference it again as `{0,number,integer}`.

```java
new TransUnit(
    Locale.forLanguageTag("en"),
    "file_deleted",
    "{0,choice,0#You deleted no files.|1#You deleted one file.|1<You deleted {0,number,integer} files.}"
);
new TransUnit(
    Locale.forLanguageTag("de"),
    "file_deleted",
    "{0,choice,0#Sie haben keine Dateien gelöscht.|1#Sie haben eine Datei gelöscht.|1<Sie haben {0,number,integer} Dateien gelöscht.}"
);

messageSource.getMessage(
    "file_deleted",
    new Object[] { 1000 },
    Locale.forLanguageTag("de")
);
```

**Result:** `Sie haben 1.000 Dateien gelöscht.`

#### Select

`java.text.MessageFormat` has **no** `select` construct. It can only branch on numbers via `choice`, not
on arbitrary string values. Value-based choices such as grammatical gender cannot be expressed. For
string-based `select` (and CLDR plural categories like `few`/`many`), enable
[ICU4J](#icu4j-comibmicutextmessageformat).

### ICU4J (com.ibm.icu.text.MessageFormat)

Enable ICU4J on the builder to format with [ICU4J's `MessageFormat`](https://unicode-org.github.io/icu-docs/apidoc/released/icu4j/com/ibm/icu/text/MessageFormat.html):

```java
@Bean
public MessageSource messageSource() {
    return CatalogMessageSourceBuilder
        .builder(Locale.forLanguageTag("en"), this.transUnits)
        .enableICU4j() // required for named arguments and plural/select
        .build();
}
```

With ICU4J enabled, patterns can use **named arguments** and the ICU `plural`/`select` constructs. Named
arguments are passed as a single `Map`, not as positional `{0}` / `{1}` arguments. The catalog detects a
lone `Map` argument and formats the pattern with it.

#### Plural

A `plural` switch selects a variant based on a number. Each case is either an **exact number**, matched as
`=N`, or a **CLDR plural keyword** (`zero`, `one`, `two`, `few`, `many`, `other`). The locale's plural
rules map the number to one of these keywords. The number itself is inserted into a case by referencing
the argument name, `{count}`.

Which keywords a language uses, and how each number maps to one, is defined per language in the
[Unicode CLDR Language Plural Rules](https://www.unicode.org/cldr/charts/latest/supplemental/language_plural_rules.html).

```java
new TransUnit(
    Locale.forLanguageTag("en"),
    "file_deleted",
    "{count, plural, =0 {You deleted no files.} =1 {You deleted one file.} other {You deleted {count} files.}}"
);
new TransUnit(
    Locale.forLanguageTag("de"),
    "file_deleted",
    "{count, plural, =0 {Sie haben keine Dateien gelöscht.} =1 {Sie haben eine Datei gelöscht.} other {Sie haben {count} Dateien gelöscht.}}"
);

messageSource.getMessage(
    "file_deleted",
    new Object[] { Map.of("count", 1000) },
    Locale.forLanguageTag("de")
);
```

**Result:** `Sie haben 1.000 Dateien gelöscht.`

#### Select (and gender)

A `select` switch picks the case whose value matches the argument. Use it for any value-based choice such
as grammatical gender. A final `other` case acts as the fallback.

```java
new TransUnit(
    Locale.forLanguageTag("en"),
    "greeting",
    "{recipient_gender, select, feminine {How is she?} masculine {How is he?} other {How are they?}}"
);
new TransUnit(
    Locale.forLanguageTag("de"),
    "greeting",
    "{recipient_gender, select, feminine {Wie geht es ihr?} masculine {Wie geht es ihm?} other {Wie geht es ihnen?}}"
);

messageSource.getMessage(
    "greeting",
    new Object[] { Map.of("recipient_gender", "feminine") },
    Locale.forLanguageTag("de")
);
```

**Result:** `Wie geht es ihr?`

## Resource classes

The `resources` package loads translation files from the classpath or filesystem: `ResourceLoaderBuilder`
reads the resources matching the configured location patterns, `ResourceFileNameParser` derives the
domain and locale from each file name, and the result is a list of `TranslationFile` records
(domain + locale + raw bytes) that a format-specific parser can turn into `TransUnit`s. The sibling
packages [spring-messagesource-xliff](https://github.com/alaugks/spring-messagesource-xliff) and
[spring-messagesource-json](https://github.com/alaugks/spring-messagesource-json) use these classes
as their file-loading stage; a custom file-based source can reuse them as well.

### ResourceLoaderBuilder

Takes one or more [Spring resource location patterns](https://docs.spring.io/spring-framework/reference/core/resources.html#resources-resource-strings)
(e.g. `translations/*`) and reads each matching resource into a `TranslationFile`. The patterns are
resolved with Spring's
[`PathMatchingResourcePatternResolver`](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/core/io/support/PathMatchingResourcePatternResolver.html)
and follow that resolver's conventions, e.g. the `classpath*:` prefix to search across all matching
classpath locations and the Ant-style wildcards `*`, `**` and `?`. Pattern resolution and read errors
are wrapped in a `CatalogMessageSourceRuntimeException`.

#### Options

| Method                                                           | Default                  | Description                                                                                                                                                                                               |
|------------------------------------------------------------------|--------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `builder(Locale defaultLocale, String locationPattern)`          | —                        | Entry point.<br><br>`defaultLocale` is used for files whose name carries no locale part.<br><br>`locationPattern` is the location pattern to scan.                                                        |
| `builder(Locale defaultLocale, List<String> locationPatterns)`   | —                        | Entry point (alternative) for multiple location patterns. Duplicate patterns are eliminated.                                                                                                              |
| `fileExtensions(List<String> fileExtensions)`                    | all extensions           | Restricts loading to resources with one of the given file extensions (without leading dot). Without this setting all discovered resources are loaded.                                                     |
| `fileNameParser(ResourceFileNameParserInterface fileNameParser)` | `ResourceFileNameParser` | Sets a custom file name parser that derives the domain and locale from a file name.                                                                                                                       |
| `build()`                                                        | —                        | Builds the `ResourceLoaderBuilder`. The location patterns are resolved on each call to `getTranslationFiles()`.                                                                                                  |
| `getTranslationFiles()`                                          | —                        | Resolves the location patterns, filters and reads the matching resources and returns them as `TranslationFile`s. File names that do not match the [naming convention](#file-name-convention) are skipped. |

#### Example

```java
ResourceLoaderBuilder loader = ResourceLoaderBuilder
    .builder(
        Locale.forLanguageTag("en"),          // default locale for files without a locale part
        List.of("translations/*")            // location patterns
    )
    .fileExtensions(List.of("ext"))           // optional: accepted file extensions (without leading dot)
    .build();

List<TranslationFileInterface> files = loader.getTranslationFiles();
```

### File name convention

Matching is case-insensitive and the file extension is ignored (`.ext` below stands for any extension).
The domain and the locale part can be separated by `_`, `-` or `.`; language and region by `_` or `-`.
Files without a locale part get the default locale passed to `ResourceLoaderBuilder`.

| File name              | Domain     | Locale  |
|------------------------|------------|---------|
| `messages.ext`         | `messages` | default |
| `messages_de.ext`      | `messages` | `de`    |
| `messages.de.ext`      | `messages` | `de`    |
| `messages_en-US.ext`   | `messages` | `en_US` |
| `payment_de.ext`       | `payment`  | `de`    |

File names that do not match this pattern are ignored by `ResourceLoaderBuilder`.

### Records: Filename and TranslationFile

- `Filename` — the parsed parts of a file name (`domain`, `language`, `region`). `hasLocale()` reports
  whether a language part was present. `locale()` builds the `Locale` from the parts.
- `TranslationFile` — a loaded file: `domain`, `locale` and the raw `content` bytes. The byte content is
  compared by value in `equals`/`hashCode`.

## Interfaces

All interfaces of the package at a glance. Each one can be implemented by third parties; the default
implementation ships with the package.

| Interface                                                                                                                                        | Default implementation   | Description                                                                                                                                                                 |
|--------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [`CatalogInterface`](src/main/java/io/github/alaugks/spring/messagesource/catalog/catalog/CatalogInterface.java)                                 | `TransUnitsCatalog`      | A source of translation units. Contributes eagerly via `getTransUnits()` or lazily via `resolveTransUnit(code, locale)`. Custom sources typically extend `AbstractCatalog`. |
| [`TransUnitInterface`](src/main/java/io/github/alaugks/spring/messagesource/catalog/records/TransUnitInterface.java)                             | `TransUnit`              | A single translation entry: a `(locale, domain, code) -> value` tuple.                                                                                                      |
| [`FilenameInterface`](src/main/java/io/github/alaugks/spring/messagesource/catalog/records/FilenameInterface.java)                               | `Filename`               | The parsed parts of a resource file name: `domain`, `language`, `region`.                                                                                                   |
| [`TranslationFileInterface`](src/main/java/io/github/alaugks/spring/messagesource/catalog/records/TranslationFileInterface.java)                 | `TranslationFile`        | A loaded translation file: `domain`, `locale` and the raw `content` bytes.                                                                                                  |
| [`ResourceFileNameParserInterface`](src/main/java/io/github/alaugks/spring/messagesource/catalog/resources/ResourceFileNameParserInterface.java) | `ResourceFileNameParser` | Parses a `Resource` into a `Filename`. Functional interface; a custom parser can be passed to `ResourceLoaderBuilder` as a lambda.                                                 |
| [`ResourceLoaderBuilderInterface`](src/main/java/io/github/alaugks/spring/messagesource/catalog/resources/ResourceLoaderBuilderInterface.java)                 | `ResourceLoaderBuilder`         | Loads translation resources and returns them as `TranslationFile`s.                                                                                                         |

## License

Licensed under the [Apache License, Version 2.0](LICENSE).

