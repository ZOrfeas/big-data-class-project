#!/usr/bin/env python

import math
import time
import asyncio
import websockets

token="eyJrIjoidmo1TGNmcUducmI5TVNDMHc4dUxaNUo2U2ZQQmdLMzEiLCJuIjoia2V5IiwiaWQiOjF9"
token="Bearer " + token

async def stream():
    async with websockets.connect("ws://localhost:3000/api/live/push/sinewave_test") as websocket:
        i = 0
        while True:
            await websocket.send("sin,val=" + str((math.sin(math.radians(i))+1)*20+80) +" " + str(time.time_ns()+1000000000))
            if (i == 361):
                i = 0
            else:
                i = i + 15
            await asyncio.sleep(0.03)

asyncio.run(stream())
