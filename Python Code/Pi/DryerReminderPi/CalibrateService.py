""" CalibrateService

Author: Michael Mohler
Data: 12/26/21
Version: 1
"""
import logging
import DryerLibrary


class CalibrateService:
    """Calibrate Service to be used as an inbetween for the api and library"""

    def calibrate_range():
        """Calls the library service to calibrate the device
        
         Return: AxesModel of the calibrated range"""

        logging.debug("Start - Calibrate Service")
        saved_range = DryerLibrary.calibrate()

        logging.debug("Calibrate - Axes Range is:")
        logging.debug('Axis X: %s Axis Y: %s Axis Z: %s',
            saved_range.get_axis_x(), saved_range.get_axis_y(), saved_range.get_axis_z())
        logging.debug("End - Calibrate Service")

        return saved_range
