package com.pranit.notification.mail.subscriber;

import com.pranit.notification.mail.router.EmailRouter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventSubscriberService {

    private final EmailRouter router;
}
