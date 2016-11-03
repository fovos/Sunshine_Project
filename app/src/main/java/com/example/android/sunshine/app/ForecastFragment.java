package com.example.android.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
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

/**
 * A fragment containing a simple view.Arxika tin eixa mesa stin MainActivity alla meta tin metefera eksw se ksexwristo arxeio
 * Apotelei to fragment to opoio mporw na to valw se opoio Activity thelw
 */
public class ForecastFragment extends Fragment {

    //FIELDS
    ArrayAdapter<String> mForecastAdapter;      //set global variable to be accessed from the thread
    ListView lstview;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //prwta ekteleitai i onCreate ki epeita i onCreateView

        //add this line in order for this fragment to handle menu events and show the extra menuitems
        //EXTRA MENU ITEMS POWERED BY THIS FRAGMENT
        setHasOptionsMenu(true);

    }

    //EXTRA MENU ITEMS ADDED FROM THIS FRAGMENT
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the detail MENU ITEMS to the menu
        inflater.inflate(R.menu.detail, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            updateWeather();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //METHODOS GIA UPDATE LISTVIEW MESV PLIROFORIAS SHAREDPREFERENCES
    private void updateWeather() {
        //apoktw prosvasi sta preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        //pernw to location key pou exw apothikeusei sta settings alliws to default an den vgalei tpt
        //PRWTO ARG EINAI TO KEY
        //DEYTERO ARG EINAI DEFAULT VALUE
        //EPISTREFEI TO VALUE GIA TO SIGKEKRIMENO KEY
        String location_id = prefs.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
        String metric_system = prefs.getString(getString(R.string.pref_temp_key), getString(R.string.pref_temp_default));
        new FetchWeatherTask().execute(location_id, metric_system);   //api location_id, metric_system
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //inflate to VIew tou fragment XML arxeiou sto placeholder apo panw
        //converts an XML layout file into the corresponding Viewgroups and widgets
        //inflate the fragment_main XML to the container ean to trito arg einai true
        //alliws tha to perasoume argotera
        //inflate layout and associate to the fragment via the return statement
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        //create the adapter for the listview
        //Adapters translate a data source into views for a ListView to display
        //prwto arg einai to context tou app
        //deutero arg einai to layout file
        //trito arg einai to item pou tha ginei host to kathe listitem
        //tetarto arg einai to arraylist (an eixa data tha evaza auto, alliws vazw new arraylist giati tha
        mForecastAdapter = new ArrayAdapter<>(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textview, new ArrayList<String>());


        //reference to Listview element
        //to rootview einai tou inflater pou orisame parapanw
        lstview = (ListView) rootView.findViewById(R.id.listview_forecast);

        //set the adapter to that listview element
        lstview.setAdapter(mForecastAdapter);

        lstview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                //to i einai to position
                //pernw to item sto i position kai to dinw se string variable
                String forecast = mForecastAdapter.getItem(i);

                //epeidi eimai se fragment to context einai sto activity, ara oxi this alla getActivity()
                //Toast.makeText(getActivity(),forecast,Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT, forecast);
                startActivity(intent);
            }
        });
        //associate the layout to the fragment
        return rootView;
    }


    //Background thread to fetch weather json results from Web
    //prwto arg ti mporw na perasw san argument -- arikti sxesi me to arg sto doinbackground()
    //deutero arg to epistrefei to thread KATA TIN DIARKEIA (onporgressupdate)
    //trito arg to epistrefei to thread STO TELOS (onpostexecute)
    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        //FIELDS
        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();   //shows class name in Logcat

        private String getReadableDateString(long time) {
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(time);
        }

        /**
         * Prepare the weather high/lows for presentation.
         */

        private String formatHighLows(double high, double low, String unitType) {

            if (unitType.equals(getString(R.string.pref_units_imperial))) {
                high = (high * 1.8) + 32;
                low = (low * 1.8) + 32;
            } else if (!unitType.equals(getString(R.string.pref_units_metric))) {
                Log.d(LOG_TAG, "Unit type not found: " + unitType);
            }

            // For presentation, assume the user doesn't care about tenths of a degree.
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }

        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "temp_max";
            final String OWM_MIN = "temp_min";
            final String OWM_DESCRIPTION = "main";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);      //metatropi tou string se Json Object
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);   //fetch the list object from JSON (7 objects)

            // OWM returns daily forecasts based upon the local time of the city that is being
            // asked for, which means that we need to know the GMT offset to translate this data
            // properly.

            // Since this data is also sent in-order and the first day is always the
            // current day, we're going to take advantage of that to get a nice
            // normalized UTC date for all of our weather.

            Time dayTime = new Time();
            dayTime.setToNow();

            // we start at the day returned by local time. Otherwise this is a mess.
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            // now we work exclusively in UTC
            dayTime = new Time();

            String[] resultStrs = new String[numDays];

            // Data is fetched in Celsius by default.
            // If user prefers to see in Fahrenheit, convert the values here.
            // We do this rather than fetching in Fahrenheit so that the user can
            // change this option without us having to re-fetch the data once
            // we start storing the values in a database.
            SharedPreferences sharedPrefs =PreferenceManager.getDefaultSharedPreferences(getActivity());

            String unitType = sharedPrefs.getString(getString(R.string.pref_temp_key),getString(R.string.pref_units_metric));


            for (int i = 0; i < weatherArray.length(); i++) {
                // For now, using the format "Day, description, hi/low"
                String day;
                String description;
                String highAndLow;

                // Get the nth list item in weatherArray array
                JSONObject dayForecast = weatherArray.getJSONObject(i);                                 //list[0],list[1],...list[7]

                // The date/time is returned as a long.  We need to convert that
                // into something human-readable, since most people won't read "1400356800" as
                // "this saturday".
                long dateTime;
                // Cheating to convert this to UTC time, which is what we want anyhow
                dateTime = dayTime.setJulianDay(julianStartDay + i);
                day = getReadableDateString(dateTime);

                // description is in a child array called "weather", which is 1 element long.
                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);      //weather->id
                description = weatherObject.getString(OWM_DESCRIPTION);                                 //weather->main

                // Temperatures are in a child object called "temp".  Try not to name variables
                // "temp" when working with temperature.  It confuses everybody.
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_DESCRIPTION);              //list[i].main[]
                double high = temperatureObject.getDouble(OWM_MAX);                                     //list[i].main["temp_max"]
                double low = temperatureObject.getDouble(OWM_MIN);                                      //list[i].main["temp_min"]

                highAndLow = formatHighLows(high, low,unitType);
                resultStrs[i] = day + " - " + description + " - " + highAndLow;                         //table saving the created strings with weather output
            }


            for (String s : resultStrs) {
                Log.v(LOG_TAG, "Forecast entry: " + s);
            }

            return resultStrs;

        }

        //to String ... params prepei ws tipos na simfwnei me to prwto argument sto asyncTask
        //to String[] prepei na simfwnei me
        @Override
        protected String[] doInBackground(String... params) {

            // If there's no zip code, there's nothing to look up.  Verify size of params.
            if (params.length == 0) {
                return null;
            }

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;
            String format = "json";
            int numDays = 7;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast

                final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/city?";
                final String QUERY_PARAM = "id";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";
                final String APPID_PARAM = "APPID";

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, params[0])       //edw pernaw tin prwti (kai monadiki) parametro
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(UNITS_PARAM, params[1])
                        .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                        .appendQueryParameter(APPID_PARAM, BuildConfig.OPEN_WEATHER_MAP_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, "Built URI " + builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();

                Log.v(LOG_TAG, "Forecast JSON String: " + forecastJsonStr);
            } catch (IOException e) {
                Log.e("ForecastFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("ForecastFragment", "Error closing stream", e);
                    }
                }
            }

            //parsing the data acquired by the http request into Json format and handle them appropriately
            try {
                //auto tha einai to apotelesma tou thread!!!!
                //auto tha paei stin onPostExecute
                //diladi to String[] me ta dedomena
                return getWeatherDataFromJson(forecastJsonStr, numDays);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            // This will only happen if there was an error getting or parsing the forecast.

            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {      //to argument tha prepei na simfwnei me to trito argument tou asycTask.
            //ektelesi sto main thread
            if (result != null) {
                mForecastAdapter.clear();   //clear the adapter from previous data
                //den xreiazetai neo adapter
                //den xreiazetai na sindesw adapter me UI elements ksana, diladi na ton arxikopoihsw
                //xreiazetai na ton gemisw me nea dedomena MONO
                //i arxikopoihsh egine sto main thread (onCreate) tou fragment kai mas arkei

                for (String dayforecastStr : result) {
                    mForecastAdapter.add(dayforecastStr);
                }

                //mForecastAdapter.addAll(result);      //more efficient code.

                //set the adapter to that listview element
                //fortwnei to listview me items
                lstview.setAdapter(mForecastAdapter);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        //opote ekkinei to fragment ginetai updateweather.
        updateWeather();
    }
}
