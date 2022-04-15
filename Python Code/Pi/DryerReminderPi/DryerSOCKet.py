""" Websocket API that will parse the messages to JSON and run the
dryer detection process from there. The connection is made by
knowing the ipaddress and the port to connect to. No Security was setup for this API
as it will likely be removed from the project in the future.

Author: Michael Mohler
Data: 03/28/2022
Version: 1
"""
import logging
import logging.handlers as handlers
import json
import asyncio
import websockets
import AxesModel
import DryerService

IPADDRESS = '192.168.0.23'
PORT = 9607

#Logging Configure
logging.basicConfig(filename='dryer.log',
    format='%(asctime)s-%(levelname)s-%(message)s', level=logging.DEBUG)
logger = logging.getLogger('DryerAPI')
logHandler = handlers.RotatingFileHandler('dryer.log', maxBytes=1000000, backupCount=1)
logger.addHandler(logHandler)


#Log that the socket server has started
logging.debug("Start - Dryer Socket")
print("Start - Dryer Socket")

async def dryer(websocket, path):
    """When a connection to a client is made and it recieves a message, parse the string to a json
    and check if the process says start. If so run the dryer detection process.

    Arg: websocket - Websocket Protocall
    Arg: path - The URI path """

    logging.debug("Dryer Socket - Client Connected")
    print("Dryer Socket - Client Connected")

    #Try to read the message it just recieved
    try:
        async for message in websocket:
            logging.debug("Dryer Socket - Recieved: " + message)

            #Convert message into JSON
            json_string = json.loads(message)

            dryer_process = json_string['process']


            #If the first part of the message says start then start the dryer
            if dryer_process == "Start":
                logging.debug("Dryer Socket - Dryer Has Started")

                #Try to read the JSON string int he second part of the message
                try:

                    #Pull each axis from the JSON file
                    axis_x = 0 + json_string['axisX']
                    axis_y = 0 + json_string['axisY']
                    axis_z = 0 + json_string['axisZ']

                    logging.debug("DryerSOCKet - Axes Range is:")
                    axes = AxesModel.AxesModel(axis_x, axis_y, axis_z)
                    logging.debug('Axis X: %s Axis Y: %s Axis Z: %s', axis_x, axis_y, axis_z)

                    #The dryer service is called using the saved range
                    DryerService.DryerService.dryer_check(axes)

                    #Send message to client thier dryer stopped
                    await websocket.send("Dryer Finished")

                except TypeError:
                    logging.error("Invalid input, object was invalid")
                    await websocket.send("Invalid input, object was invalid")
                except:
                    logging.error("Unkown Error")
                    await websocket.send("Unkown Error")

            print("End - Dryer Socket")
            print()
            logging.debug("End - Dryer Socket")
    except websockets.exceptions.ConnectionClosed:
        print("Client Disconnected")

def main():
    """Start all the server processes when the file is called. This includes running
    the server and looping it. """

    #Start the server and loop it
    start_server = websockets.serve(dryer, IPADDRESS, PORT)
    asyncio.get_event_loop().run_until_complete(start_server)
    asyncio.get_event_loop().run_forever()

#Runs the service when started, defines the host and port.
if __name__ == '__main__':
    main()
