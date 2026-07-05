export default {
    darkMode: 'class',
    content: [
        "./index.html",
        "./src/**/*.{js,ts,jsx,tsx}",
    ],
    theme: {
        extend: {
            fontFamily: {
                sans: ['Inter', 'sans-serif'],
            },
            colors: {
                base: 'var(--bg-base)',
                surface: 'var(--bg-surface)',
                border: 'var(--border-default)',
                active: 'var(--bg-active)',
                brand: {
                    DEFAULT: 'var(--brand-main)',
                    hover: 'var(--brand-hover)',
                }
            },
            textColor: {
                primary: 'var(--text-primary)',
                secondary: 'var(--text-secondary)',
            }
        },
    },
    plugins: [require('@tailwindcss/typography')],
}