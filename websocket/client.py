#!/usr/bin/env python

from websocket import create_connection

ws = create_connection("ws://localhost:9091/")
print(ws.recv())
print("Sending 'Hello, World'...")
ws.send("Hello, World")
print("Sent")
print("Receiving...")
result =  ws.recv()
print("Received '%s'" % result)
ws.close()