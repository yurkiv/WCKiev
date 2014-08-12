package com.yurkiv.wckiev;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.androidmapsextensions.ClusteringSettings;
import com.androidmapsextensions.GoogleMap;
import com.androidmapsextensions.GoogleMap.InfoWindowAdapter;
import com.androidmapsextensions.GoogleMap.OnInfoWindowClickListener;
import com.androidmapsextensions.GoogleMap.OnMapClickListener;
import com.androidmapsextensions.Marker;
import com.androidmapsextensions.GoogleMap.OnMarkerClickListener;
import com.androidmapsextensions.MarkerOptions;
import com.androidmapsextensions.SupportMapFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity implements  LocationListener {

	private static final String LOG_TAG = "WCKiev";	 
    private static final String SERVICE_URL = "http://zloysalat.github.io/test.json";
	
    private GoogleMap googleMap;    
    Location location;
    
    LinearLayout layout;    
    TextView nameTextView;
    TextView descTextView;
    TextView addressTextView;    
    RatingBar ratingBar;    
    TextView distTextView;  
    Button streetViewButton;
    Button navigateButton;
    Button reportButton;	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		layout=(LinearLayout) findViewById(R.id.pointLayout);		
		nameTextView = (TextView) findViewById(R.id.nameTextView);
        descTextView = (TextView) findViewById(R.id.descTextView);
        addressTextView = (TextView) findViewById(R.id.addressTextView);        
        ratingBar =(RatingBar) findViewById(R.id.ratingBar);        
        distTextView = (TextView) findViewById(R.id.distTextView);    
        streetViewButton = (Button) findViewById(R.id.streetViewButton);
        navigateButton = (Button) findViewById(R.id.navigateButton);
        reportButton = (Button) findViewById(R.id.reportButton);	
        
		
		// Getting Google Play availability status
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());        
        // Showing status
        if(status!=ConnectionResult.SUCCESS){ // Google Play Services are not available        	
            int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
            dialog.show();
            Log.e(LOG_TAG, "Google Play Services are not available");
        } else {  // Google Play Services are available
        	try {
                // Loading map        		
        		Log.e(LOG_TAG, "Google Play Services are available");
        		initilizeMap();    
            } catch (Exception e) {
                e.printStackTrace();
            }
        }				
	}
	
		
	private void initilizeMap() {
        if (googleMap == null) {        	
        	SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapLayout);
        	Log.e(LOG_TAG, "START");
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
            if (googleMap != null) {
                setUpMap();
            }        	 
            // check if map is created successfully or not
            if (googleMap == null) {
                Toast.makeText(getApplicationContext(),
                        "Sorry! unable to create maps", Toast.LENGTH_SHORT).show();
            }
            
            googleMap.setOnMarkerClickListener(new OnMarkerClickListener() {
				@Override
				public boolean onMarkerClick(Marker marker) {						
					LatLng latLng=marker.getPosition();
					double latitude=latLng.latitude-0.0005;
					double longitude=latLng.longitude;
					LatLng newlatLng=new LatLng(latitude, longitude);
					
					if(marker.isCluster()){
						googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newlatLng, googleMap.getCameraPosition().zoom+1));					
					} else {
						googleMap.animateCamera(CameraUpdateFactory.newLatLng(newlatLng));							
						initPointInfo(marker);
						layout.setVisibility(View.VISIBLE);
					}											
					return true;
				}
			});
            
            googleMap.setOnMapClickListener(new OnMapClickListener() {
				
				@Override
				public void onMapClick(LatLng position) {
					layout.setVisibility(View.GONE);
					
				}
			});
        }        
    }
	
	private void initPointInfo(final Marker marker){
		// Getting the position from the marker
        LatLng latLng = marker.getPosition();
        nameTextView.setText(marker.getTitle());
        descTextView.setText(marker.getSnippet());
        
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
        final String address = filterAddress;
        
        String[] data=marker.getData();
        ratingBar.setRating(Float.parseFloat(data[1]));
        
        float[] currentDistance = new float[1];
        Location.distanceBetween(location.getLatitude(), location.getLongitude(),
        		latLng.latitude, latLng.longitude, currentDistance);
        distTextView.setText(Float.toString(currentDistance[0]));
        
        final String sourceLatitude=Double.toString(location.getLatitude());
        final String sourceLongitude=Double.toString(location.getLongitude());        
        final String destLatitude=Double.toString(latLng.latitude);
        final String destLongitude=Double.toString(latLng.longitude);
        
        navigateButton.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				String navigationUrl="http://maps.google.com/maps?saddr="+sourceLatitude+","+sourceLongitude+
						"&daddr="+destLatitude+","+destLongitude;
				Intent navIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(navigationUrl));
				startActivity(navIntent);
			}
		});
        
        streetViewButton.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				String geoUriString = "google.streetview:cbll="+destLatitude+","+destLongitude+"&cbp=1,99.56,,1,2.0&mz=19";
				Intent streetView = new Intent(android.content.Intent.ACTION_VIEW,Uri.parse(geoUriString));				
				try {
					startActivity(streetView);
                } catch (Exception e) {               	
                    Toast toast = Toast.makeText(getApplicationContext(), "Error Loading StreetView, Please Install Google StreetView",
                            Toast.LENGTH_SHORT);
                    toast.show();
                }
				
			}
		});
        
        reportButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String report="Report Missing \n" +
						"\n Name: " + marker.getTitle()+
						"\n Adress: " + address+
						"\n lat: " + destLatitude+
						"\n lng:" + destLongitude;
				Intent i = new Intent(Intent.ACTION_SEND);
				i.setType("message/rfc822");
				i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"recipient@example.com"});
				i.putExtra(Intent.EXTRA_SUBJECT, "Report Point");
				i.putExtra(Intent.EXTRA_TEXT   , report);
				try {
				    startActivity(Intent.createChooser(i, "Send mail..."));
				} catch (android.content.ActivityNotFoundException ex) {
				    Toast.makeText(MainActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
				}
				
			}
		});
	}
	
	
	
	private void setUpMap() {
        // Retrieve the city data from the web service
        // In a worker thread since it's a network operation.
        new Thread(new Runnable() {
            public void run() {
                try {
                    retrieveAndAddCities();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Cannot retrive cities", e);
                    return;
                }
            }
        }).start();
    }
 
	protected void retrieveAndAddCities() throws IOException {
        HttpURLConnection conn = null;
        final StringBuilder json = new StringBuilder();
        try {
            // Connect to the web service
            URL url = new URL(SERVICE_URL);
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());
 
            // Read the JSON data into the StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                json.append(buff, 0, read);
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error connecting to service", e);
            //throw new IOException("Error connecting to service", e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
 
        // Create markers for the city data.
        // Must run this on the UI thread since it's a UI operation.
        runOnUiThread(new Runnable() {
            public void run() {
                try {
                    createMarkersFromJson(json.toString());
                	//readItems(json.toString());
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Error processing JSON", e);
                }
            }
        });
    }
	
//	private void readItems(String json) throws JSONException {
//        
//        List<Point> items = new PointReader().read(json);
//        for (int i = 0; i < items.size(); i++) {
//            //double offset = i / 60d;
//            for (Point item : items) {
//                //LatLng position = item.getPosition();
//                //double lat = position.latitude + offset;
//                //double lng = position.longitude + offset;
//                //Point offsetItem = new Point(lat, lng);
//                mClusterManager.addItem(item);
//            }
//        }
//    }
	
	void createMarkersFromJson(String json) throws JSONException {
        // De-serialize the JSON string into an array of city objects
        JSONArray jsonArray = new JSONArray(json);
        for (int i = 0; i < jsonArray.length(); i++) {
            // Create a marker for each city in the JSON data.
            JSONObject jsonObj = jsonArray.getJSONObject(i);
            String[] objData=new String[2];
            objData[0]=jsonObj.getString("name");
            objData[1]=Integer.toString(jsonObj.getInt("comfort"));
            googleMap.addMarker(new MarkerOptions()
                .title(jsonObj.getString("name"))
                .snippet(Integer.toString(jsonObj.getInt("comfort")))
                .position(new LatLng(
                        jsonObj.getDouble("lat"),
                        jsonObj.getDouble("lng")
                 ))
                 .data(objData)
            );
        }        
    }
	
    @Override
    protected void onResume() {
        super.onResume();
        initilizeMap();
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub
		
	}
	
	

}
