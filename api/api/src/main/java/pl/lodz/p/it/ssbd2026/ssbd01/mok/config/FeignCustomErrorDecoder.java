package pl.lodz.p.it.ssbd2026.ssbd01.mok.config;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.stereotype.Component;
import pl.lodz.p.it.ssbd2026.ssbd01.exceptions.EmailSendingException;

@Component
public class FeignCustomErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() >= 400 && response.status() <= 599) {

            return new EmailSendingException("error.email.microservice.failure");
        }

        return defaultErrorDecoder.decode(methodKey, response);
    }
}