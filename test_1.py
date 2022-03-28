#!/usr/bin/env python

import asyncio
import websockets

async def test(websocket):
    while True:
        print("ok")
        await websocket.send('{"result":{"data":{"timestamp":"2022-01-12T23:29:22.4268526802", "deviceId":"61d34aceea77dbd14986344a","data":{"temp":24.5}}}}')
        await asyncio.sleep(1)

# create handler for each connection

async def handler(websocket, path):
    data = await websocket.recv()
    reply = f"Data recieved as:  {data}!"
    await websocket.send(reply)
 
# start_server = websockets.serve(handler, "localhost", 9091)

# asyncio.get_event_loop().run_until_complete(start_server)
async def main():
    async with websockets.serve(test, "localhost", 3000):
        await asyncio.Future()

asyncio.run(main())
        
asyncio.get_event_loop().run_forever()