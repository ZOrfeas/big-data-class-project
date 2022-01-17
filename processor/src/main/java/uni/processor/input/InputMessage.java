package uni.processor.input;

import java.util.Date;

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

public class InputMessage {
    @JsonProperty("id")
        public String id;
    @JsonProperty("created_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
        public Date createdAt;
    @JsonProperty("sampled_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
        public Date sampledAt;
    @JsonProperty("value")
        public Float value;

    @Override
    public String toString() {
        return "id: " + id + 
               ", createdAt: " + createdAt.toString() + 
               ", sampledAt: " + sampledAt.toString() + 
               ", value: " + value;
    }
}
