package com.example.weatherapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class CoordinatesFragment extends Fragment implements LocationListener {

    // API call parts
    public static String API_KEY = "d511cde0afdd23d845c56654dd26cd75";
    public static String API_LINK = "http://api.openweathermap.org/data/2.5/weather";
    public static String API_UNIT = "units=metric";
    public static String API_LANG = "lang=hu";

    // references
    private EditText etLatitude, etLongitude;
    private Button btnSearch;
    ImageButton btnLocate;
    private TextView city, description, temperature;

    private RequestQueue requestQueue;

    private LocationManager locationManager;
    String provider;

    static double lat, lng;

    int MY_PERMISSION = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_coordinates, container, false);

        etLatitude = (EditText) view.findViewById(R.id.edit_latitude);
        etLongitude = (EditText) view.findViewById(R.id.edit_longitude);
        btnLocate = (ImageButton) view.findViewById(R.id.button_locate);
        btnSearch = (Button) view.findViewById(R.id.button_search);
        city = (TextView) view.findViewById(R.id.tv_city);
        description = (TextView) view.findViewById(R.id.tv_description);
        temperature = (TextView) view.findViewById(R.id.tv_temperature);

        requestQueue = Volley.newRequestQueue(getActivity());

        etLatitude.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateEditText(etLatitude, s);
            }
        });

        etLongitude.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateEditText(etLongitude, s);
            }
        });

        btnLocate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                provider = locationManager.getBestProvider(new Criteria(), false);
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(getActivity(), new String[]{
                            Manifest.permission.INTERNET,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },MY_PERMISSION);

                    return;
                }
                Location location = locationManager.getLastKnownLocation(provider);
                etLatitude.setText(Double.toString(location.getLatitude()));
                etLongitude.setText(Double.toString(location.getLongitude()));

                closeKeyboard();
            }
        });

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String lat = etLatitude.getText().toString();
                String lng = etLongitude.getText().toString();

                if(lat.equals("") || lng.equals("")){
                    Toast.makeText(getContext(), getString(R.string.toast_fillboth), Toast.LENGTH_SHORT).show();
                } else if( (Double.valueOf(lat) < -90 || Double.valueOf(lat) > 90) && !(Double.valueOf(lng) < -180 || Double.valueOf(lng) > 180)) {
                    Toast.makeText(getContext(), getString(R.string.toast_invalid_lat), Toast.LENGTH_SHORT).show();
                } else if( !(Double.valueOf(lat) < -90 || Double.valueOf(lat) > 90) && (Double.valueOf(lng) < -180 || Double.valueOf(lng) > 180)) {
                    Toast.makeText(getContext(), getString(R.string.toast_invalid_lng), Toast.LENGTH_SHORT).show();
                } else if( (Double.valueOf(lat) < -90 || Double.valueOf(lat) > 90) && (Double.valueOf(lng) < -180 || Double.valueOf(lng) > 180)) {
                    Toast.makeText(getContext(), getString(R.string.toast_invalid_values), Toast.LENGTH_SHORT).show();
                } else {

                    String url = API_LINK + "?lat=" + lat + "&lon=" + lng + "&" + API_UNIT + "&appid=" + API_KEY;

                    if(Locale.getDefault().getLanguage().equals("hu"))
                        url += "&" + API_LANG;

                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                            (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        if (response.getString("name").equals("Earth") || response.getString("name").equals("")) {
                                            city.setText(getString(R.string.unknown));
                                        } else {
                                            String temp = "";
                                            if(response.getJSONObject("sys").has("country")){
                                                temp = ", " + response.getJSONObject("sys").getString("country");
                                            }
                                            city.setText(response.getString("name") + temp);
                                        }

                                        description.setText(response.getJSONArray("weather").getJSONObject(0).getString("description"));
                                        temperature.setText(response.getJSONObject("main").getString("temp") + "°C");

                                        city.setVisibility(View.VISIBLE);
                                        description.setVisibility(View.VISIBLE);
                                        temperature.setVisibility(View.VISIBLE);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, new Response.ErrorListener() {

                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    System.out.println("ERROR OCCURED");
                                }
                            });
                    requestQueue.add(jsonObjectRequest);

                }
                closeKeyboard();
            }
        });

        return view;
    }

    private void closeKeyboard() {
        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }

    private void updateEditText(EditText et, CharSequence s) {

        Drawable[] compoundDrawables = et.getCompoundDrawables();
        Drawable drawableLeft = compoundDrawables[0].mutate();

        if( s.length() != 0 ) {
            drawableLeft.setColorFilter(new PorterDuffColorFilter(ResourcesCompat.getColor(getResources(), R.color.focused, null), PorterDuff.Mode.SRC_IN));
            et.setBackgroundResource(R.drawable.edittext_filled_bg);
        } else {
            drawableLeft.setColorFilter(new PorterDuffColorFilter(ResourcesCompat.getColor(getResources(), R.color.inactive, null), PorterDuff.Mode.SRC_IN));
            et.setBackgroundResource(R.drawable.edittext_bg);
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        lng = location.getLongitude();
        lat = location.getLatitude();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}

