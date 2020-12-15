package com.matchi

class LocaleHelper {
    enum Country {
        SWEDEN('SE', 'sv'),
        NORWAY('NO', ['no', 'nb']),
        DENMARK('DK', 'da'),
        GERMANY('DE', 'de'),
        FINLAND('FI', 'fi'),
        ESTONIA('ES', 'et'),
        SPAIN('ESP', 'es')

        String iso
        List<String> languages = new LinkedList<String>()

        Country(String iso, String language) {
            this.iso = iso
            this.languages.add(language)
        }

        Country(String iso, List<String> languages) {
            this.iso = iso
            this.languages = languages
        }

        static List<Country> list() {
            values()
        }

        static getByLangKey(String key) {
            values().find {
                return it.languages.any {
                    it.equalsIgnoreCase(key)
                }
            }
        }
    }
}
