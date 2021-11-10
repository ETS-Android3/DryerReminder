# Web api using flask. Using port 8090 of this Pi's IP Address
# Remember to use http instead of https when testing on other computers.
#
#
#
from flask import Flask
from flask import request
import requests

app = Flask(__name__)


#simple get method
@app.route("/", methods=["GET"])
def hello_world():
        return "Hello, World"
    
#Post Api to return data. Data sent to this method needs to be in json.
@app.route("/send", methods=["POST"])
def alert_em():
    if request.method=='POST':
        request_data = request.get_json()
        alert = request_data['alert']
        alert = alert + " but"
        print(alert)
        return alert

#Sends request to Post to spring boot app
@app.route("/bird", methods=["POST"])
def bird_em():
    if request.method=='POST':
        paras = {"alert":"conure"}
        response = requests.post(url="http://192.168.0.94:8080/service/alert", data=paras)
        print(response)
        return "done"
    
app.run(host='0.0.0.0', port=8090)