package pl.lodz.p.it.ssbd2026.ssbd01.utils.config;

import com.github.curiousoddman.rgxgen.RgxGen;

public class CommonFunctions {

    public String generateRandomStringOfLength(int length) {
        return RgxGen.parse("[A-Za-z0-9]{" + length + "}").generate();
    }

    public String generateRandomNumberfLength(int length) {
        return RgxGen.parse("[1-9]{1}[0-9]{" + (length - 1) + "}").generate();
    }

    public String generateRandomLettersOfLength(int length) {
        return RgxGen.parse("[A-Za-z]{" + length + "}").generate();
    }

    public String generateStringNotMatchingRegex(String regex) {
        return RgxGen.parse(regex).generateNotMatching();
    }
}
