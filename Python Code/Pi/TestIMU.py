#Test IMU
from sense_hat import SenseHat
import time

sense = SenseHat()
sense.clear()


while True:
    #Get Reading
    axies = sense.get_accelerometer_raw()
    x = axies['x']
    y = axies['y']
    z = axies['z']
    
    print(f"x={x}, y={y}, z={z}")
    time.sleep(1)