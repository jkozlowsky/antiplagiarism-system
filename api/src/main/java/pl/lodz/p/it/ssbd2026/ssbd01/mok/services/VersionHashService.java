package pl.lodz.p.it.ssbd2026.ssbd01.mok.services;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import pl.lodz.p.it.ssbd2026.ssbd01.config.consts.OneTimeTokenConsts;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.HashEncryptionException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Service
public class VersionHashService {

    @Value("${app.hash.secret}")
    private String secret;

    @PreAuthorize("permitAll()")
    public String hashObjectVersion(UUID id, Integer version){
        try {
            MessageDigest digest = MessageDigest.getInstance(OneTimeTokenConsts.HASHING_ALGORITHM);
            String payload = version + ":" + id.toString() + ":" + secret;
            byte[] bytes = digest.digest(payload.getBytes());

            return getStringFromByteArray(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new HashEncryptionException("Algorithm " + OneTimeTokenConsts.HASHING_ALGORITHM + " does not exist",e);
        }
    }

    @NonNull
    private static String getStringFromByteArray(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(2 * bytes.length);
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }


}
