"""
#Author: Michael Mohler
#Date: 8/22/2021
#Project: Dryer Detector
#Device: Use an accelerometer attached to a pi to determine if a dryer is off or on. Then send a
# notification to the user.
#Explanation: The Pi will achieve this by first saving the range of the x, y, z axis when
# the machine is off. If the current range is consistently below or equal to the 'off'
# range then send a notification and stop the device.

#This will be the one on the Pi
"""


import time #For the sleep command

import logging #Logging

#Let's IMU reed from the excelerameter
from sense_hat import SenseHat
import AxesModel


#Size of the array
SIZE = 60

#Initialize arrays for each axes
axes = []

#Initialize range used for calculating
saved_range = AxesModel.AxesModel(0,0,0)
range_axes = AxesModel.AxesModel(0,0,0)

#Logging Configure
logging.basicConfig(filename='dryer.log',
    format='%(asctime)s-%(levelname)s-%(message)s', level=logging.DEBUG)


def append_axes(AxesModel):
    """Adds values from the x, y, and z AxesModel object to the global Array.
    
    Arg: Current x,y,and z value, in an AxesModel object,from the accelerometer"""
    
    logging.debug("     ~ Append Axis X: %s Axis Y: %s Axis Z: %s", AxesModel.get_axis_x(),AxesModel.get_axis_y(), AxesModel.get_axis_z())
    axes.append(AxesModel)


def array_range(axis_a):
    """Gets the range of the axes array inserted by running a for each 
        loop to find the highest and lowest value. The range is the 
        highest value of each axis subtracted by the lowest value.
    
    Arg: axis_a Array of AxesModel populated with axis values from the past few seconds
    Return: found_range AxesModel of the range found from each axis"""

    #initalize the varible for the largest and smallest values of
    high_x = axis_a[0].get_axis_x()
    low_x = axis_a[0].get_axis_x()

    high_y = axis_a[0].get_axis_y()
    low_y = axis_a[0].get_axis_y()

    high_z = axis_a[0].get_axis_z()
    low_z = axis_a[0].get_axis_z()

    #Loop through array to find largest and smallest value of x.
    for every_axis in axis_a:

        #X axis
        if high_x < every_axis.get_axis_x():
            high_x = every_axis.get_axis_x()
        elif low_x > every_axis.get_axis_x():
            low_x = every_axis.get_axis_x()

        #Y axis
        if high_y < every_axis.get_axis_y():
            high_y = every_axis.get_axis_y()
        elif low_y > every_axis.get_axis_y():
            low_y = every_axis.get_axis_y()

        #Z axis
        if high_z < every_axis.get_axis_z():
            high_z = every_axis.get_axis_z()
        elif low_z > every_axis.get_axis_z():
            low_z = every_axis.get_axis_z()

    #Save and print range
    range_x = high_x - low_x
    range_y = high_y - low_y
    range_z = high_z - low_z

    #Save range to axes model
    found_range = AxesModel.AxesModel(range_x, range_y, range_z)

    #Print Range
    print()
    print("Range X: ", range_x)
    print("Range Y: ", range_y)
    print("Range Z: ", range_z)
    print()
    logging.debug("Range: X: %s Y: %s Z: %s", range_x, range_y, range_z)

    return found_range



def array_calc_ranges():
    """Takes the data from the current array and calls a method that will calculate the 
        range. Clear that array of data afterwards."""

    #Define the global variables so they can be looked at outside of the function.
    global range_axes

    range_axes = array_range(axes)

    #Clear the array for the next set
    axes.clear()
    logging.debug("Axes Array was cleared")



def array_size_check():
    """If the array is equal to or more then the size of the constant, 
        then call a method that will calculate the range and clear 
        the array for future checks"""

    if len(axes) >= SIZE:
        array_calc_ranges()



def set_offset(new_offset):
    """Save the offset to a file so the device will always have it.
    This will be called from the API.

    Arg: Float of the offset the user wants"""
    
    #Opens file to write adjust number to it.
    logging.debug("Saving Adjust to config file")

    try:
        with open('config.txt', 'w', encoding='utf8') as file:
            file.write(str(new_offset))
            file.close()
            logging.debug("Offset: %s saved", new_offset)

    except IOError:
        logging.error("File could not be read")
        raise Exception("File could not be made")



def get_offset():
    """Read the offset from the config.txt file. Defaults to a 3 when no file is found.
        Called from within the method.

    Return: Float of the offset used to determine the sensitivity of the shake detection"""
    
    logging.debug("Pulling Adjust to config file")
    try:
        with open("config.txt", encoding='utf8') as file:
            data = file.read()
            file.close()
            logging.debug("Offset: %s retrieved", data)
            return int(data)

    except IOError:
        logging.warning("File could not be found. Adjust defaulting to 3")
        print("File could not be found. Adjust defaulting to 3")
        return 3



def calibrate():
    """Calibrate the range of the accelerometer when it is not moving. It does this 
    by recording the movement of each axis every .2 sleep cycles and adding them to 
    an array. The size the array will get before calculating the range is based on a constant.

    Return: AxesModel of the range when the device is not moving"""
    
    logging.debug("Starting: Calibration Check")
    print("Starting: Calibration Check")
    print("")

    #Setup IMU
    sense = SenseHat()
    sense.clear()

    #Calibrate the first set when dryer is not moving.
    count = 0
    while count < SIZE:

        axies = sense.get_accelerometer_raw()
        append_axes(AxesModel.AxesModel(axies['x'], axies['y'], axies['z']))
        array_size_check()
        time.sleep(.2)
        count = count + 1

    #Range of each axis
    saved_range_x = range_axes.get_axis_x()
    saved_range_y = range_axes.get_axis_y()
    saved_range_z = range_axes.get_axis_z()

    print("Ending: Calibration Check")
    logging.debug("Ending: Calibration Check")
    return AxesModel.AxesModel(saved_range_x, saved_range_y, saved_range_z)



def justMain(given_range):
    """The main feature of the device. Take the saved ranged from calibrate and add an offset to determine sensitivity. 
    Save the movmemnt of the accelorometer every .2 sleep cycles. Once the array hits the SIZE constant, find the range.
    Compare the range of the accelerometer to the range when it was not moving to determine if the device is moving.

    Args: AxesModel of the calibrated saved range"""

    count_check = 0
    global saved_range

    logging.debug("Starting: Shake Detection")
    print("Starting: Shake Detection")

    offset = get_offset()

    #create the offset for each axis
    saved_range_x = given_range.get_axis_x() + (given_range.get_axis_x() * offset)
    saved_range_y = given_range.get_axis_y() + (given_range.get_axis_y() * offset)
    saved_range_z = given_range.get_axis_z() + (given_range.get_axis_z() * offset)

    logging.debug("Saved Range with offset Axis X: %s Axis Y: %s Axis Z: %s", saved_range_x, saved_range_y, saved_range_z)

    saved_range = AxesModel.AxesModel(saved_range_x, saved_range_y, saved_range_z)
    

    #Setup IMU
    sense = SenseHat()
    sense.clear()

    #Constantly record movememnt and compare it to the saved range.
    while True:
        axies = sense.get_accelerometer_raw()
        append_axes(AxesModel.AxesModel(axies['x'], axies['y'], axies['z']))
        array_size_check()
        time.sleep(.2)

        if len(axes) == 0:
            #Check if dryer is moving or not.
            if (range_axes.get_axis_x() >= saved_range.get_axis_x()) or (range_axes.get_axis_y() >= saved_range.get_axis_y()) or (range_axes.get_axis_z() >= saved_range.get_axis_z()):
                print("==Dryer is moving==")
                logging.debug("==Dryer is moving==")
                count_check = 0
            else:
                print("==Dryer is not moving==")
                print()
                logging.debug("==Dryer is not moving==")
                count_check = count_check + 1
                if count_check == 2:
                    print("===Dryer has stopped===")
                    logging.debug("===Dryer has stopped===")
                    break



    print("")
    print("Ending: Shake Detection")
    logging.debug("Ending: Shake Detection")

#Main Method
if __name__ == '__main__':
    #calibrate()
    justMain(AxesModel.AxesModel(.0027, .0029, .0040))
    #while True:
    #    justMain(calibrate())
