package com.example.weatherapp;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import java.io.UnsupportedEncodingException;
import java.util.Locale;

public class CityFragment extends Fragment {

    // API call parts
    public static String API_KEY = "d511cde0afdd23d845c56654dd26cd75";
    public static String API_LINK = "http://api.openweathermap.org/data/2.5/weather";
    public static String API_UNIT = "units=metric";
    public static String API_LANG = "lang=hu";

    // references
    private EditText etCity;
    private Button btnSearch;
    private TextView city, description, temperature;

    private RequestQueue requestQueue;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_city, container, false);

        etCity = (EditText) view.findViewById(R.id.edit_city);
        btnSearch = (Button) view.findViewById(R.id.button_search);
        city = (TextView) view.findViewById(R.id.tv_city);
        description = (TextView) view.findViewById(R.id.tv_description);
        temperature = (TextView) view.findViewById(R.id.tv_temperature);

        requestQueue = Volley.newRequestQueue(getActivity());

        etCity.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateEditText(etCity, s);
            }
        });

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String ct = etCity.getText().toString();

                if(ct  == ""){
                    Toast.makeText(getContext(), getString(R.string.select_city), Toast.LENGTH_SHORT).show();
                } else {

                    String url = API_LINK + "?q=" + ct + "&" + API_UNIT + "&appid=" + API_KEY;

                    if(Locale.getDefault().getLanguage().equals("hu"))
                        url += "&" + API_LANG;

                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                            (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                                @Override
                                public void onResponse(JSONObject response) {
                                    try {

                                        if (response.getString("name").equals("Earth") || response.getString("name").equals("")) {
                                            city.setText("Unknown");
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
                                    parseVolleyError(error);
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

    public void parseVolleyError(VolleyError error) {

        if (error == null || error.networkResponse == null)
            return;

        String body;
        final String statusCode = String.valueOf(error.networkResponse.statusCode);

        //get response body and parse with appropriate encoding
        try {
            body = new String(error.networkResponse.data,"UTF-8");
            JSONObject jsonObject = new JSONObject(body);
            Toast.makeText(getContext(), jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
        } catch (UnsupportedEncodingException e) {
            // exception
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
