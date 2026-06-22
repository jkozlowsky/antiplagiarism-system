package pl.lodz.p.it.ssbd2026.emailservice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import pl.lodz.p.it.ssbd2026.emailservice.config.i18n.Translator;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TranslatorTest {

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private Translator translator;

    @Test
    void shouldTranslateForUserWithSpecificLanguage() {
        when(messageSource.getMessage(eq("test.key"), any(), eq(Locale.of("pl"))))
                .thenReturn("Wiadomość testowa");

        String result = translator.translateForUser("test.key", "pl");

        assertEquals("Wiadomość testowa", result);
    }

    @Test
    void shouldFallbackToEnglishWhenLangCodeIsNull() {
        when(messageSource.getMessage(eq("test.key"), any(), eq(Locale.ENGLISH)))
                .thenReturn("Test message");

        String result = translator.translateForUser("test.key", null);

        assertEquals("Test message", result);
    }
}