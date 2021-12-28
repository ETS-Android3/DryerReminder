""" CalibrateService

Author: Michael Mohler
Data: 12/26/21
Version: 1
"""

import DryerLibrary
import AxesModel
import logging

class CalibrateService:
    
    def calibrateRange():
        logging.debug("Start - Calibrate Service")
        savedRange = DryerLibrary.calibrate()

        logging.debug("Calibrate - Axes Range is:")
        logging.debug('Axis X: %s Axis Y: %s Axis Z: %s', savedRange.get_axis_x(), savedRange.get_axis_y(), savedRange.get_axis_z())
        logging.debug("End - Calibrate Service")

        return savedRange


