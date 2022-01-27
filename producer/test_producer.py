#!/usr/bin/env python
from time import sleep
from kafka import KafkaProducer
import json
from datetime import datetime, timedelta
import sys
import random
import pandas as pd


random.seed(100)

id='producer_1'
server='localhost:9092'
topic='sensors'
interval=1 # seconds
duration=1 # mins
days = 1 # days of data

def formatData(id, interval, duration, data):
    sampleCount = 96 # one every 15 minutes
    lateEventCount = int(sampleCount/30)
    print(f'Generating {sampleCount*days} samples')

    counter=0
    date = datetime.now().date().isoformat()+'T00:00:00'
    for numDays in range(1,days+1):
        for minutes in range(sampleCount):
            now = (datetime.strptime(date,'%Y-%m-%dT%H:%M:%S') - timedelta(days=numDays) + timedelta(minutes=minutes*15)).isoformat()
            sampledAt = now
            if lateEventCount != 0 and (counter == 30 or counter+random.randint(1, 4) == 30):
                print("lateEventCount")
                counter = 0
                sampledAt = (datetime.strptime(now, '%Y-%m-%dT%H:%M:%S') - timedelta(minutes=random.randint(10,20))).isoformat()
                lateEventCount -= 1
            counter += 1
            yield {
                'id': id,
                'created_at': now,
                'sampled_at': sampledAt,
                'value': data[numDays*96 + minutes][0]
            }

serializer = lambda v: json.dumps(v).encode('ascii')

def main(id, server, topic, interval, duration, data):
    print(f'Creating producer {id} on {server}')
    # producer = KafkaProducer(
    #     client_id=f'{id}',
    #     bootstrap_servers=[server],
    #     value_serializer=serializer,
    #     api_version=(0, 10, 1)
    # )
    print(f'Producer {id} created')
    readyToSendData = formatData(id, interval, duration,data)
    print(f'Sending samples')
    for dat in readyToSendData:
        print(dat)
    # for data in readyToSendData:
    #     producer.send(topic, data)
    #     sleep(interval)
    print(f'Producer {id} finished')

if __name__ == '__main__':
    if len(sys.argv) > 1 and sys.argv[1] == 'help':
        print(f'Usage: {sys.argv[0]} [id=<id>] [server=<server>] [topic=<topic>] [interval=<interval>] [duration=<duration>] [days=<days>]')
        print(f'Default values: id={id}, server={server}, topic={topic}, interval={interval}, duration={duration}, days={days}')
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

    data = pd.read_csv('temperature.csv').to_numpy()
    main(id, server, topic, interval, duration,data)
