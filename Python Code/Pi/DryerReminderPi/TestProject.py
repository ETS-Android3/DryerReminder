#Just for testing parts of the project without running the entire thing



import AxesModel




if __name__ == '__main__':
    print("JK... unless?")
    axes = AxesModel.AxesModel()
    
    print(axes.getAxisZ())
    
    axes.setAxisZ(73)
    
    print(axes.getAxisZ())