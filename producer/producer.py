#!/usr/bin/env python
from time import sleep
from kafka import KafkaProducer
import json
from datetime import datetime, timedelta
import sys
import random

# random.seed(100)

id='producer_1'
server='localhost:9092'
topic='sensors'
interval=5 # seconds
duration=15 # mins
days=0

def valueGen(filepath):
    lines = open(filepath, 'r').readlines()
    for line in lines:
        if line.startswith("MinTemp"): continue
        yield float(line.strip())

def formatData(id, created_at, sampled_at, value):
    return {
        'id': id,
        'created_at': created_at,
        'sampled_at': sampled_at,
        'value': value
    }

def dataRealTimeGen(id, interval, duration, valueGen):
    sampleCount = int((duration*60)/interval)
    lateEventCount = int(sampleCount/30)
    print(f'Generating {sampleCount} samples for realtime')
    counter=0
    for _ in range(sampleCount):
        now = (datetime.now() - timedelta(days=2))
        nowString = now.isoformat()
        sampledAt = nowString
        if lateEventCount != 0 and (counter == 30 or counter+random.randint(1, 4) == 30):
            counter = 0
            sampledAt = (now - timedelta(minutes=random.randint(10,20))).isoformat()
            lateEventCount -= 1
        counter += 1
        datum = formatData(id, nowString, sampledAt, next(valueGen))
        yield datum

def dataBurstGen(id, interval, days, valueGen):
    sampleCount = int(days*24*60*60/interval)
    lateEventCount = int(sampleCount/30)
    counter=0
    now = datetime.now() - timedelta(days=days+2)
    print(f'Generating {sampleCount} samples for burst')
    for _ in range(sampleCount):
        nowString = now.isoformat()
        sampledAt = nowString
        if lateEventCount != 0 and (counter == 30 or counter+random.randint(1, 4) == 30):
            counter = 0
            sampledAt = (now - timedelta(minutes=random.randint(10,20))).isoformat()
            lateEventCount -= 1
        counter += 1
        datum = formatData(id, nowString, sampledAt, next(valueGen))
        now = now + timedelta(seconds=interval)
        yield datum
    
serializer = lambda v: json.dumps(v).encode('ascii')

def main(id, server, topic, interval, duration, days):
    print(f'Creating producer {id} on {server}')
    producer = KafkaProducer(
        client_id=f'{id}',
        bootstrap_servers=[server],
        value_serializer=serializer,
        api_version=(0, 10, 1)
    )
    print(f'Producer {id} created')
    valueGenInst = valueGen('temperature.csv')
    burstData = dataBurstGen(id, interval, days, valueGenInst)
    realTimeData = dataRealTimeGen(id, interval, duration, valueGenInst)
    print(f'Sending burst of {days} days')
    for data in burstData:
        producer.send(topic, data)

    print(f'Sending realtime samples')
    for data in realTimeData:
        producer.send(topic, data)
        sleep(interval)
    print(f'Producer {id} finished')

if __name__ == '__main__':
    if len(sys.argv) > 1 and sys.argv[1] == 'help':
        print(f'Usage: {sys.argv[0]} [id=<id>] [server=<server>] [topic=<topic>] [interval=<interval>] [duration=<duration>] [days=<days>]')
        print(f'Default values: id={id}, server={server}, topic={topic}, interval={interval}, duration={duration} days={days}')
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
        elif arg.startswith('days='):
            days = int(arg[5:])
        else:
            print(f'Unknown argument: {arg}')
            sys.exit(1)
    main(id, server, topic, interval, duration, days)
