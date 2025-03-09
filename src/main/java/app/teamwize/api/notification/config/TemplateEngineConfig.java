package app.teamwize.api.notification.config;

import com.github.jknack.handlebars.Handlebars;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TemplateEngineConfig {

    @Bean
    public Handlebars templateEngine() {
        return new Handlebars();
    }

}
