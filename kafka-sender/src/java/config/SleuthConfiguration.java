package java.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.sleuth.zipkin2.ZipkinAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import zipkin2.Span;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.Reporter;

import java.sender.KafkaSender;

@Configuration
@EnableConfigurationProperties(KafkaProperties.class)
public class SleuthConfiguration {

    @Bean(ZipkinAutoConfiguration.REPORTER_BEAN_NAME)
    Reporter<Span> myReporter(KafkaProperties properties) {
        return AsyncReporter.create(sender(properties));
    }

    @Bean(ZipkinAutoConfiguration.SENDER_BEAN_NAME)
    KafkaSender sender(KafkaProperties properties) {
        return new KafkaSender(properties);
    }
}
