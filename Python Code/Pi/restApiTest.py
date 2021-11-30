# Web api using flask. Using port 8090 of this Pi's IP Address
# Remember to use http instead of https when testing on other computers.
#
#
#
from flask import Flask, request
from flask_restful import Resource, Api
import time
import DryerCheck

app = Flask(__name__)
api = Api(app)

class HelloWorld(Resource):
    def get(self):
        print("Start - API")
        DryerCheck.mainMethod() 
        print("End - API") 
        return {'about':'Hello World!'} 
    
    def post(self):
        some_json = request.get_json()
        return {'you sent': some_json}, 201
    
class Multi(Resource):
    def get(self,num):
        return {'result' : num*10}
    
api.add_resource(HelloWorld, '/')
api.add_resource(Multi, '/multi/<int:num>')


if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=8090)