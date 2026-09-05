# Package to create a custom Spring MessageSource

This package provides the [MessageSource interface](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/context/MessageSource.html). Internally, it builds a [`ResourceBundle`](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/ResourceBundle.html) and delegates locale fallback handling to it — the same mechanism Spring's own [`ResourceBundleMessageSource`](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/context/support/ResourceBundleMessageSource.html) relies on.

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=alaugks_spring-messagesource-catalog&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=alaugks_spring-messagesource-catalog)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.alaugks/spring-messagesource-catalog.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.alaugks/spring-messagesource-catalog/2.0.0-SNAPSHOT)

## Table of Contents

- [Dependency](#dependency)
  - [Maven](#maven)
  - [Gradle](#gradle)
- [Packages that use the catalog as a base package](#packages-that-use-the-catalog-as-a-base-package)
- [CatalogMessageSource Configuration](#catalogmessagesource-configuration)
  - [Options](#options)
  - [TransUnit Record](#transunit-record)
  - [Configuration example](#configuration-example)
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

## Dependency

### Maven

```xml
<dependency>
    <groupId>io.github.alaugks</groupId>
    <artifactId>spring-messagesource-catalog</artifactId>
    <version>2.0.0-SNAPSHOT</version>
</dependency>
```

### Gradle

```
implementation group: 'io.github.alaugks', name: 'spring-messagesource-catalog', version: '2.0.0-SNAPSHOT'
```

## Packages that use the catalog as a base package

* [spring-messagesource-xliff](https://github.com/alaugks/spring-messagesource-xliff): Xliff MessageSource for Spring
* [spring-messagesource-json](https://github.com/alaugks/spring-messagesource-json): JSON MessageSource for Spring
* [spring-messagesource-db-example](https://github.com/alaugks/spring-messagesource-db-example): Example custom Spring MessageSource from database

## CatalogMessageSource Configuration

### Options

| Method                                                               | Default    | Description                                                                                                                                                                                                                                                  |
|----------------------------------------------------------------------|------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `builder(Locale defaultLocale, List<TransUnitInterface> transUnits)` | —          | Entry point.<br><br>`defaultLocale` is the locale to fall back to when a code cannot be resolved for the requested locale.<br><br>`transUnits` are aggregated into the catalog.                                                                              |
| `enableICU4j()`                                                      | disabled   | Format messages with ICU4J's `com.ibm.icu.text.MessageFormat` instead of the default `java.text.MessageFormat`. Adds named arguments and ICU `plural`/`select` patterns. See [Message formatting](#message-formatting) for details and examples.             |
| `parentMessageSource(MessageSource parentMessageSource)`             | —          | Sets a parent [`MessageSource`](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/context/MessageSource.html) to delegate to. When a code cannot be resolved in the catalog, the lookup falls back to the parent source.  |
| `build()`                                                            | —          | Builds the `CatalogMessageSourceBuilder` from the configured trans units and default locale. Trans units are aggregated at this point; subsequent mutations of the builder have no effect on the returned instance.                                          |

### TransUnit Record

```java
TransUnit(Locale locale, String code, String value);
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

        // en-US
        add(new TransUnit(Locale.forLanguageTag("en-US"), "postcode", "Zip code"));

        // de
        add(new TransUnit(Locale.forLanguageTag("de"), "headline", "Überschrift"));
        add(new TransUnit(Locale.forLanguageTag("de"), "postcode", "Postleitzahl"));
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
    <th>jp**</th>
  </tr>
  </thead>
  <tbody>
  <tr>
    <td>headline</td>
    <td>Headline</td>
    <td>Headline*</td>
    <td>Überschrift</td>
    <td>Headline</td>
  </tr>
  <tr>
    <td>postcode</td>
    <td>Postcode</td>
    <td>Zip code</td>
    <td>Postleitzahl</td>
    <td>Postcode</td>
  </tr>
  </tbody>
</table>

> *Example of a fallback from Language_Region (`en-US`) to Language (`en`). The `id` does not exist in `en-US`, so it tries to select the translation with locale `en`.
>
> **There is no translation for Japanese (`jp`). The default locale transUnits (`en`) are selected.

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
locale from each file name, and the result is a list of `TranslationFile` records
(locale + raw bytes) that a format-specific parser can turn into `TransUnit`s. The sibling
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
| `fileNameParser(ResourceFileNameParserInterface fileNameParser)` | `ResourceFileNameParser` | Sets a custom file name parser that derives the locale from a file name.                                                                                                                                  |
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
Every file name must carry a leading, non-empty segment before the locale part and the extension; the
locale part can be separated by `_`, `-` or `.`; language and region by `_` or `-`. Files without a
locale part get the default locale passed to `ResourceLoaderBuilder`.

| File name              | Locale  |
|------------------------|---------|
| `messages.ext`         | default |
| `messages_de.ext`      | `de`    |
| `messages.de.ext`      | `de`    |
| `messages_en-US.ext`   | `en_US` |

File names that do not match this pattern are ignored by `ResourceLoaderBuilder`.

### Records: Filename and TranslationFile

- `Filename` — the parsed parts of a file name (`language`, `region`). `hasLocale()` reports
  whether a language part was present. `locale()` builds the `Locale` from the parts.
- `TranslationFile` — a loaded file: `locale` and the raw `content` bytes. The byte content is
  compared by value in `equals`/`hashCode`.

## Interfaces

All interfaces of the package at a glance. Each one can be implemented by third parties; the default
implementation ships with the package.

| Interface                                                                                                                                        | Default implementation   | Description                                                                                                                                                                 |
|--------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [`TransUnitInterface`](src/main/java/io/github/alaugks/spring/messagesource/catalog/records/TransUnitInterface.java)                             | `TransUnit`              | A single translation entry: a `(locale, code) -> value` tuple.                                                                                                              |
| [`FilenameInterface`](src/main/java/io/github/alaugks/spring/messagesource/catalog/records/FilenameInterface.java)                               | `Filename`               | The parsed parts of a resource file name: `language`, `region`.                                                                                                             |
| [`TranslationFileInterface`](src/main/java/io/github/alaugks/spring/messagesource/catalog/records/TranslationFileInterface.java)                 | `TranslationFile`        | A loaded translation file: `locale` and the raw `content` bytes.                                                                                                            |
| [`ResourceFileNameParserInterface`](src/main/java/io/github/alaugks/spring/messagesource/catalog/resources/ResourceFileNameParserInterface.java) | `ResourceFileNameParser` | Parses a `Resource` into a `Filename`. Functional interface; a custom parser can be passed to `ResourceLoaderBuilder` as a lambda.                                                 |
| [`ResourceLoaderInterface`](src/main/java/io/github/alaugks/spring/messagesource/catalog/resources/ResourceLoaderInterface.java)                 | `ResourceLoaderBuilder`         | Loads translation resources and returns them as `TranslationFile`s.                                                                                                         |

## License

Licensed under the [Apache License, Version 2.0](LICENSE).

