package uni.processor.input;

import java.nio.charset.StandardCharsets;

import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InputMessageDeserializationSchema implements DeserializationSchema<InputMessage> {
    
    private static final Logger logger = LoggerFactory.getLogger(InputMessageDeserializationSchema.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public InputMessage deserialize(byte[] message) {
        String jsonStr = new String(message, StandardCharsets.US_ASCII);
        try {
            return mapper.readValue(jsonStr, InputMessage.class);
        } catch (Exception e) {
            logger.error(e.getMessage());
            System.exit(1);
            return null;
        }
    }

    @Override
    public boolean isEndOfStream(InputMessage nextElement) {
        return false;
    }

    @Override
    public TypeInformation<InputMessage> getProducedType() {
        return TypeInformation.of(InputMessage.class);
    }
}
