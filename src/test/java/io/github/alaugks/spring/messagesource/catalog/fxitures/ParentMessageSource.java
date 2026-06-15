package io.github.alaugks.spring.messagesource.catalog.fxitures;

import java.text.MessageFormat;
import java.util.Locale;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.lang.Nullable;

public class ParentMessageSource extends AbstractMessageSource {

    @Nullable
    @Override
    protected MessageFormat resolveCode(String code, Locale locale) {
        if (code.equals("parent-messagesource-code")) {
            return new MessageFormat("ParentMessageSource with args: {0,number,integer}");
        }
        return null;
    }
}
