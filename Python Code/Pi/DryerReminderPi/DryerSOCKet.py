import asyncio
import websockets
import logging
import logging.handlers as handlers
import json
import AxesModel
import DryerService
import _thread

IPADDRESS = '172.24.170.0'
PORT = 9607

#Logging Configure
logging.basicConfig(filename='dryer.log',
    format='%(asctime)s-%(levelname)s-%(message)s', level=logging.DEBUG)
logger = logging.getLogger('DryerAPI')
logHandler = handlers.RotatingFileHandler('dryer.log', maxBytes=1000000, backupCount=1)
logger.addHandler(logHandler)

logging.debug("Start - Dryer Socket")
print("Start - Dryer Socket")
    
async def dryer(websocket, path):
    logging.debug("Dryer Socket - Client Connected")
    print("Dryer Socket - Client Connected")

    #Try to read message
    try:
        async for message in websocket:
            logging.debug("Dryer Socket - Recieved: " + message)

            #Convert message into JSON
            json_string = json.loads(message)

            dryer_process = json_string['process']


            #If the first part of the message says start then start the dryer
            if(dryer_process == "Start"):
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

            if(dryer_process == "End"):
                logging.debug("Dryer Socket - Dryer Detection Should Stop")

                #Sets dryer status to false
                DryerLibrary.set_moving_false()

                await websocket.send("Dryer Detection Stopped")

            print("End - Dryer Socket")
            print()
            logging.debug("End - Dryer Socket")
    except websockets.exceptions.ConnectionClosed as e:
        print("Client Disconnected")

def main():
    #Start the server and loop it
    start_server = websockets.serve(dryer, IPADDRESS, PORT)
    asyncio.get_event_loop().run_until_complete(start_server)
    asyncio.get_event_loop().run_forever()

#Runs the service when started, defines the host and port.
if __name__ == '__main__':
    main()