package martin.so.foodrecipemanager.model;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Passive class for initiating Firebase realtime database persistence to true.
 * New data will be able to be cached while offline,
 * and uploaded to Firebase once there is internet connection.
 */
public class FirebaseHandler extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}