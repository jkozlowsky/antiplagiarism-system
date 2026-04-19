package pl.lodz.p.it.ssbd2026.ssbd01.utils.consts;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TestConsts {
    BASE_ENDPOINT("http://localhost");

    private final String keyName;
}


