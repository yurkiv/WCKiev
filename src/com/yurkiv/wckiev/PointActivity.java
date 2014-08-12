package com.yurkiv.wckiev;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.w3c.dom.Document;

import com.androidmapsextensions.ClusteringSettings;
import com.androidmapsextensions.GoogleMap;
import com.androidmapsextensions.Marker;
import com.androidmapsextensions.MarkerOptions;
import com.androidmapsextensions.Polyline;
import com.androidmapsextensions.PolylineOptions;
import com.androidmapsextensions.SupportMapFragment;
import com.androidmapsextensions.GoogleMap.InfoWindowAdapter;
import com.androidmapsextensions.GoogleMap.OnInfoWindowClickListener;
import com.androidmapsextensions.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;

import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

public class PointActivity extends ActionBarActivity implements  LocationListener{

	private GoogleMap googleMap;
	Location location;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_point);
		
		initilizeMap();
		
		final LinearLayout layout=(LinearLayout) findViewById(R.id.pointLayout);
		
        TextView nameTextView = (TextView) findViewById(R.id.nameTextView);
        TextView descTextView = (TextView) findViewById(R.id.descTextView);
        TextView addressTextView = (TextView) findViewById(R.id.addressTextView);
        
        RatingBar ratingBar =(RatingBar) findViewById(R.id.ratingBar);
        
        TextView distTextView = (TextView) findViewById(R.id.distTextView);
        
        Button streetViewButton = (Button) findViewById(R.id.streetViewButton);
        Button navigateButton = (Button) findViewById(R.id.navigateButton);
        Button reportButton = (Button) findViewById(R.id.reportButton);
		
        Bundle extras = getIntent().getExtras();
    	if (extras != null) {    	    
    	    double latitude = extras.getDouble("lat");
    		double longitude = extras.getDouble("lng");
    		String name=extras.getString("name");
    		String snippet=extras.getString("snippet");
    		Float rating=extras.getFloat("rating");
    		String desc=extras.getString("desc");
    		
    		nameTextView.setText(name);
            descTextView.setText(desc);
            
            LatLng latLng=new LatLng(latitude, longitude);
            String filterAddress = "";
            Geocoder geoCoder = new Geocoder(getBaseContext(), Locale.getDefault());
            try {
                List<Address> addresses = geoCoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                if (addresses.size() > 0) {                            
                	filterAddress=addresses.get(0).getAddressLine(0);
                }
            }catch (IOException ex) {        
                ex.printStackTrace();
            }catch (Exception e2) {
                // TODO: handle exception
                e2.printStackTrace();
            } 
            addressTextView.setText(filterAddress);
            
            ratingBar.setRating(rating);
            
            float[] currentDistance = new float[1];
            Location.distanceBetween(location.getLatitude(), location.getLongitude(),
            		latLng.latitude, latLng.longitude, currentDistance);
            distTextView.setText(Float.toString(currentDistance[0]));
            
            if (googleMap != null) {
            	googleMap.addMarker(new MarkerOptions()
    	            .title(name)
    	            .snippet(snippet)
    	            .position(new LatLng(latitude, longitude))    	             
            	);
            	
            	GMapV2Direction md = new GMapV2Direction();
            	LatLng sourcePosition=new LatLng(location.getLatitude(), location.getLongitude()); 
            	LatLng destPosition=new LatLng(latitude, longitude);            	
            	Document doc = md.getDocument(sourcePosition, destPosition,
            	                    GMapV2Direction.MODE_WALKING);

            	ArrayList<LatLng> directionPoint = md.getDirection(doc);
				PolylineOptions rectLine = new PolylineOptions().width(3)
						.color(Color.RED);

				for (int i = 0; i < directionPoint.size(); i++) {
					rectLine.add(directionPoint.get(i));
				}
				Polyline polylin = googleMap.addPolyline(rectLine);
            	            
				googleMap.setOnMarkerClickListener(new OnMarkerClickListener() {
					@Override
					public boolean onMarkerClick(Marker marker) {	
						
						
							googleMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));	
							
							layout.setVisibility(View.VISIBLE);
							
																	
						return true;
					}
				});
            }
    	}
	}
	
	private void initilizeMap() {
        if (googleMap == null) {
        	
        	SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        	googleMap = mapFragment.getExtendedMap();
        	
        	// Enabling MyLocation Layer of Google Map
            googleMap.setMyLocationEnabled(true);
 
            // Getting LocationManager object from System Service LOCATION_SERVICE
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
 
            // Creating a criteria object to retrieve provider
            Criteria criteria = new Criteria();
 
            // Getting the name of the best provider
            String provider = locationManager.getBestProvider(criteria, true);
 
            // Getting Current Location
            location = locationManager.getLastKnownLocation(provider);
 
            if(location!=null){
                onLocationChanged(location);
            }
            locationManager.requestLocationUpdates(provider, 300000, 0, this);
        	
            //clustering
            googleMap.setClustering(new ClusteringSettings().addMarkersDynamically(true));
                        
            // check if map is created successfully or not
            if (googleMap == null) {
                Toast.makeText(getApplicationContext(),
                        "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                        .show();
            }
            
        }        
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.point, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onLocationChanged(Location location) {
		// Getting latitude of the current location
        double latitude = location.getLatitude();
 
        // Getting longitude of the current location
        double longitude = location.getLongitude();
 
        // Creating a LatLng object for the current location
        LatLng latLng = new LatLng(latitude, longitude);
 
        // Showing the current location in Google Map
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
 
        // Zoom in the Google Map
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
		
	}
	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
}
