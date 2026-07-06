// SPDX-License-Identifier: Apache-2.0
// Copyright 2024 André Laugks <alaugks@gmail.com>

package io.github.alaugks.spring.messagesource.catalog;

import io.github.alaugks.spring.messagesource.catalog.catalog.CatalogInterface;
import io.github.alaugks.spring.messagesource.catalog.records.TransUnitInterface;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.util.Assert;

/**
 * Base class for fluent builders that share the common catalog configuration: the configured
 * sources, default locale, default domain, ICU4J formatting, and an optional parent message
 * source.
 *
 * <p>The recursive type parameter {@code B} lets each concrete builder return its own type from
 * the shared fluent methods, so chaining stays type-safe across subclasses (including sibling
 * builders in other packages).
 *
 * @param <B> the concrete builder type returned by the fluent methods
 */
public abstract class AbstractCatalogMessageSourceBuilder<B extends AbstractCatalogMessageSourceBuilder<B>> {

    /** String constant used to separate the domain and the code in message keys. */
    protected static final String DOMAIN_DIVIDER = ".";

    /** Sources aggregated additively; their late-binding methods are consulted in insertion order. */
    private final List<CatalogInterface> sources = new ArrayList<>();

    /** Locale used as fallback when a code cannot be resolved for the requested locale. */
    private final Locale defaultLocale;

    /** Domain applied when a code is requested without an explicit domain. */
    private String defaultDomain = CatalogMessageSourceBuilder.DEFAULT_DOMAIN;

    /** Whether messages are formatted with ICU4J. */
    private boolean useICU4j = false;

    /** Optional parent consulted when a code cannot be resolved locally. */
    private MessageSource parentMessageSource = null;

    /** Separator between domain and code in a qualified code. */
    private String domainDivider = DOMAIN_DIVIDER;

    protected AbstractCatalogMessageSourceBuilder(Locale defaultLocale) {
        Assert.notNull(defaultLocale, "Argument defaultLocale must not be null");

        this.defaultLocale = defaultLocale;
    }

    /**
     * Appends another source. Sources are aggregated additively at build time; their
     * {@code resolveTransUnit} late-binding methods are consulted in the order they were added.
     *
     * @param source the source to append; must not be {@code null}
     * @return this builder
     */
    public B addSource(CatalogInterface source) {
        Assert.notNull(source, "Argument source must not be null");

        this.sources.add(source);

        return (B) this;
    }

    /**
     * Convenience overload of {@link #addSource(CatalogInterface)} that wraps the given
     * trans units in a {@link TransUnitsCatalog}.
     *
     * @param transUnits the trans units to append as a new source; must not be {@code null}
     * @return this builder
     */
    public B addSource(List<TransUnitInterface> transUnits) {
        Assert.notNull(transUnits, "Argument transUnits must not be null");

        return this.addSource(new TransUnitsCatalog(transUnits));
    }

    /**
     * Returns the configured sources in insertion order.
     *
     * @return the configured sources
     */
    protected List<CatalogInterface> getSources() {
        return this.sources;
    }

    /**
     * Returns the default locale used as a fallback when a code cannot be resolved for the
     * requested locale.
     *
     * @return the default locale; never {@code null}
     */
    protected Locale getDefaultLocale() {
        return this.defaultLocale;
    }

    /**
     * Returns the configured default domain.
     *
     * @return the default domain
     * @see #defaultDomain(String)
     */
    protected String getDefaultDomain() {
        return this.defaultDomain;
    }

    /**
     * Sets the default domain. Codes stored under this domain are also accessible via
     * their name without the domain prefix; codes stored under any other domain require the
     * {@code <domain>.<code>} prefix.
     *
     * @param defaultDomain the default domain; must not be {@code null}
     * @return this builder
     */
    public B defaultDomain(String defaultDomain) {
        Assert.notNull(defaultDomain, "Argument defaultDomain must not be null");

        this.defaultDomain = defaultDomain;

        return (B) this;
    }

    /**
     * Returns whether ICU4J message formatting is enabled.
     *
     * @return {@code true} if ICU4J formatting is enabled, {@code false} otherwise
     * @see #enableICU4j()
     */
    protected boolean isICU4jEnabled() {
        return this.useICU4j;
    }

    /**
     * Configures whether ICU4J message formatting is enabled.
     *
     * @param useICU4j {@code true} to enable ICU4J message formatting,
     *                 {@code false} to use standard Java message formatting
     * @return this builder instance for method chaining
     */
    public B useICU4j(boolean useICU4j) {
        this.useICU4j = useICU4j;

        return (B) this;
    }

    /**
     * Enables ICU4J message formatting. When enabled, resolved messages are formatted with
     * {@link com.ibm.icu.text.MessageFormat} (supporting ICU syntax such as named arguments
     * and {@code plural}/{@code select}); otherwise {@link java.text.MessageFormat} is used.
     *
     * @return this builder
     */
    public B enableICU4j() {
        this.useICU4j = true;

        return (B) this;
    }

    /**
     * Sets the parent message source to be used as a fallback when a message cannot be resolved
     * from the current sources or domains.
     *
     * @param messageSource the parent message source; must not be {@code null}
     * @return this builder
     */
    public B parentMessageSource(MessageSource messageSource) {
        this.parentMessageSource = messageSource;

        return (B) this;
    }

    /**
     * Returns the parent message source used as a fallback, or {@code null} if none is configured.
     *
     * @return the parent message source, or {@code null}
     * @see #parentMessageSource(MessageSource)
     */
    protected MessageSource getParentMessageSource() {
        return this.parentMessageSource;
    }

    /**
     * Sets the domain divider to be used when building domain-based message catalogs.
     * Default is {@code .}
     *
     * @param domainDivider the domain divider string; must not be {@code null}
     * @return this builder instance for method chaining
     */
    public B domainDivider(String domainDivider) {
        this.domainDivider = domainDivider;

        return (B) this;
    }

    /**
     * Retrieves the domain divider used to separate domains in message codes.
     * The domain divider is used in domain-based message catalogs to distinguish
     * domain-specific message entries.
     *
     * @return the domain divider string
     */
    protected String getDomainDivider() {
        return this.domainDivider;
    }
}
