# Flask API to handle the 4 main methods to use the Dryer Reminder Project.
#
#
#
from flask import Flask, request
from flask_restful import Resource, Api
import time

#import DryerLibrary
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
        axes = AxesModel.AxesModel()
        axes.setAxisX(2)
        axes.setAxisY(8)
        axes.setAxisZ(33)
        print(axes.toJSON())
        print("End - Dryer API") 
        return axes.toJSON()
 
#Washer method for starting the washer process
class Washer(Resource): 
    def post(self):
        print("Start - Washer API")
        WasherService.washerCheck()
        print("End - Washer API") 
        return 
  
#Calibrate the accelerameter by saving the axes and sending the results back.
class Calibrate(Resource): 
    def post(self):
        print("Start - Calibrate API")
        CalibrateService.calibrateRange()
        print("End - Calibrate API") 
        return 

#Update the offset to better detect when the device is or is not moving.
class Adjust(Resource): 
    def post(self):
        print("Start - Adjust API")
        DryerLibrary.setOffset()
        print("End - adjust API") 
        return 

api.add_resource(Dryer, '/DryPi/dryStop')
api.add_resource(Washer, '/DryPi/washerStop')
api.add_resource(Calibrate, '/DryPi/calibrate')
api.add_resource(Adjust, '/DryPi/adjust')



if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=port)