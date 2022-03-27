#!/usr/bin/env python

import websockets
import asyncio

async def test(websocket):
    while True:
        await websocket.Send("100")
        await asyncio.sleep(1)

async def main():
    async with websockets.serve(test, "localhost", 9091):
        await asyncio.Future()

asyncio.run(main())