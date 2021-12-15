# Flask API to handle the 4 main methods to use the Dryer Reminder Project.
#
#
#
from flask import Flask, jsonify, make_response
from flask_restful import Resource, Api
import time

import DryerLibrary
#import DryerService
#import WasherService
import AxesModel

#Intialize the varaibles for handling the ipAddress, port, and the sercurity toekn
ipAddress = ""
port = "7069"
token = ""


app = Flask(__name__)
api = Api(app)

#Dryer method for starting the dryer process after taking in the saved AxesModel values
class Dryer(Resource):
    def post(self):
        print("Start - Dryer API")
        #DryerService.dryerCheck()

        print("End - Dryer API") 
        return 
 
#Washer method for starting the washer process
class Washer(Resource): 
    def post(self):
        print("Start - Washer API")
        #WasherService.washerCheck()
        print("End - Washer API") 
        return 
  
#Calibrate the accelerometer by saving the axes and sending the results back.
class Calibrate(Resource): 
    def post(self):
        print("Start - Calibrate API")
        
        #This will return a method of the range for when the device is not moving
        #CalibrateService.calibrateRange()
        
        axes = AxesModel.AxesModel(4,7,9)
        
        try:
            print("End - Calibrate API") 
            return make_response(jsonify(axes.toJSON()), 200)
        except:
            return make_response("Error", 400)

#Update the offset to better detect when the device is or is not moving.
class Adjust(Resource): 
    def post(self):
        print("Start - Adjust API")
        #DryerLibrary.setOffset()
        print("End - adjust API") 
        return 

api.add_resource(Dryer, '/DryPi/dryStop')
api.add_resource(Washer, '/DryPi/washerStop')
api.add_resource(Calibrate, '/DryPi/calibrate')
api.add_resource(Adjust, '/DryPi/adjust')



if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=port)