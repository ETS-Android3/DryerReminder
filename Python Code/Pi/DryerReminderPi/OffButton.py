#Shuts down the Raspberry Pi when the Sense HAT joystick button is pressed.
#When the button is pressed it runs the shutdown command line

from sense_hat import SenseHat
import time
import os
from pynput.keyboard import Key, Listener

sense = SenseHat()
sense.clear()

print("Ready for press")

#Always check for button press
while True:
    
    #Checking for SenseHAT button press
    for event in sense.stick.get_events():
        
        #If any button is pressed then continue
        if event.action == "pressed":
            
            #If the Middle Button is pressed then turn off the pi
            if event.direction == "middle":
                print("Turn off Pi")
                
                #This will shutdown the Pi safely.
                home_dir = os.system("sudo shutdown -P now")
                time.sleep(20000) #prevents multiple shutdowns from running
        
