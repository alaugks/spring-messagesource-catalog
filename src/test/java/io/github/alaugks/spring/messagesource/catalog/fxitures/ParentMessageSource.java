package io.github.alaugks.spring.messagesource.catalog.fxitures;

import java.text.MessageFormat;
import java.util.Locale;
import org.jspecify.annotations.Nullable;
import org.springframework.context.support.AbstractMessageSource;

public class ParentMessageSource extends AbstractMessageSource {

    @Override
    protected @Nullable MessageFormat resolveCode(String code, Locale locale) {
        if (code.equals("parent-messagesource-code")) {
            return new MessageFormat("ParentMessageSource with args: {0,number,integer}", locale);
        }
        return null;
    }
}
