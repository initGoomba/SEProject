package com.example.tripplannr.application_layer.addressservice;

import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

import com.example.tripplannr.application_layer.search.SearchViewModel;

public class AddressResultReceiver extends ResultReceiver {

    private final SearchViewModel model;
    private final Location clickedLocation;

    public AddressResultReceiver(SearchViewModel model, Handler handler, Location clickedLocation) {
        super(handler);
        this.model = model;
        this.clickedLocation = clickedLocation;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (resultData == null) {
            return;
        }
        // Display the name string or an error message sent from the intent service.
        String addressOutput =
                resultData.getString(FetchAddressConstants.RESULT_DATA_KEY);
        if (addressOutput == null) {
            addressOutput = "";
        }
        model.setLocation(clickedLocation, addressOutput);
    }
}
