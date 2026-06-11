package pl.lodz.p.it.ssbd2026.ssbd01.mpp.services;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import org.springframework.stereotype.Service;

@Service
public class GoogleTranslationService {
    private final Translate translate = TranslateOptions.getDefaultInstance().getService();

    public String translateText(String text, String sourceLanguage, String targetLanguageCode) {
        Translation translation = translate.translate(
                text,
                Translate.TranslateOption.sourceLanguage(sourceLanguage),
                Translate.TranslateOption.targetLanguage(targetLanguageCode)
        );

        return translation.getTranslatedText();
    }
}
