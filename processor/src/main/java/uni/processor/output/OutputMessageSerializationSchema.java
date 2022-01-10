package uni.processor.output;

import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OutputMessageSerializationSchema<T> implements SerializationSchema<T> {
    
    private static final Logger logger = LoggerFactory.getLogger(OutputMessageSerializationSchema.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public byte[] serialize(T message) {
        try {
            return mapper.writeValueAsString(message).getBytes();
        } catch (Exception e) {
            logger.error(e.getMessage());
            System.exit(1);
            return null;
        }
    }
}
