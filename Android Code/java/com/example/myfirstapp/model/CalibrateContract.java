package com.example.myfirstapp.presenter;

import com.example.myfirstapp.model.AxesModel;
import com.example.myfirstapp.model.ClientModel;

public interface CalibrateContract {

    interface View{
        public void showInUseError();
        public void writeToFile(AxesModel savedRange);
    }

    interface Presenter{
        public void doCalibrate();
    }
}
