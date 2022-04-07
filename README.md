# Streaming data processing and visualization with Kafka-Flink-Redistimeseries-Grafana

## Dependencies
- Docker
- python3 (kafka-python, redis-py)
- docker/producer/producer.jar (flink job)
- producer/producer.py (python sensor emulation)

## Steps to setup
From project root: `docker compose -f docker/full-app.yml up -d`   
(and optionally `docker compose -f docker/full-app.yml logs -f` to monitor system status)  
and wait a sec before running `./start.sh`  
(if any permissions issue arrises with the .jar, download manually and adjust).  
Open a browser at localhost:3000, generate an api key from Grafana and paste it the last line of `docker/telegraf/mytele.conf`  
and startup telegraf by running `docker compose -f docker/telegraf.yml up -d`.  
If all has gone well the streaming simulation should be ready and fully featured.

## Optional features
- `./producer/producer.py` can be invoked multiple times (with different sensor id each time) to simulate multiple sensors
- `./producer/producer.py` can be parametrized based on data interval, duration and others

## Todo's
- [ ] package python faux-sensors in a dockerfile
- [ ] package flink job jar in a dockerfile with flink (workaround = .jar is checked into github)
- [ ] (low-prio) customize grafana docker image to startup with a preset dashboard
- [ ] (possibly not possible :) ) check if grafana image can have a preset api key to facilitate single step startup (without a hard dependency between grafana and telegraf)

#### Authors
- [Orfeas Zografos(03117160)](https://github.com/ZOrfeas)
- [George Pagonis(03117030)](https://github.com/GeorgePag4028)
