import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
// import LanguageDetector from 'i18next-browser-languagedetector';

import enTranslations from '../locales/en.json';
import plTranslations from '../locales/pl.json';
import ukTranslations from '../locales/uk.json';

i18n
    // .use(LanguageDetector) //TODO: Zmienić na ręczne ustawienie i18n a nie automat z przeglądarki
    .use(initReactI18next)
    .init({
        resources: {
            en: { translation: enTranslations },
            pl: { translation: plTranslations },
            uk: { translation: ukTranslations }
        },
        fallbackLng: 'en',

        interpolation: {
            escapeValue: false
        }
    });

export default i18n;