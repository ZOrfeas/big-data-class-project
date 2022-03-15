#!/usr/bin/env python
from time import sleep
from kafka import KafkaProducer
import json
from datetime import datetime, timedelta
import sys
import random

random.seed(100)

id='producer_1'
server='localhost:9092'
topic='sensors'
interval=1 # seconds
duration=15 # mins

def formatData(id, interval, duration, ):
    sampleCount = int(duration*60/interval)
    lateEventCount = int(sampleCount/30)
    print(f'Generating {sampleCount} samples')
    counter=0
    for _ in range(sampleCount):
        now = (datetime.now() - timedelta(days=1)).isoformat()
        sampledAt = now
        if lateEventCount != 0 and (counter == 30 or counter+random.randint(1, 4) == 30):
            counter = 0
            sampledAt = (datetime.strptime(now, '%Y-%m-%dT%H:%M:%S.%f') - timedelta(minutes=random.randint(10,20))).isoformat()
            lateEventCount -= 1
        counter += 1
        yield {
            'id': id,
            'created_at': now,
            'sampled_at': sampledAt,
            'value': random.random()*10
        }

serializer = lambda v: json.dumps(v).encode('ascii')

def main(id, server, topic, interval, duration):
    print(f'Creating producer {id} on {server}')
    producer = KafkaProducer(
        client_id=f'{id}',
        bootstrap_servers=[server],
        value_serializer=serializer,
        api_version=(0, 10, 1)
    )
    print(f'Producer {id} created')
    readyToSendData = formatData(id, interval, duration)
    print(f'Sending samples')
    # for data in readyToSendData:
    #     print(data)
    #     break

    for data in readyToSendData:
        producer.send(topic, data)
        sleep(interval)
    print(f'Producer {id} finished')

if __name__ == '__main__':
    if len(sys.argv) > 1 and sys.argv[1] == 'help':
        print(f'Usage: {sys.argv[0]} [id=<id>] [server=<server>] [topic=<topic>] [interval=<interval>] [duration=<duration>]')
        print(f'Default values: id={id}, server={server}, topic={topic}, interval={interval}, duration={duration}')
        sys.exit(0)
    for arg in sys.argv[1:]:
        if arg.startswith('id='):
            id = arg[3:]
        elif arg.startswith('server='):
            server = arg[7:]
        elif arg.startswith('topic='):
            topic = arg[6:]
        elif arg.startswith('interval='):
            interval = int(arg[9:])
        elif arg.startswith('duration='):
            duration = int(arg[9:])
        else:
            print(f'Unknown argument: {arg}')
            sys.exit(1)

    main(id, server, topic, interval, duration)
