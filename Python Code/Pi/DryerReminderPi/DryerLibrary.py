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

#For the sleep command
import time

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


def append_axes(AxesModel):
    """Adds values in the x, y, and z arrays at the same time."""
    axes.append(AxesModel)


def array_range(axis_a):
    """Get's range of the axes array inserted."""

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
    print("Range X: ", range_x)
    print("Range Y: ", range_y)
    print("Range Z: ", range_z)

    return found_range



def array_calc_ranges():
    """Save the ranges of the axis arrays to their ranges and clear them."""

    #Define the global variables so they can be looked at outside of the function.
    global range_axes

    range_axes = array_range(axes)

    #Clear the array for the next set
    axes.clear()



def array_size_check():
    """If one of the arrays are equal to or more then"""

    if len(axes) >= SIZE:
        array_calc_ranges()



def set_offset(new_offset):
    """Save the offset to a file so the Pi will always remember it"""

    file = open('config.txt', 'w')
    file.write(str(new_offset))
    file.close()


#pull the offset from a file and return it as an int
def get_offset():
    """Read the offset from the config.txt file"""

    file = open('config.txt', 'r')
    data = file.read()
    file.close()
    return int(data)



def calibrate():
    """Calibrate the range of the accelerometer when it is not moving"""
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

    #Offset needs to be moved to main method
    saved_range_x = range_axes.get_axis_x()
    saved_range_y = range_axes.get_axis_y()
    saved_range_z = range_axes.get_axis_z()

    print("Ending: Calibration Check")
    return AxesModel.AxesModel(saved_range_x, saved_range_y, saved_range_z)



def justMain(given_range):
    """Compare the range of the accelerometer to the range when it is not moving"""

    count_check = 0

    global saved_range

    set_offset(2)
    offset = get_offset()


    #create the offset for each axis
    saved_range_x = given_range.get_axis_x() + (given_range.get_axis_x() * offset)
    saved_range_y = given_range.get_axis_y() + (given_range.get_axis_y() * offset)
    saved_range_z = given_range.get_axis_z() + (given_range.get_axis_z() * offset)

    saved_range = AxesModel.AxesModel(saved_range_x, saved_range_y, saved_range_z)


    print("Starting: Shake Detection")

    #Setup IMU
    sense = SenseHat()
    sense.clear()

    #Constantly
    while True:
        axies = sense.get_accelerometer_raw()
        append_axes(AxesModel.AxesModel(axies['x'], axies['y'], axies['z']))
        array_size_check()
        time.sleep(.2)


        if len(axes) == 0:
            #Check if dryer is moving or not.
            if range_axes.get_axis_y() <= saved_range.get_axis_y():
                print()
                print("==Dryer is not moving==")
                count_check = count_check + 1
                if count_check == 2:
                    print("Dryer has stopped")
                    break
            else:
                print()
                print("==Dryer is moving==")
                count_check = 0

    print("")
    print("End")

#Main Method
if __name__ == '__main__':
    #calibrate()
    #justMain(AxesModel.AxesModel(.02, .004, .06))
    justMain(calibrate())
