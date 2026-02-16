package com.ev12.config.api;

import com.ev12.config.model.InboundMessage;
import com.ev12.config.service.InboundMessageStore;
import com.ev12.config.service.LocalInboundMessageClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class InboundSmsController {
    private final InboundMessageStore messageStore;
    private final LocalInboundMessageClient localInboundMessageClient;

    public InboundSmsController(InboundMessageStore messageStore, LocalInboundMessageClient localInboundMessageClient) {
        this.messageStore = messageStore;
        this.localInboundMessageClient = localInboundMessageClient;
    }

    @PostMapping("/inbound-sms")
    public ResponseEntity<String> inboundSms(
        @RequestParam(value = "msisdn", required = false) String msisdn,
        @RequestParam(value = "from", required = false) String from,
        @RequestParam(value = "text", required = false) String text
    ) {
        String sender = msisdn != null ? msisdn : from;
        messageStore.addMessage(sender, text != null ? text : "");
        return ResponseEntity.ok("ok");
    }

    @GetMapping("/inbound-messages")
    public ResponseEntity<List<InboundMessage>> listInbound(
        @RequestParam(value = "since", required = false) Long since,
        @RequestParam(value = "limit", required = false, defaultValue = "100") Integer limit,
        @RequestParam(value = "phone", required = false) String phone
    ) {
        List<InboundMessage> sourceMessages = fetchSourceMessages();
        List<InboundMessage> filtered = sourceMessages.stream()
            .filter(message -> matchesSince(message, since))
            .filter(message -> matchesPhone(message, phone))
            .sorted((left, right) -> left.getReceivedAt().compareTo(right.getReceivedAt()))
            .limit(Math.max(1, Math.min(limit, 500)))
            .toList();

        return ResponseEntity.ok(filtered);
    }

    private List<InboundMessage> fetchSourceMessages() {
        String receiveUrl = System.getenv("LOCAL_SMS_RECEIVE_URL");
        String authorization = System.getenv("LOCAL_SMS_AUTHORIZATION");
        if (receiveUrl == null || receiveUrl.isBlank()) {
            return messageStore.getMessages();
        }

        try {
            return localInboundMessageClient.fetchMessages(receiveUrl, authorization);
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(
                BAD_GATEWAY,
                "Failed to poll local receive endpoint. Verify LOCAL_SMS_RECEIVE_URL and auth settings. " + ex.getMessage(),
                ex
            );
        }
    }

    private boolean matchesSince(InboundMessage message, Long since) {
        if (since == null) {
            return true;
        }
        Instant marker = since > 9_999_999_999L ? Instant.ofEpochMilli(since) : Instant.ofEpochSecond(since);
        return message.getReceivedAt().isAfter(marker);
    }

    private boolean matchesPhone(InboundMessage message, String phone) {
        if (phone == null || phone.isBlank()) {
            return true;
        }
        return normalizePhone(message.getFrom()).equals(normalizePhone(phone));
    }

    private String normalizePhone(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("[^+0-9]", "");
    }
}
