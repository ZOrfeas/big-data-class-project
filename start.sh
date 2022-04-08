#!/bin/sh

JM_CONTAINER=$(docker ps --filter name=jobmanager --format={{.ID}})

docker exec $JM_CONTAINER flink run -d -c uni.processor.DataStreamJob /processor/processor.jar

cd producer 
./producer.py id='producer_1' #&
# ./producer.py id='producer_2' 