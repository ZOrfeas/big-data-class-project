#!/bin/sh

JM_CONTAINER=$(docker ps --filter name=jobmanager --format={{.ID}})

docker exec $JM_CONTAINER flink run -d -c uni.processor.DataStreamJob /processor/processor.jar

cd producer  
# ./test_producer.py id='producer_1' days=$1 
./test_producer.py id='producer_1' days=$1 & ./test_producer.py id='producer_2' days=$1
