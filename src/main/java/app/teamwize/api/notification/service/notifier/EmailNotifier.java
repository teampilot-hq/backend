package app.teamwize.api.notification.service.notifier;

import app.teamwize.api.notification.exception.NotificationSendFailureException;
import app.teamwize.api.notification.model.NotificationChannel;
import app.teamwize.api.notification.model.event.NotificationEventPayload;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.io.StringTemplateSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.Map;


@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotifier implements Notifier {

    private final JavaMailSender mailSender;
    private final Handlebars templateEngine;
    @Value("classpath:templates/email/layout.hbs")
    private Resource emailTemplate;

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

            var context = Map.of(
                    "title", event.id(),
                    "body", event.htmlContent(),
                    "year", LocalDate.now().getYear(),
                    "unsubscribeUrl", "https://teamwize.app/unsubscribe?email=" + event.user().email(),
                    "companyName", "TeamPilot"
            );

            var html = templateEngine.compile(new StringTemplateSource("email", emailTemplate.getContentAsString(Charset.defaultCharset()))).apply(context);

            helper.setText(html, true); // Set to true for HTML

            mailSender.send(message);
            log.info("Email sent to {}", event.user().email());
        } catch (Exception e) {
            throw new NotificationSendFailureException("Failed to send email notification: " + e.getMessage());
        }
    }
}
