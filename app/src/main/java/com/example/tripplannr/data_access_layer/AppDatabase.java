package com.example.tripplannr.data_access_layer;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.tripplannr.data_access_layer.data_sources.TripDAO;
import com.example.tripplannr.data_access_layer.type_converters.LocalDateTimeConverter;
import com.example.tripplannr.data_access_layer.type_converters.ModeOfTransportConverter;
import com.example.tripplannr.domain_layer.Route;
import com.example.tripplannr.domain_layer.Trip;

@Database(entities = {Trip.class, Route.class}, version = 2, exportSchema = false)
@TypeConverters({LocalDateTimeConverter.class, ModeOfTransportConverter.class})
public abstract class AppDatabase extends RoomDatabase {

    public abstract TripDAO tripDAO();

    private volatile static AppDatabase instance = null;

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room
                    .databaseBuilder(context, AppDatabase.class, "TripDatabase")
                    .build();
        }
        return instance;
    }

}
