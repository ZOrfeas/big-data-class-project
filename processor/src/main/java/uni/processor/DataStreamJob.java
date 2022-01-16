package uni.processor;

import com.redislabs.redistimeseries.RedisTimeSeries;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uni.processor.input.Consumers;
import uni.processor.input.InputMessage;
import uni.processor.output.OutputMessage;
import uni.processor.output.RedisSinkFunction;
import uni.processor.processing.RedisMapFunction;


public class DataStreamJob {

	private static final Logger logger = LoggerFactory.getLogger(DataStreamJob.class);
	
	private static final String KAFKA_TOPIC = "sensors";
	private static final String KAFKA_ADDRESS = "broker:29092";
	private static final String KAFKA_GROUP = "consumer-1";

	private static final String REDIS_ADDRESS = "redis";
	private static final int REDIS_PORT = 6379;
	
	public static void main(String[] args) throws Exception {
		// Sets up the execution environment, which is the main entry point
		// to building Flink applications.
		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

		DataStream<InputMessage> stream = env.fromSource(
			Consumers.createInputConsumer(KAFKA_TOPIC, KAFKA_ADDRESS, KAFKA_GROUP),
			WatermarkStrategy.noWatermarks(),
			"KafkaSource"
		);

		DataStream<OutputMessage> processedStream = stream.map(new RedisMapFunction());

		processedStream.addSink(new RedisSinkFunction(REDIS_ADDRESS, REDIS_PORT));

		// Execute program, begining computation.
		// stream.print();
		env.execute("Flink Streaming Java API Skeleton");
	}
}
