package pl.lodz.p.it.ssbd2026.ssbd01.utils.consts;

public enum TestConsts {
    BASE_ENDPOINT("http://localhost");

    private final String keyName;

TestConsts(String keyName) {
        this.keyName = keyName;
    }

    public String getKeyName() {
        return keyName;
    }
}


