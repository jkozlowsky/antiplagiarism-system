package pl.lodz.p.it.ssbd2026.ssbd01.mok.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.lodz.p.it.ssbd2026.ssbd01.mok.clients.EmailServiceClient;
import pl.lodz.p.it.ssbd2026.ssbd01.dto.email_messages.ActivationEmailRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class EmailRestClientTest {

    @Mock
    private EmailServiceClient emailServiceClient;

    @InjectMocks
    private EmailRestClient emailRestClient;

    @Test
    void shouldCallFeignClientWithCorrectParameters() {

        String toAddress = "test_user@example.com";
        String token = "super-secret-token";
        String langCode = "pl";


        emailRestClient.sendActivationEmail(toAddress, token, langCode);


        ArgumentCaptor<ActivationEmailRequest> requestCaptor = ArgumentCaptor.forClass(ActivationEmailRequest.class);
        verify(emailServiceClient).sendActivationEmail(requestCaptor.capture());

        ActivationEmailRequest sentRequest = requestCaptor.getValue();

        assertEquals(toAddress, sentRequest.toAddress());
        assertEquals(token, sentRequest.token());
        assertEquals(langCode, sentRequest.langCode());
    }
}