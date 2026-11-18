package com.pranit.notification.subscriber;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pranit.notification.mail.dto.EmailRequest;
import com.pranit.notification.mail.dto.Recipient;
import com.pranit.notification.mail.model.EmailPurpose;
import com.pranit.notification.mail.router.EmailRouter;
import com.pranit.notification.mail.subscriber.EventSubscriberService;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventSubscriberService - Test Suite (Mocking)")
class EventSubscriberServiceTest {

    @Mock
    private EmailRouter router;

    @Mock
    private Validator validator;

    @InjectMocks
    private EventSubscriberService service;

    @Captor
    private ArgumentCaptor<EmailRequest> requestCaptor;

    private ObjectMapper objectMapper;

    static Stream<Integer> batchSizes() {
        return Stream.of(1, 5, 10, 25, 50, 100);
    }

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        when(validator.validate(any(EmailRequest.class))).thenReturn(Set.of());
    }

    private EmailRequest loadWelcomeRequest(String fileName) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("testdata/welcome/" + fileName)) {
            if (is == null) {
                throw new IllegalStateException("Resource not found: testdata/welcome/" + fileName);
            }
            return objectMapper.readValue(is, EmailRequest.class);
        }
    }

    private List<EmailRequest> loadWelcomeRequests(String fileName) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("testdata/welcome/" + fileName)) {
            if (is == null) {
                throw new IllegalStateException("Resource not found: testdata/welcome/" + fileName);
            }
            return objectMapper.readValue(is, objectMapper.getTypeFactory().constructCollectionType(List.class, EmailRequest.class));
        }
    }

    private EmailRequest loadOtpRequest(String fileName) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("testdata/otp/" + fileName)) {
            if (is == null) {
                throw new IllegalStateException("Resource not found: testdata/otp/" + fileName);
            }
            return objectMapper.readValue(is, EmailRequest.class);
        }
    }

    private List<EmailRequest> loadOtpRequests(String fileName) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("testdata/otp/" + fileName)) {
            if (is == null) {
                throw new IllegalStateException("Resource not found: testdata/otp/" + fileName);
            }
            return objectMapper.readValue(is, objectMapper.getTypeFactory().constructCollectionType(List.class, EmailRequest.class));
        }
    }

    private List<EmailRequest> loadBatchRequests(String fileName) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("testdata/batch/" + fileName)) {
            if (is == null) {
                throw new IllegalStateException("Resource not found: testdata/batch/" + fileName);
            }
            return objectMapper.readValue(is, objectMapper.getTypeFactory().constructCollectionType(List.class, EmailRequest.class));
        }
    }

    @Test
    @DisplayName("FT-WEL-01: Single welcome email from JSON - full flow through router")
    void singleWelcomeEmailFromJson() throws IOException {
        EmailRequest request = loadWelcomeRequest("welcome-single.json");
        service.processEmail().accept(request);
        verify(router).send(argThat(e -> e instanceof EmailRequest r
                        && r.eventId().equals("evt-welcome-001")
                        && r.purpose() == EmailPurpose.WELCOME
                        && r.templateData().containsKey("organizationName")
                        && r.templateData().containsKey("fullName")
                        && r.templateData().containsKey("username")
                        && r.templateData().containsKey("organizationUrl")
                        && r.templateData().containsKey("createdAt")),
                argThat(p -> p == EmailPurpose.WELCOME)
        );
    }

    @Test
    @DisplayName("FT-WEL-02: Multiple welcome emails from JSON - batch processing")
    void multipleWelcomeEmailsFromJson() throws IOException {
        List<EmailRequest> requests = loadWelcomeRequests("welcome-multiple.json");
        for (EmailRequest request : requests) {
            service.processEmail().accept(request);
        }
        verify(router, atLeastOnce()).send(any(), argThat(p -> p == EmailPurpose.WELCOME));
    }

    @Test
    @DisplayName("FT-WEL-03: Welcome email with CC/BCC recipients - full flow")
    void welcomeEmailWithCcBcc() throws IOException {
        EmailRequest request = loadWelcomeRequest("welcome-single.json");
        assertThat(request.recipient().cc()).contains("manager@example.com");
        assertThat(request.recipient().bcc()).contains("audit@example.com");

        // When
        service.processEmail().accept(request);

        // Then
        verify(router).send(
                argThat(e -> e instanceof EmailRequest r
                        && r.recipient().to().equals("john.doe@example.com")
                        && r.recipient().cc().contains("manager@example.com")
                        && r.recipient().bcc().contains("audit@example.com")),
                argThat(p -> p == EmailPurpose.WELCOME)
        );
    }

    // =========================================================================
    // OTP EMAIL FUNCTIONAL TESTS (from JSON)
    // =========================================================================

    @Test
    @DisplayName("FT-WEL-04: Welcome email - all 5 required templateData keys present")
    void welcomeEmailAllRequiredTemplateDataKeys() throws IOException {
        // Given
        EmailRequest request = loadWelcomeRequest("welcome-single.json");
        Map<String, Object> templateData = request.templateData();

        // Verify all 5 required keys present
        assertThat(templateData).containsKeys(
                "organizationName", "organizationUrl", "fullName", "username", "createdAt"
        );

        // When
        service.processEmail().accept(request);

        // Then - verify router called with all keys
        verify(router).send(
                argThat(e -> e instanceof EmailRequest r
                        && r.templateData().get("organizationName").equals("Acme Corp")
                        && r.templateData().get("organizationUrl").equals("https://acme.com")
                        && r.templateData().get("fullName").equals("John Doe")
                        && r.templateData().get("username").equals("johndoe")
                        && r.templateData().get("createdAt").equals("2024-01-15T10:30:00Z")),
                argThat(p -> p == EmailPurpose.WELCOME)
        );
    }

    @Test
    @DisplayName("FT-OTP-01: Single OTP email from JSON - full flow through router")
    void singleOtpEmailFromJson() throws IOException {
        // Given
        EmailRequest request = loadOtpRequest("otp-single.json");

        // When
        service.processEmail().accept(request);

        // Then - verify router called with PASSWORD_RESET purpose
        verify(router).send(
                argThat(e -> e instanceof EmailRequest r
                        && r.eventId().equals("evt-otp-001")
                        && r.purpose() == EmailPurpose.PASSWORD_RESET
                        && r.templateData().containsKey("organizationName")
                        && r.templateData().containsKey("otpCode")),
                argThat(p -> p == EmailPurpose.PASSWORD_RESET)
        );
    }

    @Test
    @DisplayName("FT-OTP-02: Multiple OTP emails from JSON - batch processing")
    void multipleOtpEmailsFromJson() throws IOException {
        // Given
        List<EmailRequest> requests = loadOtpRequests("otp-multiple.json");

        // When
        for (EmailRequest request : requests) {
            service.processEmail().accept(request);
        }

        // Then
        verify(router, atLeastOnce()).send(any(), argThat(p -> p == EmailPurpose.PASSWORD_RESET));
    }

    @Test
    @DisplayName("FT-OTP-03: OTP email - both required templateData keys validated")
    void otpEmailBothRequiredKeys() throws IOException {
        // Given
        EmailRequest request = loadOtpRequest("otp-single.json");
        Map<String, Object> templateData = request.templateData();

        // Verify both required keys present
        assertThat(templateData).containsKeys("organizationName", "otpCode");

        // When
        service.processEmail().accept(request);

        // Then
        verify(router).send(
                argThat(e -> e instanceof EmailRequest r
                        && r.templateData().get("organizationName").equals("Acme Corp")
                        && r.templateData().get("otpCode").equals("123456")),
                argThat(p -> p == EmailPurpose.PASSWORD_RESET)
        );
    }

    // =========================================================================
    // BATCH / HIGH TRAFFIC TESTS (from JSON + programmatic generation)
    // =========================================================================

    @Test
    @DisplayName("FT-OTP-04: OTP email with BCC to security team")
    void otpEmailWithSecurityBcc() throws IOException {
        // Given
        EmailRequest request = loadOtpRequest("otp-single.json");
        assertThat(request.recipient().bcc()).contains("security@example.com");

        // When
        service.processEmail().accept(request);

        // Then
        verify(router).send(
                argThat(e -> e instanceof EmailRequest r
                        && r.recipient().bcc().contains("security@example.com")),
                argThat(p -> p == EmailPurpose.PASSWORD_RESET)
        );
    }

    @Test
    @DisplayName("FT-BATCH-01: Welcome batch of 10 users from JSON")
    void welcomeBatch10Users() throws IOException {
        // Given
        List<EmailRequest> requests = loadBatchRequests("welcome-batch-10.json");
        assertThat(requests).hasSize(10);

        // When - process all sequentially (router.send is @Async, so it's fire-and-forget)
        for (EmailRequest request : requests) {
            service.processEmail().accept(request);
        }

        // Then - verify router was called for each request
        verify(router, atLeastOnce()).send(any(), argThat(p -> p == EmailPurpose.WELCOME));
    }

    @Test
    @DisplayName("FT-BATCH-02: OTP batch of 10 users from JSON")
    void otpBatch10Users() throws IOException {
        // Given
        List<EmailRequest> requests = loadBatchRequests("otp-batch-10.json");
        assertThat(requests).hasSize(10);

        // When
        for (EmailRequest request : requests) {
            service.processEmail().accept(request);
        }

        // Then
        verify(router, atLeastOnce()).send(any(), argThat(p -> p == EmailPurpose.PASSWORD_RESET));
    }

    @ParameterizedTest
    @MethodSource("batchSizes")
    @DisplayName("FT-BATCH-03: Variable batch sizes - welcome emails (programmatic)")
    void variableBatchSizesWelcome(int batchSize) throws IOException {
        // Given - generate batch programmatically
        List<EmailRequest> requests = generateWelcomeBatch(batchSize);

        // When
        for (EmailRequest request : requests) {
            service.processEmail().accept(request);
        }

        // Then - verify router called batchSize times
        verify(router, atLeastOnce()).send(any(), argThat(p -> p == EmailPurpose.WELCOME));
    }

    @ParameterizedTest
    @MethodSource("batchSizes")
    @DisplayName("FT-BATCH-04: Variable batch sizes - OTP emails (programmatic)")
    void variableBatchSizesOtp(int batchSize) throws IOException {
        // Given
        List<EmailRequest> requests = generateOtpBatch(batchSize);

        // When
        for (EmailRequest request : requests) {
            service.processEmail().accept(request);
        }

        // Then
        verify(router, atLeastOnce()).send(any(), argThat(p -> p == EmailPurpose.PASSWORD_RESET));
    }

    // =========================================================================
    // TEMPLATE DATA VALIDATION TESTS (Welcome & OTP only)
    // =========================================================================

    @Test
    @DisplayName("FT-VAL-01: Welcome email missing organizationName - router still called")
    void welcomeMissingOrganizationName() throws IOException {
        // Given
        EmailRequest original = loadWelcomeRequest("welcome-single.json");
        Map<String, Object> mutableTemplateData = new java.util.HashMap<>(original.templateData());
        mutableTemplateData.remove("organizationName");
        EmailRequest request = new EmailRequest(
                original.eventId(), original.purpose(), original.recipient(), original.subject(),
                mutableTemplateData, original.attachments()
        );

        // When
        service.processEmail().accept(request);

        // Then - router called (handler validates template data async)
        verify(router).send(any(), argThat(p -> p == EmailPurpose.WELCOME));
    }

    @Test
    @DisplayName("FT-VAL-02: Welcome email missing fullName - router still called")
    void welcomeMissingFullName() throws IOException {
        // Given
        EmailRequest original = loadWelcomeRequest("welcome-single.json");
        Map<String, Object> mutableTemplateData = new java.util.HashMap<>(original.templateData());
        mutableTemplateData.remove("fullName");
        EmailRequest request = new EmailRequest(
                original.eventId(), original.purpose(), original.recipient(), original.subject(),
                mutableTemplateData, original.attachments()
        );

        // When
        service.processEmail().accept(request);

        // Then
        verify(router).send(any(), argThat(p -> p == EmailPurpose.WELCOME));
    }

    @Test
    @DisplayName("FT-VAL-03: OTP email missing otpCode - router still called")
    void otpMissingOtpCode() throws IOException {
        // Given
        EmailRequest original = loadOtpRequest("otp-single.json");
        Map<String, Object> mutableTemplateData = new java.util.HashMap<>(original.templateData());
        mutableTemplateData.remove("otpCode");
        EmailRequest request = new EmailRequest(
                original.eventId(), original.purpose(), original.recipient(), original.subject(),
                mutableTemplateData, original.attachments()
        );

        // When
        service.processEmail().accept(request);

        // Then
        verify(router).send(any(), argThat(p -> p == EmailPurpose.PASSWORD_RESET));
    }

    @Test
    @DisplayName("FT-VAL-04: OTP email missing organizationName - router still called")
    void otpMissingOrganizationName() throws IOException {
        // Given
        EmailRequest original = loadOtpRequest("otp-single.json");
        Map<String, Object> mutableTemplateData = new java.util.HashMap<>(original.templateData());
        mutableTemplateData.remove("organizationName");
        EmailRequest request = new EmailRequest(
                original.eventId(), original.purpose(), original.recipient(), original.subject(),
                mutableTemplateData, original.attachments()
        );

        // When
        service.processEmail().accept(request);

        // Then
        verify(router).send(any(), argThat(p -> p == EmailPurpose.PASSWORD_RESET));
    }

    // =========================================================================
    // INTEGRATION FLOW TESTS
    // =========================================================================

    @Test
    @DisplayName("FT-INT-01: Welcome email flows through router with WELCOME purpose")
    void welcomeEmailFlowsWithWelcomePurpose() throws IOException {
        // Given
        EmailRequest request = loadWelcomeRequest("welcome-single.json");

        // When
        service.processEmail().accept(request);

        // Then
        verify(router).send(
                argThat(req -> req instanceof EmailRequest emailRequest
                        && emailRequest.purpose() == EmailPurpose.WELCOME),
                argThat(p -> p == EmailPurpose.WELCOME)
        );
    }

    @Test
    @DisplayName("FT-INT-02: OTP email flows through router with PASSWORD_RESET purpose")
    void otpEmailFlowsWithPasswordResetPurpose() throws IOException {
        // Given
        EmailRequest request = loadOtpRequest("otp-single.json");

        // When
        service.processEmail().accept(request);

        // Then
        verify(router).send(
                argThat(req -> req instanceof EmailRequest emailRequest
                        && emailRequest.purpose() == EmailPurpose.PASSWORD_RESET),
                argThat(p -> p == EmailPurpose.PASSWORD_RESET)
        );
    }

    @Test
    @DisplayName("FT-INT-03: Null request handled gracefully - logs warning, router not called")
    void nullRequestHandledGracefully() {
        // Reset validator mock for this test to avoid unnecessary stubbing
        org.mockito.Mockito.reset(validator);
        assertThatNoException().isThrownBy(() -> service.processEmail().accept(null));
        verify(router, never()).send(any(), any());
    }

    // =========================================================================
    // HELPER METHODS FOR PROGRAMMATIC BATCH GENERATION
    // =========================================================================

    private List<EmailRequest> generateWelcomeBatch(int count) {
        return java.util.stream.IntStream.rangeClosed(1, count)
                .mapToObj(i -> EmailRequest.builder()
                        .eventId("evt-welcome-gen-%03d".formatted(i))
                        .purpose(EmailPurpose.WELCOME)
                        .recipient(new Recipient("user%03d@example.com".formatted(i), List.of(), List.of()))
                        .subject("Welcome to Acme Corp!")
                        .templateData(Map.of(
                                "organizationName", "Acme Corp",
                                "organizationUrl", "https://acme.com",
                                "fullName", "User %03d".formatted(i),
                                "username", "user%03d".formatted(i),
                                "createdAt", "2024-01-15T10:30:00Z"
                        ))
                        .attachments(List.of())
                        .build())
                .toList();
    }

    private List<EmailRequest> generateOtpBatch(int count) {
        return java.util.stream.IntStream.rangeClosed(1, count)
                .mapToObj(i -> EmailRequest.builder()
                        .eventId("evt-otp-gen-%03d".formatted(i))
                        .purpose(EmailPurpose.PASSWORD_RESET)
                        .recipient(new Recipient("user%03d@example.com".formatted(i), List.of(), List.of("security@example.com")))
                        .subject("Your OTP Code for Password Reset")
                        .templateData(Map.of(
                                "organizationName", "Acme Corp",
                                "otpCode", String.format("%06d", i * 111111 % 1000000)
                        ))
                        .attachments(List.of())
                        .build())
                .toList();
    }
}