package com.example.tripplannr.application_layer.profile;

import android.app.Notification;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tripplannr.R;
import com.example.tripplannr.application_layer.trip.RoutesAdapter;
import com.example.tripplannr.application_layer.trip.TripResultViewModel;
import com.example.tripplannr.application_layer.util.InjectorUtils;
import com.example.tripplannr.application_layer.util.ModeOfTransportIconDictionary;
import com.example.tripplannr.databinding.FragmentSavedTripDetailsBinding;
import com.example.tripplannr.domain_layer.Trip;

import java.util.Objects;


public class SavedTripDetailsFragment extends Fragment {

    private TripResultViewModel viewModel;
    private Trip tripData;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentSavedTripDetailsBinding detailsBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_saved_trip_details, container, false);
        View view = detailsBinding.getRoot();
        detailsBinding.setFragment(this);
        initViewModel();
        initRecyclerView(view);
        return view;
    }

    private void initViewModel() {
        viewModel = InjectorUtils.getTripResultViewModel(getContext(), getActivity());
        tripData = viewModel.getTripLiveData().getValue();
    }

    private void initRecyclerView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.routesRecyclerView);
        recyclerView.setAdapter(new RoutesAdapter(viewModel));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    public void deleteSavedTrip() {
        NotificationManagerCompat.from(Objects.requireNonNull(getActivity())).notify(0, getNotification());
        viewModel.removeTrip(tripData);
        Navigation.findNavController(getView()).navigate(R.id.action_navigation_saved_trips_detailed_to_navigation_saved_trips);
    }

    private Notification getNotification() {
        return new NotificationCompat.Builder(Objects.requireNonNull(getActivity()), "TRIP_CHANNEL")
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), ModeOfTransportIconDictionary.getTransportIcon(tripData.getRoutes().get(0).getMode())))
                .setContentTitle("Notification about your Trip")
                .setContentText("Your trip \"" + tripData.getName() + "\" has been deleted from save trips," +
                        " you will no longer be receiving notifications related to this trip")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_ALL)
                .setStyle(new NotificationCompat.BigTextStyle())
                .build();
    }


}
