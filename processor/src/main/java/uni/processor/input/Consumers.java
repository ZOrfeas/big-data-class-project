package uni.processor.input;

import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;
import org.apache.kafka.common.serialization.StringDeserializer;

public class Consumers {
    public static KafkaSource<String> createStringConsumer(String topic, String kafkaAddres, String kafkaGroup) {
        return KafkaSource
            .<String>builder()
            .setBootstrapServers(kafkaAddres)
            .setGroupId(kafkaGroup)
            .setTopics(topic)
            .setStartingOffsets(OffsetsInitializer.earliest())
            .setDeserializer(KafkaRecordDeserializationSchema.valueOnly(StringDeserializer.class))
            .build();
    }
    public static KafkaSource<InputMessage> createInputConsumer(String topic, String kafkaAddress, String kafkaGroup) {
        return KafkaSource
            .<InputMessage>builder()
            .setBootstrapServers(kafkaAddress)
            .setGroupId(kafkaGroup)
            .setTopics(topic)
            .setStartingOffsets(OffsetsInitializer.earliest())
            .setValueOnlyDeserializer(new InputMessageDeserializationSchema())
            .build();
    }
}
