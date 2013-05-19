package com.turbomanage.sample.geekwatch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.cloud.backend.android.CloudBackendActivity;
import com.google.cloud.backend.android.CloudCallbackHandler;
import com.google.cloud.backend.android.CloudEntity;
import com.google.cloud.backend.android.CloudQuery;
import com.google.cloud.backend.android.CloudQuery.Order;
import com.google.cloud.backend.android.CloudQuery.Scope;
import com.google.cloud.backend.android.F.Op;
import com.turbomanage.sample.geekwatch.InterestPickerDialog.HasGeekInterest;

public class GeekwatchActivity extends CloudBackendActivity implements
                OnCameraChangeListener, OnMyLocationChangeListener, HasGeekInterest {

        private GoogleMap mMap;
        private TextView locText;
        private String mCurrentRegionHash;
        private Location mCurrentLocation;
        private Location mLastLocation;
        private Geek mSelf;
        // Indicates that we're still waiting for an accurate location fix
        private boolean mWaitingForLoc = true;
        private String mInterest;
        private static final Geohasher gh = new Geohasher();
        private static final String KEY_CURRENT_LOC = "mCurrentLocation";
        private static final String KEY_ZOOM = "zoom";
		private static final String KEY_INTEREST = "interest";
		private List<Geek> mGeeks = new ArrayList<Geek>();

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_geekwatch);
                locText = (TextView) findViewById(R.id.loc);
        }

        /* (non-Javadoc)
         * @see com.google.cloud.backend.android.CloudBackendActivity#onPostCreate()
         */
        @Override
        protected void onPostCreate() {
        		super.onPostCreate();
    			mInterest = getSelectedInterest();
    			if (mInterest == null) {
    				showInterestPickerDialog();
    			}
        }

        @Override
        protected void onPause() {
                super.onPause();
                // save current location
                SharedPreferences.Editor ed = getPreferences(MODE_PRIVATE).edit();
                if (mMap != null) {
                        CameraPosition camPos = mMap.getCameraPosition();
                        ed.putString(KEY_CURRENT_LOC, gh.encode(camPos.target));
                        ed.putFloat(KEY_ZOOM, camPos.zoom);
                }
                ed.commit();
        }

		@Override
		protected void onResume() {
			super.onResume();
			setUpMapIfNeeded();
			SharedPreferences prefs = getPreferences(MODE_PRIVATE);
			String locHash = prefs.getString(KEY_CURRENT_LOC, "9q8yy");
			LatLng camPos = gh.decode(locHash);
			float zoom = prefs.getFloat(KEY_ZOOM, 16f);
			try {
			    this.mCurrentRegionHash = null;
			    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(camPos, zoom));
            } catch (Exception e) {
                // gulp: CameraUpdateFactory not ready if Google Play Services
                // needs to be updated
            }
			startUpdateTimer();
			this.mWaitingForLoc = true;
		}

        /**
         * Starts the timer to send location every 2 min
         */
        private void startUpdateTimer() {
                final Handler handler = new Handler();
                handler.post(new Runnable() {
                        @Override
                        public void run() {
                                sendMyLocation();
                                handler.postDelayed(this, 120000); // 2 min
                        }
                });
        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
                // Inflate the menu; this adds items to the action bar if it is present.
                getMenuInflater().inflate(R.menu.activity_geekwatch, menu);
                return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
                switch (item.getItemId()) {
                case R.id.menu_settings:
                		showInterestPickerDialog();
                		return true;
                }
                return super.onOptionsItemSelected(item);
        }

        private void showInterestPickerDialog() {
			InterestPickerDialog dlg = new InterestPickerDialog();
			dlg.show(getSupportFragmentManager(), "interests");
		}

		private void setUpMapIfNeeded() {
                // Do a null check to confirm that we have not already instantiated the
                // map.
                if (mMap == null) {
                        // Try to obtain the map from the SupportMapFragment.
                        mMap = ((SupportMapFragment) getSupportFragmentManager()
                                        .findFragmentById(R.id.map)).getMap();
                        // Check if we were successful in obtaining the map.
                        if (mMap != null) {
                                setUpMap();
                        }
                }
        }

        private void setUpMap() {
                mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(getLayoutInflater()));
                mMap.getUiSettings().setCompassEnabled(true);
                mMap.setMyLocationEnabled(true);
                mMap.setOnCameraChangeListener(this);
                mMap.setOnMyLocationChangeListener(this);
        }

        @Override
        public void onMyLocationChange(Location location) {
                double lat = location.getLatitude();
                double lon = location.getLongitude();
                locText.setText("My location:\n" + lat + "\n" + lon + "\n"
                                + gh.encode(lat, lon));
                // on start or first reliable fix, center the map
                boolean firstGoodFix = mWaitingForLoc && location.getAccuracy() < 30.;
                if (mCurrentLocation == null || firstGoodFix) {
                        LatLng myLocation = new LatLng(lat, lon);
                        // center map on new location
                        // TODO animate vs. move?
                        mMap.animateCamera(CameraUpdateFactory.newLatLng(myLocation));
                }
                mCurrentLocation = location;
                if (firstGoodFix) {
                        sendMyLocation(location);
                        mWaitingForLoc = false;
                }
        }

        @Override
        public void onCameraChange(CameraPosition position) {
//                LatLngBounds visibleBounds = mMap.getProjection().getVisibleRegion().latLngBounds;
//                String visibleRegionHash = gh.findHashForRegion(visibleBounds);
                String visibleRegionHash = "allGeeks";
                findGeeks(visibleRegionHash);
        }

        private void findGeeks(String visibleRegionHash) {
                // We've moved or the visible region has changed
                if (!visibleRegionHash.equals(mCurrentRegionHash)) {
                        mCurrentRegionHash = visibleRegionHash;
//                        Toast.makeText(this, visibleRegionHash, Toast.LENGTH_LONG).show();
                        queryGeeksWithin(visibleRegionHash);
                }
        }

        private void queryGeeksWithin(String visibleRegionHash) {
                CloudCallbackHandler<List<CloudEntity>> handler = new CloudCallbackHandler<List<CloudEntity>>() {
                        @Override
                        public void onComplete(List<CloudEntity> results) {
                                mGeeks = Geek.fromEntities(results);
                                drawMarkers();
                        }

                        @Override
                        public void onError(IOException exception) {
                                handleEndpointException(exception);
                        }
                };

                // Remove previous query
            		getCloudBackend().clearAllSubscription();
                // execute the query with the handler
            	CloudQuery cq = new CloudQuery("Geek");
            	    cq.setSort(CloudEntity.PROP_UPDATED_AT, Order.DESC);
                cq.setLimit(100);
                cq.setScope(Scope.FUTURE_AND_PAST);
                getCloudBackend().list(cq, handler);
        }

		private void drawMarkers() {
			mMap.clear();
			for (Geek geek : mGeeks) {
				if (geek.getGeohash() != null) {
					LatLng pos = gh.decode(geek.getGeohash());
					// choose marker color
					float markerColor;
					if (geek.getName() != null && geek.getName().equals(super.getAccountName())) {
						markerColor = BitmapDescriptorFactory.HUE_AZURE;
					} else {
						if (geek.getInterest() != null) {
							markerColor = InterestPickerDialog.getInterestColor(geek.getInterest());
						} else {
							markerColor = BitmapDescriptorFactory.HUE_RED;
						}
					}
					mMap.addMarker(new MarkerOptions()
						.position(pos)
						.title(geek.getInterest() + " Geek")
						.snippet("" + geek.getUpdatedAt().getTime())
						.icon(BitmapDescriptorFactory
								.defaultMarker(markerColor)));
				}
			}
		}

		/**
         * Send location to server if we've moved >30m
         */
        void sendMyLocation() {
                if (mCurrentLocation != null) {
                        if (mLastLocation == null
                                        || mLastLocation.distanceTo(mCurrentLocation) > 30.) {
                                sendMyLocation(mCurrentLocation);
                        }
                }
        }

		final CloudCallbackHandler<CloudEntity> updateHandler = new CloudCallbackHandler<CloudEntity>() {
			@Override
			public void onComplete(final CloudEntity result) {
				// Update mLastLocation only after success so timer will keep
				// trying otherwise
				mLastLocation = mCurrentLocation;
				mSelf = new Geek(result);
				mGeeks.remove(mSelf);
				mGeeks.add(mSelf);
				drawMarkers();
			}

			@Override
			public void onError(final IOException exception) {
				handleEndpointException(exception);
			}
		};

        void sendMyLocation(final Location loc) {
                // execute the insertion with the handler
                // query for existing username before inserting
                if (mSelf == null || mSelf.asEntity().getId() == null) {
                        getCloudBackend().listByProperty("Geek", "name", Op.EQ,
                                        super.getAccountName(), null, 1, Scope.PAST,
                                        new CloudCallbackHandler<List<CloudEntity>>() {
                                                @Override
                                                public void onComplete(List<CloudEntity> results) {
                                                        if (results.size() > 0) {
                                                                mSelf = new Geek(results.get(0));
                                                                mSelf.setGeohash(gh.encode(loc));
                                                                mSelf.setInterest(mInterest);
                                                                getCloudBackend().update(mSelf.asEntity(),
                                                                                updateHandler);
                                                        } else {
                                                                final Geek newGeek = new Geek(
                                                                                GeekwatchActivity.super
                                                                                                .getAccountName(),
                                                                                                mInterest,
                                                                                                gh.encode(loc));
                                                                getCloudBackend().insert(newGeek.asEntity(),
                                                                                updateHandler);
                                                        }
                                                }
                                        });
                } else {
                        mSelf.setGeohash(gh.encode(loc));
                        mSelf.setInterest(mInterest);
                        getCloudBackend().update(mSelf.asEntity(), updateHandler);
                }
        }

        private void handleEndpointException(IOException e) {
//            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        }

        @Override
		public String getSelectedInterest() {
			SharedPreferences prefs = getPreferences(MODE_PRIVATE);
			String interest = prefs.getString(KEY_INTEREST, null);
			return interest;
		}

		@Override
		public void setSelectedInterest(String interest) {
			mInterest = interest;
	        SharedPreferences.Editor ed = getPreferences(MODE_PRIVATE).edit();
	        ed.putString(GeekwatchActivity.KEY_INTEREST, interest);
	        ed.commit();
			if (mSelf != null) {
				mSelf.setInterest(mInterest);
				getCloudBackend().update(mSelf.asEntity(), updateHandler);
			}
		}

}