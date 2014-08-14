package com.yurkiv.wckiev;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.w3c.dom.Document;

import com.androidmapsextensions.ClusteringSettings;
import com.androidmapsextensions.GoogleMap;
import com.androidmapsextensions.GoogleMap.OnMapClickListener;
import com.androidmapsextensions.Marker;
import com.androidmapsextensions.MarkerOptions;
import com.androidmapsextensions.Polyline;
import com.androidmapsextensions.PolylineOptions;
import com.androidmapsextensions.SupportMapFragment;
import com.androidmapsextensions.GoogleMap.InfoWindowAdapter;
import com.androidmapsextensions.GoogleMap.OnInfoWindowClickListener;
import com.androidmapsextensions.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.suredigit.inappfeedback.FeedbackDialog;
import com.suredigit.inappfeedback.FeedbackSettings;

import android.content.Intent;
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
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

public class AddPointActivity extends ActionBarActivity implements  LocationListener{

	private GoogleMap googleMap;
	private Location location;
	private LatLng newPointLatLng;
	TextView  newAddressTextView;
	FeedbackDialog feedBack;
	EditText nameEditText;
	EditText descEditText;
	RatingBar ratingBar;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_point);
		
				
		final LinearLayout layout=(LinearLayout) findViewById(R.id.addPointLayout);
		
		nameEditText=(EditText) findViewById(R.id.nameEditText);
		descEditText=(EditText) findViewById(R.id.descEditText);
		ratingBar=(RatingBar) findViewById(R.id.addRatingBar);
		newAddressTextView=(TextView) findViewById(R.id.newAddressTextView);  
        Button sendNewPointButton = (Button) findViewById(R.id.sendNewPointButton);
        initilizeMap();        
        sendNewPointButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sendNewPoint();
			}
		});        
	}
	
	private void sendNewPoint(){
		String senddata="New Point: \n" + 
				" Name: " + nameEditText.getText().toString() +
				"\n Discription: " + descEditText.getText().toString() +
				"\n Address: " + newAddressTextView.getText().toString() +
				"\n Rate: " + ratingBar.getRating() +
				"\n Lat: " +newPointLatLng.latitude+
				"\n Lng: "+newPointLatLng.longitude;
		
		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("message/rfc822");
		i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"jurkiw.misha@gmail.com"});
		i.putExtra(Intent.EXTRA_SUBJECT, "New Point");
		i.putExtra(Intent.EXTRA_TEXT   , senddata);
		try {
		    startActivity(Intent.createChooser(i, "Send mail..."));
		} catch (android.content.ActivityNotFoundException ex) {
		    Toast.makeText(AddPointActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
		}
		finish();
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
                  
            if (googleMap == null) {
                Toast.makeText(getApplicationContext(),
                        "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                        .show();
            }            
            newPointLatLng=new LatLng(location.getLatitude(), location.getLongitude());
            googleMap.addMarker(new MarkerOptions()
				.position(newPointLatLng)							
				.icon(BitmapDescriptorFactory
						.fromResource(R.drawable.ic_icon_toilets)));
            
			googleMap.setOnMapClickListener(new OnMapClickListener() {
				@Override				
				public void onMapClick(LatLng position) {
					googleMap.clear();
					googleMap.addMarker(new MarkerOptions()
							.position(position)							
							.icon(BitmapDescriptorFactory
									.fromResource(R.drawable.ic_icon_toilets)));
					newPointLatLng=position;
					String filterAddress = "";
			        Geocoder geoCoder = new Geocoder(getBaseContext(), Locale.getDefault());
			        try {
			            List<Address> addresses = geoCoder.getFromLocation(newPointLatLng.latitude, newPointLatLng.longitude, 1);
			            if (addresses.size() > 0) {                            
			            	filterAddress=addresses.get(0).getAddressLine(0);
			            }
			        }catch (IOException ex) {        
			            ex.printStackTrace();
			        }catch (Exception e2) {
			            // TODO: handle exception
			            e2.printStackTrace();
			        } 
			        newAddressTextView.setText(filterAddress);
				}
			}); 
        }        
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {		
			return true;
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
