package uni.processor.output;

import org.apache.flink.api.common.functions.MapFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uni.processor.input.InputMessage;

public class RedisMapFunction implements MapFunction<InputMessage,  OutputMessage> {

    private static final Logger logger = LoggerFactory.getLogger(RedisMapFunction.class);

    @Override
    public OutputMessage map(InputMessage value) throws Exception {
        // placeholder for now, logic can be added here or in the OutputMessage constructor
        logger.info("Processing input message: {}", value.toString());
        return new OutputMessage(value);
    }
}   
