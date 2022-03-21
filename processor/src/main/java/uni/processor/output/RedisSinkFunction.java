package uni.processor.output;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import com.redislabs.redistimeseries.Aggregation;
import com.redislabs.redistimeseries.Measurement;
import com.redislabs.redistimeseries.RedisTimeSeries;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Pair<T, E> {
    public T left;
    public E right;
    public Pair(T left, E right) {
        this.left = left;
        this.right = right;
    }
    
}

class AggregationState {
    private Queue<Pair<Date, Float>> queue;
    private Date oldestValDate;
    private Pair<Date, Float> min, max;
    public AggregationState() {
        this.queue = new LinkedList<Pair<Date, Float>>();
        this.oldestValDate = null;
    }
    public void push(Pair<Date, Float> pair) {
        if (this.oldestValDate == null || this.queue.size() == 0) {
            this.oldestValDate = pair.left;
        }
        while ((pair.left.getTime() - this.oldestValDate.getTime()) > 24*60*60*1000) {
            Pair<Date, Float> removed = this.queue.remove();
            if (min == removed) {
                min = null;
            }
            if (max == removed) {
                max = null;
            }
            this.oldestValDate = this.queue.peek().left;
        }
        if (this.min == null || pair.right < this.min.right) {
            this.min = pair;
        }
        if (this.max == null || pair.right > this.max.right) {
            this.max = pair;
        }
        this.queue.add(pair);
    }
    public Float getSum() {
        if (this.queue.size() == 0) {
            return null;
        }
        Float sum = 0f;
        for (Pair<Date, Float> p : this.queue) {
            sum += p.right;
        }
        return sum;
    }
    public Float getAvg() {
        if (this.queue.size() == 0) {
            return null;
        }
        return getSum() / this.queue.size();
    }
    public Float getMin() {
        if (this.min == null) {
            return null;
        }
        return this.min.right;
    }
    public Float getMax() {
        if (this.max == null) {
            return null;
        }
        return this.max.right;
    }
    @Override
    public String toString() {
        return "{size=" + queue.size() + "oldest=" + oldestValDate + ", min=" + getMin() + ", max=" + getMax() + ", avg=" + getAvg() + ", sum=" + getSum() + "}";
    }
}

public class RedisSinkFunction extends RichSinkFunction<OutputMessage> {
    
    private static final Logger logger = LoggerFactory.getLogger(RedisSinkFunction.class);
    private HashSet<String> metIds; // ids of producers that have already been encountered
    private RedisTimeSeries rts; // https://github.com/RedisTimeSeries/JRedisTimeSeries
    private AggregationState state;
    
    @Override
    public void open(Configuration parameters) throws Exception {
        String ip = "redis";
        int port = 6379;
        logger.info("Creating RedisSinkFunction with ip={} and port={}", ip, port);
        metIds = new HashSet<>();
        rts = new RedisTimeSeries(ip, port);
        state = new AggregationState();
    }

    private boolean rtsCreateAndLog(String id) {
        logger.info("Creating time-series with id: {}", id);
        try {
            Map<String, String> labels = new HashMap<>();
            labels.put("producer", id);
            rts.create(id, labels);
            logger.info("Created {} key successfuly", id);
            return true;
        } catch (Exception e) {
            logger.error("Failed to create {}", id, e);
        }
        return false;
    }

    private boolean rtsCreateRuleAndLog(String sourceKey, String destKey, Aggregation agg, long timeBucket) {
        logger.info("Creating rule from {} to {} with aggregation {} and timeBucket {}", sourceKey, destKey, agg.toString(), timeBucket);
        boolean created = false;
        try {
            created = rts.createRule(sourceKey, agg, timeBucket, destKey);
        } catch (Exception e) {
            logger.error("Failed to create rule from {} to {} with aggregation {} and timeBucket {}", sourceKey, destKey, agg.toString(), timeBucket, e);
        }
        if (created) {
            logger.info("Created {} from {} to {} with bucketsize {}", agg.toString(), sourceKey, destKey, timeBucket);
        } else {
            logger.info("Failed to create {} from {} to {} with bucketsize {}", agg.toString(), sourceKey, destKey, timeBucket);
        }
        return created;
    }
    
    private static final long bucketSize = 1000 * 60 * 60 * 24; // 1 day
    @Override
    public void invoke(OutputMessage value, Context context) throws Exception {
        logger.info("Sending OutputMessage {} to Redis...", value.toString());
        ArrayList<Measurement> measurements = new ArrayList<>();
        if (!metIds.contains(value.msg.id)) {
            logger.info("New producer id encountered {}", value.msg.id);
            logger.info("Creating all necessary keys...");
            // create time-series for this producer, create keys for aggregation rules
            rtsCreateAndLog(value.msg.id);
            rtsCreateAndLog(value.msg.id + ":late");
            rtsCreateAndLog(value.msg.id + ":min");
            rtsCreateAndLog(value.msg.id + ":max");
            rtsCreateAndLog(value.msg.id + ":avg");
            rtsCreateAndLog(value.msg.id + ":sum");

            rtsCreateAndLog("flink:" + value.msg.id);
            rtsCreateAndLog("flink:" + value.msg.id + ":min");
            rtsCreateAndLog("flink:" + value.msg.id + ":max");
            rtsCreateAndLog("flink:" + value.msg.id + ":avg");
            rtsCreateAndLog("flink:" + value.msg.id + ":sum");

            logger.info("Creating aggregation rules...");
            // create aggregation rules
            // !!! BE AWARE OF THE TIME BUCKET SIZE, MAY BE MILLISECONDS OR SECONDS, NOT SURE
            rtsCreateRuleAndLog(value.msg.id, value.msg.id + ":min", Aggregation.MIN, bucketSize);
            rtsCreateRuleAndLog(value.msg.id, value.msg.id + ":max", Aggregation.MAX, bucketSize);
            rtsCreateRuleAndLog(value.msg.id, value.msg.id + ":avg", Aggregation.AVG, bucketSize);
            rtsCreateRuleAndLog(value.msg.id, value.msg.id + ":sum", Aggregation.SUM, bucketSize);
            metIds.add(value.msg.id);
        }
        if (value.isLateEvent()) {
            logger.info("Late event found, processing...");
            measurements.add(new Measurement(
                value.msg.id + ":late",
                value.msg.createdAt.getTime(),
                (double)(value.msg.createdAt.getTime() - value.msg.sampledAt.getTime()) // delay in milliseconds
            ));
        } else {
            logger.info("Adding basic measurement to to-send list...");
            measurements.add(new Measurement(
                value.msg.id,
                value.msg.sampledAt.getTime(),
                value.msg.value
            ));
            state.push(new Pair<Date, Float>(value.msg.sampledAt, value.msg.value));
            logger.info("Local aggregation state: {}", state.toString());
            measurements.add(new Measurement("flink:"+value.msg.id+":min", value.msg.sampledAt.getTime(), state.getMin()));
            measurements.add(new Measurement("flink:"+value.msg.id+":max", value.msg.sampledAt.getTime(), state.getMax()));
            measurements.add(new Measurement("flink:"+value.msg.id+":avg", value.msg.sampledAt.getTime(), state.getAvg()));
            measurements.add(new Measurement("flink:"+value.msg.id+":sum", value.msg.sampledAt.getTime(), state.getSum()));
        }
        logger.info("Sending measurements to Redis...");
        rts.madd(measurements.toArray(new Measurement[0])); // syntax magic because .madd is variadic`
    }
}
