package com.ev12.config.api;

import com.ev12.config.model.InboundMessage;
import com.ev12.config.service.InboundMessageStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class InboundSmsController {
    private final InboundMessageStore messageStore;

    public InboundSmsController(InboundMessageStore messageStore) {
        this.messageStore = messageStore;
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
    public ResponseEntity<List<InboundMessage>> listInbound() {
        return ResponseEntity.ok(messageStore.getMessages());
    }
}
