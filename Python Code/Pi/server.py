import asyncio
import websockets

async def echo(websocket, path):
    print("Client Connected")
    try:
        async for message in websocket:
            print("Pong" + message)
            if(message = "Start Dryer"):
                
            await websocket.send("Parrot: " + message)
    except websockets.exceptions.ConnectionClosed as e:
        print("A client just disconnected")

start_server = websockets.serve(echo, "192.168.0.23", 8765)

asyncio.get_event_loop().run_until_complete(start_server)
asyncio.get_event_loop().run_forever()