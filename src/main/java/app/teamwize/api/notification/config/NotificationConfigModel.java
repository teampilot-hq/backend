package app.teamwize.api.notification.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;


@ConfigurationProperties(prefix = "app.notification")
public record NotificationConfigModel(EmailConfigModel email) {
    public record EmailConfigModel(String from, String companyName, String unsubscribeUrl, Resource baseTemplate,
                                   String baseUrl) {
    }
}

