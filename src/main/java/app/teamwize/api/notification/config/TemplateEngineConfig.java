package app.teamwize.api.notification.config;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.helper.ConditionalHelpers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TemplateEngineConfig {

    @Bean
    public Handlebars templateEngine() {
        return new Handlebars().registerHelpers(ConditionalHelpers.class);
    }

}
