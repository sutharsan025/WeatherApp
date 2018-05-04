package com.sutharsan.weatherapp;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;



public class WeatherFragment extends Fragment {
    Typeface weatherFont;
     
    TextView cityField;
    TextView updatedField;
    TextView detailsField;
    TextView currentTemperatureField;
    TextView weatherIcon;
	LinearLayout click;
    Handler handler;
	JSONObject details;
	JSONObject main;
	String detailstr,cityName;
	public WeatherFragment(){
        handler = new Handler();
    }
 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_weather, container, false);
        cityField = (TextView)rootView.findViewById(R.id.city_field);
        updatedField = (TextView)rootView.findViewById(R.id.updated_field);
        detailsField = (TextView)rootView.findViewById(R.id.details_field);
        currentTemperatureField = (TextView)rootView.findViewById(R.id.current_temperature_field);
        weatherIcon = (TextView)rootView.findViewById(R.id.weather_icon);
		click = (LinearLayout)rootView.findViewById(R.id.click);
		click.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showInputDialog();
			}
		});
        weatherIcon.setTypeface(weatherFont);
        return rootView;


	}
    

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    weatherFont = Typeface.createFromAsset(getActivity().getAssets(), "weather.ttf");
	    updateWeatherData(new CityPreference(getActivity()).getCity());
	}
	private void showInputDialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Weather Detailed View");
		final TextView input = new TextView(getActivity());
		input.setPadding(50,50,50,50);
		input.setText(detailstr);

		builder.setView(input);
		builder.setPositiveButton("Share", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
				sharingIntent.setType("text/plain");
				sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, cityName+"  Weather");
				sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, detailstr);
				startActivity(Intent.createChooser(sharingIntent, "Share via"));
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		builder.show();
	}
	
	private void updateWeatherData(final String city){
	    new Thread(){
	        public void run(){
	            final JSONObject json = RemoteFetch.getJSON(getActivity(), city);
	            if(json == null){
	                handler.post(new Runnable(){
	                    public void run(){
	                        Toast.makeText(getActivity(), 
	                                getActivity().getString(R.string.place_not_found), 
	                                Toast.LENGTH_LONG).show(); 
	                    }
	                });
	            } else {
	                handler.post(new Runnable(){
	                    public void run(){
	                        renderWeather(json);
	                    }
	                });
	            }               
	        }
	    }.start();
	}
	
	private void renderWeather(JSONObject json){
	    try {
	        cityField.setText(json.getString("name").toUpperCase(Locale.US) + 
	                ", " + 
	                json.getJSONObject("sys").getString("country"));
	         
	         details = json.getJSONArray("weather").getJSONObject(0);
	         main = json.getJSONObject("main");
	        /*detailsField.setText(
	                details.getString("description").toUpperCase(Locale.US) +
	                "\n" + "Humidity: " + main.getString("humidity") + "%" +
	                "\n" + "Pressure: " + main.getString("pressure") + " hPa");*/
			detailstr=details.getString("description").toUpperCase(Locale.US) +
					"\n" + "Humidity: " + main.getString("humidity") + "%" +
					"\n" + "Pressure: " + main.getString("pressure") + " hPa";
	        currentTemperatureField.setText(
	                    String.format("%.2f", main.getDouble("temp"))+ " ℃");
			cityName=json.getString("name");
	        DateFormat df = DateFormat.getDateTimeInstance();
	        String updatedOn = df.format(new Date(json.getLong("dt")*1000));
	        updatedField.setText("Last update: " + updatedOn);
	 
	        setWeatherIcon(details.getInt("id"),
	                json.getJSONObject("sys").getLong("sunrise") * 1000,
	                json.getJSONObject("sys").getLong("sunset") * 1000);
	         
	    }catch(Exception e){
	        Log.e("SimpleWeather", "One or more fields not found in the JSON data");
	    }
	}
	
	private void setWeatherIcon(int actualId, long sunrise, long sunset){
	    int id = actualId / 100;
	    String icon = "";
	    if(actualId == 800){
	        long currentTime = new Date().getTime();
	        if(currentTime>=sunrise && currentTime<sunset) {
	            icon = getActivity().getString(R.string.weather_sunny);
	        } else {
	            icon = getActivity().getString(R.string.weather_clear_night);
	        }
	    } else {
	        switch(id) {
	        case 2 : icon = getActivity().getString(R.string.weather_thunder);
	                 break;         
	        case 3 : icon = getActivity().getString(R.string.weather_drizzle);
	                 break;     
	        case 7 : icon = getActivity().getString(R.string.weather_foggy);
	                 break;
	        case 8 : icon = getActivity().getString(R.string.weather_cloudy);
	                 break;
	        case 6 : icon = getActivity().getString(R.string.weather_snowy);
	                 break;
	        case 5 : icon = getActivity().getString(R.string.weather_rainy);
	                 break;
	        }
	    }
	    weatherIcon.setText(icon);
	}
	
	public void changeCity(String city){
	    updateWeatherData(city);
	}
	
}