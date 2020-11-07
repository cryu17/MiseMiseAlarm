package com.example.dustapp;

public interface AsyncTaskListener {
    public void updateUI(boolean result);

    public void getData(DataPackage dpkg);
}
