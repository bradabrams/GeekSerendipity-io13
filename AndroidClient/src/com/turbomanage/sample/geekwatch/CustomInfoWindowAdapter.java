package com.turbomanage.sample.geekwatch;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;

public class CustomInfoWindowAdapter implements InfoWindowAdapter {

	private LayoutInflater mInflater;

	/**
	 * @param geekwatchActivity
	 */
	CustomInfoWindowAdapter(LayoutInflater inflater) {
		mInflater = inflater;
	}

		@Override
        public View getInfoContents(Marker marker) {
                View view = mInflater.inflate(
                                R.layout.map_info_window_layout, null);
                TextView title = (TextView) view.findViewById(R.id.title);
                TextView sinceWhen = (TextView) view.findViewById(R.id.sinceWhen);
                title.setText(marker.getTitle());
                sinceWhen.setText(sinceWhen(marker.getSnippet()));
                return view;
        }

        @Override
        public View getInfoWindow(Marker marker) {
                // use default window
                return null;
        }

        private String sinceWhen(String timestamp) {
                if (timestamp == null) {
                        return "";
                }
                long secs = (System.currentTimeMillis() - Long.valueOf(timestamp)) / 1000;
                if (secs < 60) {
                        return secs + " sec ago";
                } else if (secs < 3600) {
                        return (secs / 60) + " min ago";
                } else if (secs < 3600 * 24) {
                        return (secs / 3600) + " hrs ago";
                } else {
                        return (secs / 3600 / 24) + " days ago";
                }
        }

}