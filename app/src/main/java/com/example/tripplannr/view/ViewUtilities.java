package com.example.tripplannr.view;

import android.view.MotionEvent;
import android.view.View;

// Class for reusable view methods

class ViewUtilities {

    final static float SEMI_TRANSPARENT_ALPHA = 0.5f;
    final static float OPAQUE_ALPHA = 1f;


    // Set different levels of alpha (opacity) when view pressed and when not
    static void setAlphaLevels(View v, MotionEvent event, float pressedAlpha, float nonPressedAlpha) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                v.setAlpha(pressedAlpha);
                break;
            case MotionEvent.ACTION_UP:
                v.setAlpha(nonPressedAlpha);
            default:
                v.setAlpha(nonPressedAlpha);
        }
    }

    static void setAlphaLevels(View v, MotionEvent event) {
        setAlphaLevels(v, event, SEMI_TRANSPARENT_ALPHA, OPAQUE_ALPHA);
    }

}