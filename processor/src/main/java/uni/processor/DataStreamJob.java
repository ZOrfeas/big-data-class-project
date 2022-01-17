package uni.processor;


import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uni.processor.input.Consumers;
import uni.processor.input.InputMessage;
import uni.processor.output.OutputMessage;
import uni.processor.output.RedisMapFunction;
import uni.processor.output.RedisSinkFunction;


public class DataStreamJob {

	private static final Logger logger = LoggerFactory.getLogger(DataStreamJob.class);
	
	private static final String KAFKA_TOPIC = "sensors";
	private static final String KAFKA_ADDRESS = "broker:29092";
	private static final String KAFKA_GROUP = "consumer-1";

	// private static final String REDIS_ADDRESS = "redis";
	// private static final int REDIS_PORT = 6379;
	
	public static void main(String[] args) throws Exception {
		logger.info("Starting DataStreamJob");
		logger.info("Getting StreamExecutionEnvironment");

		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

		logger.info("Setting up Kafka Consumer");
		DataStream<InputMessage> stream = env.fromSource(
			Consumers.createInputConsumer(KAFKA_TOPIC, KAFKA_ADDRESS, KAFKA_GROUP),
			WatermarkStrategy.noWatermarks(),
			"KafkaSource"
		);

		logger.info("Setting up RedisMapFunction from InputMessage to OutputMessage");
		DataStream<OutputMessage> processedStream = stream.map(new RedisMapFunction());

		logger.info("Setting up RedisSinkFunction");
		processedStream.addSink(new RedisSinkFunction());

		// Execute program, begining computation.
		env.execute("Flink Streaming Java API Skeleton");
	}
}
