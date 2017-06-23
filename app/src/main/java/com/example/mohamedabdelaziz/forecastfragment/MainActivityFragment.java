package com.example.mohamedabdelaziz.forecastfragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.opengl.EGLExt;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
public class MainActivityFragment extends Fragment {
    ArrayAdapter mForecastAdapter;
    public MainActivityFragment() {


    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {


        inflater.inflate(R.menu.menu_main,menu);

    }
    private void updateWeather() {
        FetchWeatherTask weatherTask = new FetchWeatherTask();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String location = prefs.getString(getString(R.string.pref_location_key),
                getString(R.string.pref_location_default));
        weatherTask.execute(location);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {

            startActivity(new Intent(getContext(),SettingsActivity.class));

            return true;
        }
        if (id == R.id.refresh) {

      updateWeather();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        List<String> weekForecast = new ArrayList<String>();
        mForecastAdapter =
                new ArrayAdapter<String>(getContext(), android.R.layout.simple_expandable_list_item_1,
                        weekForecast);
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        final ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mForecastAdapter);



      listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
          @Override
          public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
              String data = listView.getItemAtPosition(i).toString();
             Intent intent = new Intent(getContext(),Detail_Activity.class) ;
              intent.putExtra("data",data) ;

             startActivity(intent);
          }
      });
        setHasOptionsMenu(true);
        return rootView;
    }
    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {
        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
        private String getReadableDateString(long time) {
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(time);
        }
        private String formatHighLows(double high, double low , String unitetype) {
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);
            if(unitetype.equals("imperial"))
            {
                high =(high*1.80)+32 ;
                low =(low*1.80)+32 ;
            }
            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }
        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
                throws JSONException {
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DESCRIPTION = "main";
            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);
            Time dayTime = new Time();
            dayTime.setToNow();
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);
            dayTime = new Time();
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext()) ;
            String unite = sharedPreferences.getString(getString(R.string.pref_units_key),getString(R.string.pref_units_metric)) ;

            String[] resultStrs = new String[numDays];
            for (int i = 0; i < weatherArray.length(); i++) {
                String day;
                String description;
                String highAndLow;
                JSONObject dayForecast = weatherArray.getJSONObject(i);
                long dateTime;
                dateTime = dayTime.setJulianDay(julianStartDay + i);
                day = getReadableDateString(dateTime);
                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);
                highAndLow = formatHighLows(high, low ,unite);
                resultStrs[i] = day + " - " + description + " - " + highAndLow;
            }
            for (String s : resultStrs) {
                Log.v(LOG_TAG, "Forecast entry: " + s);
            }
            return resultStrs;
        }
        @Override
        protected String[] doInBackground(String... params) {
            if (params.length == 0) {
                return null;
            }
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String forecastJsonStr = null;
            String format = "json";
            String units = "metric";
            int numDays = 7;
            try {
                final String FORECAST_BASE_URL =
                        "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String QUERY_PARAM = "q";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";
                final String APPID_PARAM = "APPID";
                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, params[0])
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(UNITS_PARAM, units)
                        .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                        .appendQueryParameter(APPID_PARAM, "804f526daffcb2784584800c987d5447").build();
                URL url = new URL(builtUri.toString());
                Log.v(LOG_TAG, "Built URI " + builtUri.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }
                if (buffer.length() == 0) {
                    return null;
                }
                forecastJsonStr = buffer.toString();
                Log.v(LOG_TAG, "Forecast string: " + forecastJsonStr);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            try {
                return getWeatherDataFromJson(forecastJsonStr, numDays);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(String[] result) {
            if (result != null) {
                mForecastAdapter.clear();
                for (String dayForecastStr : result) {
                    mForecastAdapter.add(dayForecastStr);
                }
            }
        }
    }

}

























