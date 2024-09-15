# Catalog to create a custom Spring MessageSource

This package extends the AbstractMessageSource and therefore the MessageSource interface. 

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=alaugks_spring-messagesource-base&metric=alert_status&token=3d2b79af1f0f0ab6089e565495b4db6f621e9a13)](https://sonarcloud.io/summary/overall?id=alaugks_spring-messagesource-base)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.alaugks/spring-messagesource-catalog.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.alaugks/spring-messagesource-catalog/0.1.0)

## Dependency

### Maven

```xml
<dependencies>
    <dependency>
        <groupId>io.github.alaugks</groupId>
        <artifactId>spring-messagesource-catalog</artifactId>
        <version>0.1.0</version>
    </dependency>
</dependencies>
```

### Gradle

```
implementation group: 'io.github.alaugks', name: 'spring-messagesource-catalog', version: '0.1.0'
```

## Packages that use the catalog as a base package

* [spring-messagesource-xliff](https://github.com/alaugks/spring-messagesource-xliff): MessageSource for translations from XLIFF files.

## CatalogMessageSource Configuration

### Options

`builder(CatalogInterface catalogSource, Locale defaultLocale)` (required)

* Argument `CatalogInterface catalogSource`: CatalogInterface<br>
* Argument `Locale defaultLocale`: Default Locale

`defaultDomain(String defaultDomain)`
* If the default domain not set, the default is **messages**.

### TransUnit Record

If the `String domain` argument is not set, the default is the **messages** domain.

```java
TransUnit(Locale locale, String code, String value);

TransUnit(Locale locale, String code, String value, String domain);
```


### Configuration example

#### MessageConfig with List of TransUnits 

```java
import io.github.alaugks.spring.messagesource.catalog.catalog.CatalogHandler;
import io.github.alaugks.spring.messagesource.catalog.records.TransUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageConfig {
    
    List<TransUnit> transUnits = new ArrayList<>() {{
        // en
        add(new TransUnit(Locale.forLanguageTag("en"), "headline", "Headline"));
        add(new TransUnit(Locale.forLanguageTag("en"), "postcode", "Postcode"));
        add(new TransUnit(Locale.forLanguageTag("en"), "validation.email.exists", "Your email {0} has been registered."));
        add(new TransUnit(Locale.forLanguageTag("en"), "default-message", "This is a default message."));
        add(new TransUnit(Locale.forLanguageTag("en"), "headline", "Payment", "payment"));
        add(new TransUnit(Locale.forLanguageTag("en"), "form.expiry_date", "Expiry date", "payment"));

        // en-US
        add(new TransUnit(Locale.forLanguageTag("en-US"), "postcode", "Zip code"));
        add(new TransUnit(Locale.forLanguageTag("en-US"), "form.expiry_date", "Expiration date", "payment"));

        // de
        add(new TransUnit(Locale.forLanguageTag("de"), "headline", "Überschrift"));
        add(new TransUnit(Locale.forLanguageTag("de"), "postcode", "Postleitzahl"));
        add(new TransUnit(Locale.forLanguageTag("de"), "validation.email.exists", "Ihre E-Mail {0} wurde registriert."));
        add(new TransUnit(Locale.forLanguageTag("de"), "default-message", "Das ist ein Standardtext."));
        add(new TransUnit(Locale.forLanguageTag("de"), "headline", "Zahlung", "payment"));
        add(new TransUnit(Locale.forLanguageTag("de"), "form.expiry_date", "Ablaufdatum", "payment"));
    }};

    @Bean
    public MessageSource messageSource() {
        return CatalogMessageSourceBuilder
            .builder(new TransUnitsCatalog(this.transUnits), Locale.forLanguageTag("en"))
            .build();
	}
}
```

#### With custom CatalogInterface

##### CatalogInterface

```java
import java.util.List;

import io.github.alaugks.spring.messagesource.catalog.catalog.CatalogAbstract;
import io.github.alaugks.spring.messagesource.catalog.records.TransUnit;

public class MyCustomCatalog extends CatalogAbstract {

    List<TransUnit> transUnits;
	
    @Override
    public List<TransUnit> getTransUnits() {
        return this.transUnits;
    }
    
    @Override
    public void build() {
        // Build a list with TransUnit from any kind of source.
        this.transUnits = new ArrayList<>() {{
            // ...
        }};
    }
}
```

##### MessageConfig

```java
import io.github.alaugks.spring.messagesource.catalog.catalog.CatalogHandler;
import io.github.alaugks.spring.messagesource.catalog.records.TransUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageConfig {
    @Bean
    public MessageSource messageSource() {
        return CatalogMessageSourceBuilder
            .builder(new MyCustomCatalog(), Locale.forLanguageTag("en"))
            .build();
	}
}
```

### Target values

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
    <td>validation.email.exists*<br>messages.validation.email.exists</td>
    <td>Your email {0} has been registered.</td>
    <td>Your email {0} has been registered.**</td>
    <td>Ihre E-Mail {0} wurde registriert.</td>
    <td>Your email {0} has been registered.</td>
  </tr>
  <tr>
    <td>default-message*<br>messages.default-message</td>
    <td>This is a default message.</td>
    <td>This is a default message.**</td>
    <td>Das ist ein Standardtext.</td>
    <td>This is a default message.</td>
  </tr>
  <tr>
    <td>payment.headline</td>
    <td>Payment</td>
    <td>Payment**</td>
    <td>Zahlung</td>
    <td>Payment</td>
  </tr>
  <tr>
    <td>payment.form.expiry_date</td>
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


## Support

If you have questions, comments or feature requests please use the [Discussions](https://github.com/alaugks/spring-messagesource-catalog/discussions) section.

<a name="a8"></a>



