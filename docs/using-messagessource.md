# Using the MessageSource

## Thymeleaf

With the configured MessageSource, the translations are available in Thymeleaf.

```html
<!-- Default domain: messages -->

<!-- "Headline" -->
<h1 th:text="#{headline}"/>
<h1 th:text="#{messages.headline}"/>

<!-- "Postcode" -->
<label th:text="#{postcode}"/>
<label th:text="#{messages.postcode}"/>

<!-- "Your email john.doe@example.com has been registered." -->
<span th:text="#{email-notice('john.doe@example.com')}"/>
<span th:text="#{messages.email-notice('john.doe@example.com')}"/>

<!-- "This is a default message." -->
<span th:text="${#messages.msgOrNull('not-exists-id')} ?: #{default-message}"/>
<span th:text="${#messages.msgOrNull('not-exists-id')} ?: #{messages.default-message}"/>


<!-- Domain: payment -->

<!-- "Payment" -->
<h2 th:text="#{payment.headline}"/>

<!-- "Expiry date" -->
<strong th:text="#{payment.expiry_date}"/>
```


## Service (Dependency Injection)

The MessageSource can be set via Autowire to access the translations.

```java
import org.springframework.context.MessageSource;

private final MessageSource messageSource;

// Autowire MessageSource
public MyClass(MessageSource messageSource) {
    this.messageSource = messageSource;
}


// Default domain: messages

// "Headline"
this.messageSource.getMessage("headline", null, locale);
this.messageSource.getMessage("messages.headline", null, locale);

// "Postcode"
this.messageSource.getMessage("postcode", null, locale);
this.messageSource.getMessage("messages.postcode", null, locale);

// "Your email john.doe@example.com has been registered."
Object[] args = {"john.doe@example.com"};
this.messageSource.getMessage("email-notice", args, locale);
this.messageSource.getMessage("messages.email-notice", args, locale);

// "This is a default message."
//String defaultMessage = this.messageSource.getMessage("default-message", null, locale);
String defaultMessage = this.messageSource.getMessage("messages.default-message", null, locale);
this.messageSource.getMessage("not-exists-id", null, defaultMessage, locale);


// Domain: payment

// "Payment"
this.messageSource.getMessage("payment.headline", null, locale);

// "Expiry date"
this.messageSource.getMessage("payment.expiry-date", null, locale);
```

## Custom Validation Messages

The article [Custom Validation MessageSource in Spring Boot](https://www.baeldung.com/spring-custom-validation-message-source) describes how to use custom validation messages.

## More Information

### MessageSource, Internationalization and Thymeleaf
* [Guide to Internationalization in Spring Boot](https://www.baeldung.com/spring-boot-internationalization)
* [How to Internationalize a Spring Boot Application](https://reflectoring.io/spring-boot-internationalization/)
* [Spring Boot internationalization i18n: Step-by-step with examples](https://lokalise.com/blog/spring-boot-internationalization/)

### Caching
* [A Guide To Caching in Spring](https://www.baeldung.com/spring-cache-tutorial)
* [Implementing a Cache with Spring Boot](https://reflectoring.io/spring-boot-cache/)
