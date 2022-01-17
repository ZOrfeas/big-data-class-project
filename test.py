import redis 
import json
from datetime import datetime,timedelta
from time import sleep
import random
redis_client = redis.Redis(host='localhost',port=6379,db=0)


interval = 1
sample_data_count = 10
def sendData(interval,sample_data_count):
    print("SendingData")
    now = datetime.now().isoformat()
    for i in range(sample_data_count):
            
        date = (datetime.strptime(now, '%Y-%m-%dT%H:%M:%S.%f') + timedelta(days = i)).strftime("%x")
    
        x = {"date":date,
                "min":random.randint(10,25) ,
                "max":random.randint(50,75) ,
                "avg":random.randint(25,50) ,
                "sum":random.randint(200,250)}
        data = json.dumps(x)
        print(data)
        redis_client.set('date'+str(i),data)
        sleep(interval)


def getData(sample_data_count):
    print("Getting Data")
    for i in range(sample_data_count):
        
        print(redis_client.get('date'+str(i)))

def main():
        sendData(interval,sample_data_count)
        getData(sample_data_count)

if __name__ == '__main__':
    main()
