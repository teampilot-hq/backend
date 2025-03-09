package app.teamwize.api.notification.service.notifier;

import app.teamwize.api.notification.config.NotificationConfigModel;
import app.teamwize.api.notification.exception.NotificationSendFailureException;
import app.teamwize.api.notification.model.NotificationChannel;
import app.teamwize.api.notification.model.event.NotificationEventPayload;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.io.StringTemplateSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.HashMap;


@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotifier implements Notifier {

    private final JavaMailSender mailSender;
    private final Handlebars templateEngine;
    private final NotificationConfigModel config;

    @Override
    public boolean accepts(NotificationChannel channel) {
        return channel == NotificationChannel.EMAIL;
    }

    @Override
    public void notify(NotificationEventPayload event) throws NotificationSendFailureException {
        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(event.user().email());
            helper.setSubject(event.title());

            var unsubscribeUrl = config.email().unsubscribeUrl() == null ? null : config.email().unsubscribeUrl() + "?email=" + event.user().email();

            var context = new HashMap<String, Object>();
            context.put("title", event.title());
            context.put("body", event.htmlContent());
            context.put("year", LocalDate.now().getYear());
            context.put("unsubscribeUrl", unsubscribeUrl);
            context.put("companyName", config.email().companyName());
            context.put("baseUrl", config.email().baseUrl());

            var baseTemplateRawHTML = config.email().baseTemplate().getContentAsString(Charset.defaultCharset());
            var html = templateEngine.compile(new StringTemplateSource("email", baseTemplateRawHTML)).apply(context);

            helper.setText(html, true); // Set to true for HTML

            mailSender.send(message);
            log.info("Email sent to {}", event.user().email());
        } catch (Exception e) {
            throw new NotificationSendFailureException("Failed to send email notification: " + e.getMessage());
        }
    }
}
