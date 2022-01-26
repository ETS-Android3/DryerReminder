package com.example.myfirstapp.presenter;

import com.example.myfirstapp.model.ClientModel;

public interface CalibrateContract {

    interface View{
        public void showInUseError();
    }

    interface Presenter{
        public void doCalibrate();
    }
}
