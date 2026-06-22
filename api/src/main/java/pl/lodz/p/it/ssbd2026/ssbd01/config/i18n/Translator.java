package pl.lodz.p.it.ssbd2026.ssbd01.config.i18n;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class Translator {

    private final MessageSource messageSource;

    public String translate(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }

    public String translate(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }

    public String translateForUser(String key, String langCode, Object... args) {
        Locale locale = (langCode != null) ? Locale.of(langCode.toLowerCase()) : Locale.ENGLISH;
        return messageSource.getMessage(key, args, locale);
    }
}
