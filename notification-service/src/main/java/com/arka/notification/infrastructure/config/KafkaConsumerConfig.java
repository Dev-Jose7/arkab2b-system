package com.arka.notification.infrastructure.config;

import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerConfig.class);

    @Bean
    public CommonErrorHandler notificationKafkaErrorHandler(
            KafkaTemplate<String, String> kafkaTemplate,
            @Value("${app.kafka.consumers.order-events.retry-backoff-ms:1000}") long retryBackoffMs,
            @Value("${app.kafka.consumers.order-events.max-retries:3}") long maxRetries,
            @Value("${app.kafka.consumers.order-events.dlq-suffix:.dlq}") String dlqSuffix) {

        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, exception) -> new TopicPartition(record.topic() + dlqSuffix, -1));

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                recoverer,
                new FixedBackOff(Math.max(0L, retryBackoffMs), Math.max(0L, maxRetries)));
        errorHandler.addNotRetryableExceptions(IllegalArgumentException.class);
        errorHandler.setRetryListeners((record, ex, deliveryAttempt) -> log.warn(
                "Retrying notification consumer record. topic={} partition={} offset={} attempt={} error={}",
                record.topic(),
                record.partition(),
                record.offset(),
                deliveryAttempt,
                ex.toString(),
                ex));
        errorHandler.setCommitRecovered(true);
        return errorHandler;
    }

    @Bean("notificationKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<Object, Object> notificationKafkaListenerContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            ConsumerFactory<Object, Object> consumerFactory,
            CommonErrorHandler notificationKafkaErrorHandler) {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        configurer.configure(factory, consumerFactory);
        factory.setCommonErrorHandler(notificationKafkaErrorHandler);
        return factory;
    }
}
