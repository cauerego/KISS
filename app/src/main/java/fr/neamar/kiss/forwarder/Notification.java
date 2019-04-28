package fr.neamar.kiss.forwarder;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import fr.neamar.kiss.MainActivity;
import fr.neamar.kiss.R;
import fr.neamar.kiss.notification.NotificationListener;

import static android.content.Context.MODE_PRIVATE;

class Notification extends Forwarder {
    private final SharedPreferences notificationPreferences;

    private SharedPreferences.OnSharedPreferenceChangeListener onNotificationDisplayed = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String packageName) {
            Log.e("WTF", "Invalidated dataset: " + packageName);
            ListView list = mainActivity.list;

            // A new notification was received, iterate over the currently displayed results
            // if one of them is for the package that just received a notification,
            // update the notification dot visual if required.
            //
            // This implementation should be more efficient than calling notifyDataSetInvalidated()
            // since we only iterate over the items currently displayed in the list
            // and do not rebuild them all, just toggle visibility if required.
            updateDots(list, list.getLastVisiblePosition() - list.getFirstVisiblePosition(), packageName);

            updateDots(mainActivity.favoritesBar, mainActivity.favoritesBar.getChildCount(), packageName);

        }
    };

    Notification(MainActivity mainActivity) {
        super(mainActivity);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            notificationPreferences = mainActivity.getSharedPreferences(NotificationListener.NOTIFICATION_PREFERENCES_NAME, MODE_PRIVATE);
        } else {
            notificationPreferences = null;
        }
    }

    void onCreate() {
        if (notificationPreferences != null) {
            notificationPreferences.registerOnSharedPreferenceChangeListener(onNotificationDisplayed);
        }
    }

    void onStop() {
        if (notificationPreferences != null) {
            notificationPreferences.unregisterOnSharedPreferenceChangeListener(onNotificationDisplayed);
        }
    }

    private void updateDots(ViewGroup vg, int childCount, String packageName) {
        for (int i = 0; i < childCount; i++) {
            View v = vg.getChildAt(i);
            final View notificationDot = v.findViewById(R.id.item_notification_dot);
            if (notificationDot != null && notificationDot.getTag().toString().equals(packageName)) {
                boolean hasNotification = notificationPreferences.contains(packageName);
                animateDot(notificationDot, hasNotification);
            }
        }
    }

    private void animateDot(final View notificationDot, Boolean hasNotification) {
        int currentVisibility = notificationDot.getVisibility();

        if(currentVisibility != View.VISIBLE && hasNotification) {
            // There is a notification and dot was not visible
            notificationDot.setVisibility(View.VISIBLE);
            notificationDot.setAlpha(0);
            notificationDot.animate().alpha(1).setListener(null);
        }
        else if(currentVisibility == View.VISIBLE && !hasNotification) {
            // There is no notification anymore, and dot was visible
            notificationDot.animate().alpha(0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    notificationDot.setVisibility(View.GONE);
                    notificationDot.setAlpha(1);
                }
            });
        }
    }
}
