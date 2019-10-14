package com.example.tripplannr.application_layer.search;

import android.location.Location;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tripplannr.application_layer.util.Utilities;
import com.example.tripplannr.data_access_layer.repositories.VasttafikRepository;
import com.example.tripplannr.domain_layer.Trip;
import com.example.tripplannr.domain_layer.TripLocation;
import com.example.tripplannr.data_access_layer.repositories.GenericTripRepository;
import com.example.tripplannr.domain_layer.TripQuery;

import java.util.ArrayDeque;
import java.util.Calendar;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

import static com.example.tripplannr.application_layer.search.SearchViewModel.LocationField.*;
import static com.example.tripplannr.application_layer.search.SearchViewModel.ShownFragment.*;

public class SearchViewModel extends ViewModel {

    public enum ShownFragment {MAP, TIME_CONTROLS}
    public enum LocationField {ORIGIN, DESTINATION}
    private MutableLiveData<TripLocation> origin = new MutableLiveData<>();
    private MutableLiveData<TripLocation> destination = new MutableLiveData<>();
    private MutableLiveData<Boolean> addressQuery = new MutableLiveData<>();
    private MutableLiveData<Calendar> desiredTime = new MutableLiveData<>();
    private MutableLiveData<Boolean> timeIsDeparture = new MutableLiveData<>();
    private MutableLiveData<ShownFragment> fragments = new MutableLiveData<>();
    private MutableLiveData<List<Trip>> trips = new MutableLiveData<>();
    private VasttafikRepository vasttafikRepository = new VasttafikRepository();

    private boolean initOriginField = true;
    private Deque<LocationField> focusedLocationFields = new ArrayDeque<>();
    private GenericTripRepository genericTripRepository;

    public SearchViewModel() {
        focusedLocationFields.push(DESTINATION);
        timeIsDeparture.setValue(true);
        genericTripRepository = new GenericTripRepository();
        desiredTime.setValue(Calendar.getInstance());
    }

    public void obtainTrips(String origin, String destination) {
        this.origin.setValue(new TripLocation(origin, new Location("")));
        this.destination.setValue(new TripLocation(destination, new Location("")));
        vasttafikRepository.loadTrips(obtainQuery());
    }

    private TripQuery obtainQuery() {
        return new TripQuery.Builder()
                .origin(origin.getValue().getName())
                .destination(destination.getValue().getName())
                .time(Utilities.toLocalDateTime(desiredTime.getValue()))
                .timeIsDeparture(timeIsDeparture.getValue())
                .build();
    }

    public MutableLiveData<ShownFragment> getFragments() {
        return fragments;
    }

    public void setTime(Calendar desiredTime, boolean timeIsDeparture) {
        this.desiredTime.setValue(desiredTime);
        this.timeIsDeparture.setValue(timeIsDeparture);
    }

    public void showTimeControls() {
        fragments.setValue(TIME_CONTROLS);
    }

    public void showMap() {
        fragments.setValue(MAP);
    }

    public void flattenFocLocationStack() {
        if (focusedLocationFields.size() > 1) focusedLocationFields.remove();
    }

    public void setAddressQuery(boolean addressQuery) {
        this.addressQuery.setValue(addressQuery);
    }

    public void getCurrTripLocation() {
        focusedLocationFields.push(ORIGIN);
        addressQuery.setValue(true);
    }

    public void setFocusedLocationField(LocationField focusedLocationField) {
        focusedLocationFields.remove();
        focusedLocationFields.push(focusedLocationField);
    }

    public LocationField getFocusedLocationField() {
        return focusedLocationFields.peek();
    }

    public void setLocation(Location location, String name) {
        TripLocation tripLocation = null;
        if (location != null) tripLocation = new TripLocation(name, location, null);
        if (initOriginField) {
            origin.setValue(tripLocation);
            initOriginField = false;
        }
        else if (focusedLocationFields.peek() == ORIGIN)
            origin.setValue(tripLocation);
        else destination.setValue(tripLocation);
    }

    public MutableLiveData<Boolean> getAddressQuery() {
        return addressQuery;
    }

    public LiveData<TripLocation> getOrigin() {
        return origin;
    }

    public LiveData<TripLocation> getDestination() {
        return destination;
    }

    public MutableLiveData<Calendar> getDesiredTime() {
        return desiredTime;
    }

    public MutableLiveData<Boolean> getTimeIsDeparture() {
        return timeIsDeparture;
    }

    public MutableLiveData<List<Trip>> getTrips() {
        return trips;
    }

    public boolean isInitOriginField() {
        return initOriginField;
    }
}