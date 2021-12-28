""" DryerService

Author: Michael Mohler
Data: 12/26/21
Version: 1
"""

import DryerLibrary
import AxesModel
import logging

#Logging Configure
logging.basicConfig(filename='dryer.log', format='%(asctime)s-%(levelname)s-%(message)s', level=logging.DEBUG)

class DryerService:
    
    def dryerCheck(savedRange):
        logging.debug("Start - Dryer Service")
        DryerLibrary.justMain(savedRange)
        logging.debug("End - Dryer Service")
        
    if __name__ == '__main__':
        axes = AxesModel.AxesModel(1, 1, 1)
        dryerCheck(axes)
