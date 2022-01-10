package uni.processor.input;

import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.KafkaSourceBuilder;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;
import org.apache.kafka.common.serialization.StringDeserializer;

public class Consumers {
    public static KafkaSource<String> createStringConsumer(String topic, String kafkaAddres, String kafkaGroup) {
        KafkaSourceBuilder<String> builder = KafkaSource.builder();
        builder.setBootstrapServers(kafkaAddres)
        .setGroupId(kafkaGroup)
        .setTopics(topic)
        .setStartingOffsets(OffsetsInitializer.earliest())
        .setDeserializer(KafkaRecordDeserializationSchema.valueOnly(StringDeserializer.class));
        return builder.build();
    }
    public static KafkaSource<InputMessage> createInputConsumer(String topic, String kafkaAddress, String kafkaGroup) {
        KafkaSourceBuilder<InputMessage> builder = KafkaSource.builder();
        builder.setBootstrapServers(kafkaAddress)
        .setGroupId(kafkaGroup)
        .setTopics(topic)
        .setStartingOffsets(OffsetsInitializer.earliest())
        .setValueOnlyDeserializer(new InputMessageDeserializationSchema());
        return builder.build();
    }
}
