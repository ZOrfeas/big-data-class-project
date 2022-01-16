package uni.processor.output;

import com.redislabs.redistimeseries.RedisTimeSeries;

import org.apache.flink.streaming.api.functions.sink.SinkFunction;

public class RedisSinkFunction implements SinkFunction<OutputMessage> {
    
    // https://github.com/RedisTimeSeries/JRedisTimeSeries
    private final RedisTimeSeries rts;

    public RedisSinkFunction(String ip, int port) {
        super();
        rts = new RedisTimeSeries(ip, port);
    }

    @Override
    public void invoke(OutputMessage value, Context context) throws Exception {
        // TODO Implement sending from Flink to Redis Timeseries
    }
}
