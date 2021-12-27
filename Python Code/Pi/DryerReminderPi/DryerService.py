""" DryerService

Author: Michael Mohler
Data: 12/26/21
Version: 1
"""

import DryerLibrary
import AxesModel

class DryerService:
    
    def dryerCheck(savedRange):
        DryerLibrary.justMain(savedRange)
        
    if __name__ == '__main__':
        axes = AxesModel.AxesModel(1, 1, 1)
        dryerCheck(axes)
