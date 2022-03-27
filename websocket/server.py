#!/usr/bin/env python

import websockets
import asyncio

async def test(websocket):
    while True:
        print("ok")
        await websocket.send('{"result":{"data":{"timestamp":"2022-01-12T23:29:22.4268526802", "deviceId":"61d34aceea77dbd14986344a","data":{"temp":24.5}}}}')
        await asyncio.sleep(1)

async def main():
    async with websockets.serve(test, "localhost", 9091):
        await asyncio.Future()

asyncio.run(main())