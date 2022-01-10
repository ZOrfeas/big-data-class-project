package uni.processor.output;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.sink.KafkaSinkBuilder;

public class Producers {
    public static KafkaSink<String> createStringProducer(String topic, String kafkaAddress) {
        KafkaSinkBuilder<String> builder = KafkaSink.builder();
        builder.setBootstrapServers(kafkaAddress)
        .setRecordSerializer(KafkaRecordSerializationSchema.builder()
            .setTopic(topic)
            .setValueSerializationSchema(new SimpleStringSchema())
            .build()
        );
        return builder.build();
    }

    public static <T> KafkaSink<T> createOutputProducer(String topic, String kafkaAddress) {
        KafkaSinkBuilder<T> builder = KafkaSink.builder();
        builder.setBootstrapServers(kafkaAddress)
        .setRecordSerializer(KafkaRecordSerializationSchema.builder()
            .setTopic(topic)
            .setValueSerializationSchema(new OutputMessageSerializationSchema<T>())
            .build()
        );
        return builder.build();
    }
}
