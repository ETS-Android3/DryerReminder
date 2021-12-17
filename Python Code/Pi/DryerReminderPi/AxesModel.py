""" The Axes Model is an object model that will be used to hold 3 values
representing each axis: X, Y, Z.

Author: Michael Mohler
Data: 12/07/21
Version: 1
"""
class AxesModel:
    """Axes Object Model"""
    
    #Varaibles for each axis
    axis_x = 0
    axis_y = 0
    axis_z = 0


    def __init__(self, axis_x=0, axis_y=0,axis_z=0):
        """Parameterized And Default Constructor.
        Take in three number variables that represent each axis"""

        self.axis_x = axis_x
        self.axis_y = axis_y
        self.axis_z = axis_z



    def to_json(self):
        """Converts the current AxesModel object into a JSON like object
        Returns A JSON format after being used with jsonify"""
        
        json_format = {
            "axisX":self.axis_x,
            "axisY":self.axis_y,
            "axisZ":self.axis_z
            }
        
        return json_format

    #---Getters for X, Y, Z varaibles---
    def get_axis_x(self):
        """Getter for X Axis"""
        return self.axis_x

    def get_axis_y(self):
        """Getter for Y Axis"""
        return self.axis_y

    def get_axis_z(self):
        """Getter for Z Axis"""
        return self.axis_z


    #---Setters for X, Y, Z varaibles---
    def set_axis_x(self, axis_x=0):
        """Setter for X Axis"""
        self.axis_x = axis_x

    def set_axis_y(self, axis_y=0):
        """Setter for Y Axis"""
        self.axis_y = axis_y

    def set_axis_z(self, axis_z=0):
        """Setter for Z Axis"""
        self.axis_z = axis_z
