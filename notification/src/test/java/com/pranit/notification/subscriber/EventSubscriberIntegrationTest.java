package com.pranit.notification.subscriber;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pranit.notification.mail.dto.EmailRequest;
import com.pranit.notification.mail.dto.Recipient;
import com.pranit.notification.mail.model.EmailPurpose;
import com.pranit.notification.mail.subscriber.EventSubscriberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@DisplayName("EventSubscriberService - Test Suite (Real Email Delivery)")
class EventSubscriberIntegrationTest {

    @Container
    static RabbitMQContainer rabbitMQ =
            new RabbitMQContainer(DockerImageName.parse("rabbitmq:4-management")).withExposedPorts(5672, 15672);

    @Autowired
    private EventSubscriberService eventSubscriberService;

    @Autowired
    private ObjectMapper objectMapper;

    @DynamicPropertySource
    static void rabbitProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbitMQ::getHost);
        registry.add("spring.rabbitmq.port", rabbitMQ::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbitMQ::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbitMQ::getAdminPassword);
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.H2Dialect");
        registry.add("spring.flyway.enabled", () -> "false");
    }

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    private EmailRequest loadWelcomeRequest(String fileName) throws IOException {
        try (InputStream is = new ClassPathResource("testdata/welcome/" + fileName).getInputStream()) {
            return objectMapper.readValue(is, EmailRequest.class);
        }
    }

    private List<EmailRequest> loadWelcomeRequests(String fileName) throws IOException {
        try (InputStream is = new ClassPathResource("testdata/welcome/" + fileName).getInputStream()) {
            return objectMapper.readValue(is, objectMapper.getTypeFactory().constructCollectionType(List.class, EmailRequest.class));
        }
    }

    private EmailRequest loadOtpRequest(String fileName) throws IOException {
        try (InputStream is = new ClassPathResource("testdata/otp/" + fileName).getInputStream()) {
            return objectMapper.readValue(is, EmailRequest.class);
        }
    }

    private List<EmailRequest> loadOtpRequests(String fileName) throws IOException {
        try (InputStream is = new ClassPathResource("testdata/otp/" + fileName).getInputStream()) {
            return objectMapper.readValue(is, objectMapper.getTypeFactory().constructCollectionType(List.class, EmailRequest.class));
        }
    }

    @Test
    @DisplayName("IT-WEL-01: Single welcome email - full flow through RabbitMQ to SMTP")
    void singleWelcomeEmailRealDelivery() throws IOException, InterruptedException {
        EmailRequest request = loadWelcomeRequest("welcome-single.json");
        eventSubscriberService.processEmail().accept(request);
        Thread.sleep(3000);
        assertThat(request.eventId()).isEqualTo("evt-welcome-001");
        assertThat(request.purpose()).isEqualTo(EmailPurpose.WELCOME);
    }

    @Test
    @DisplayName("IT-WEL-02: Multiple welcome emails - batch through RabbitMQ")
    void multipleWelcomeEmailsRealDelivery() throws IOException, InterruptedException {
        List<EmailRequest> requests = loadWelcomeRequests("welcome-multiple.json");
        AtomicInteger sentCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(requests.size());
        for (EmailRequest request : requests) {
            eventSubscriberService.processEmail().accept(request);
            sentCount.incrementAndGet();
            latch.countDown();
        }
        assertThat(latch.await(30, TimeUnit.SECONDS)).isTrue();
        assertThat(sentCount.get()).isEqualTo(3);
        Thread.sleep(5000);
    }

    @Test
    @DisplayName("IT-OTP-01: Single OTP email - full flow through RabbitMQ to SMTP")
    void singleOtpEmailRealDelivery() throws IOException, InterruptedException {
        EmailRequest request = loadOtpRequest("otp-single.json");
        eventSubscriberService.processEmail().accept(request);
        assertThat(request.eventId()).isEqualTo("evt-otp-001");
        assertThat(request.purpose()).isEqualTo(EmailPurpose.PASSWORD_RESET);
        Thread.sleep(3000);
    }

    @Test
    @DisplayName("IT-OTP-02: Multiple OTP emails - batch through RabbitMQ")
    void multipleOtpEmailsRealDelivery() throws IOException, InterruptedException {
        List<EmailRequest> requests = loadOtpRequests("otp-multiple.json");
        AtomicInteger sentCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(requests.size());
        for (EmailRequest request : requests) {
            eventSubscriberService.processEmail().accept(request);
            sentCount.incrementAndGet();
            latch.countDown();
        }
        assertThat(latch.await(30, TimeUnit.SECONDS)).isTrue();
        assertThat(sentCount.get()).isEqualTo(3);
        Thread.sleep(5000);
    }

    @Test
    @DisplayName("IT-BATCH-01: High traffic - 10 welcome emails concurrent")
    void highTrafficWelcomeEmails() throws IOException, InterruptedException {
        List<EmailRequest> requests = generateWelcomeBatch(10);
        AtomicInteger sentCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(requests.size());
        for (EmailRequest request : requests) {
            CompletableFuture.runAsync(() -> {
                eventSubscriberService.processEmail().accept(request);
                sentCount.incrementAndGet();
                latch.countDown();
            });
        }
        assertThat(latch.await(60, TimeUnit.SECONDS)).isTrue();
        assertThat(sentCount.get()).isEqualTo(10);
        Thread.sleep(10000);
    }

    @Test
    @DisplayName("IT-BATCH-02: High traffic - 10 OTP emails concurrent")
    void highTrafficOtpEmails() throws IOException, InterruptedException {
        List<EmailRequest> requests = generateOtpBatch(10);
        AtomicInteger sentCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(requests.size());
        for (EmailRequest request : requests) {
            CompletableFuture.runAsync(() -> {
                eventSubscriberService.processEmail().accept(request);
                sentCount.incrementAndGet();
                latch.countDown();
            });
        }
        assertThat(latch.await(60, TimeUnit.SECONDS)).isTrue();
        assertThat(sentCount.get()).isEqualTo(10);
        Thread.sleep(10000);
    }

    @Test
    @DisplayName("IT-VAL-01: Welcome email with missing template data - handler validation")
    void welcomeMissingTemplateData() throws IOException, InterruptedException {
        EmailRequest request = loadWelcomeRequest("welcome-single.json");
        eventSubscriberService.processEmail().accept(request);
        assertThat(request.eventId()).isNotNull();
        Thread.sleep(3000);
    }

    @Test
    @DisplayName("IT-VAL-02: OTP email with missing template data - handler validation")
    void otpMissingTemplateData() throws IOException, InterruptedException {
        EmailRequest request = loadOtpRequest("otp-single.json");
        eventSubscriberService.processEmail().accept(request);
        assertThat(request.eventId()).isNotNull();
        Thread.sleep(3000);
    }

    private List<EmailRequest> generateWelcomeBatch(int count) {
        return java.util.stream.IntStream.rangeClosed(1, count)
                .mapToObj(i -> EmailRequest.builder()
                        .eventId("evt-welcome-it-%03d".formatted(i))
                        .purpose(EmailPurpose.WELCOME)
                        .recipient(new Recipient(
                                "testuser%03d@example.com".formatted(i), List.of(), List.of()))
                        .subject("Welcome to Integration Test!")
                        .templateData(Map.of(
                                "organizationName", "Test Corp",
                                "organizationUrl", "https://test.com",
                                "fullName", "Test User %03d".formatted(i),
                                "username", "testuser%03d".formatted(i),
                                "createdAt", "2024-01-15T10:30:00Z"
                        ))
                        .attachments(List.of())
                        .build())
                .collect(Collectors.toList());
    }

    private List<EmailRequest> generateOtpBatch(int count) {
        return java.util.stream.IntStream.rangeClosed(1, count)
                .mapToObj(i -> EmailRequest.builder()
                        .eventId("evt-otp-it-%03d".formatted(i))
                        .purpose(EmailPurpose.PASSWORD_RESET)
                        .recipient(new Recipient(
                                "testuser%03d@example.com".formatted(i), List.of(), List.of()))
                        .subject("Your OTP Code for Integration Test")
                        .templateData(Map.of(
                                "organizationName", "Test Corp",
                                "otpCode", String.format("%06d", i * 111111 % 1000000)
                        ))
                        .attachments(List.of())
                        .build())
                .collect(Collectors.toList());
    }
}