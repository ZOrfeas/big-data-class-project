package uni.processor.processing;

import org.apache.flink.api.common.functions.MapFunction;

import uni.processor.input.InputMessage;
import uni.processor.output.OutputMessage;

public class RedisMapFunction implements MapFunction<InputMessage,  OutputMessage> {

    @Override
    public OutputMessage map(InputMessage value) throws Exception {
        // TODO Implement serialization from string to form that is acceptable by redis timeseries
        return null;
    }
}   
