package com.example.android.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.v("On Create"," DONE");

        //inflate a layout and associate to the activity
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {

            //prosthiki fragment element dinamika sto XML layout element me id container (tou mainactivity)
            //orizw to antikeimeno tipou fragment pou prosthetw sto activity
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ForecastFragment())
                    .commit();
        }
    }

    //METHODOI GIA TA MENU ITEMS
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent=new Intent(this,SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        else if(id==R.id.action_location){

            openprefferedlocationInMap();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //METHODOS GIA EMFANISI LOCATION MESW IMPLICIT EVENT KAI EKMETALEUSI PLIROFORIAS STA SHAREDPREFERENCES
    private void openprefferedlocationInMap(){

        //reference to sharedPreferences
        SharedPreferences sharedPrefs= PreferenceManager.getDefaultSharedPreferences(this);

        //reference to specific preference
        String location=sharedPrefs.getString(getString(R.string.pref_location_key),getString((R.string.pref_location_default)));

        //build Uri
        //to geo einai scheme to opoio to kanei handle to katallilo app (google maps)
        //
        Uri geoLocation=Uri.parse("geo:0,0?").buildUpon()
                .appendQueryParameter("q",location)
                .build();

        //Implicit Intent for Google Maps
        Intent intent=new Intent(Intent.ACTION_VIEW);

        Log.v("Uri",location.toString());

        //setData used to point the location of a file (mono Uri perimenei san argument)
        //putExtra adds simple Data types
        intent.setData(geoLocation);

        //elegxos ean iparxei app stin siskeui na treksei auto to implicit intent
        if(intent.resolveActivity(getPackageManager())!=null){
            startActivity(intent);
        }
        else{
            Log.e("MainClass Error", "Couldn't call " + location + ", no receiving apps installed!");
        }

    }

    @Override
    public void onStart() {
        super.onStart();

        Log.v("On Start"," DONE");
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.v("On Resume"," DONE");
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.v("On Pause"," DONE");
    }

    @Override
    public void onStop() {
        super.onStop();

        Log.v("On Stop"," DONE");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.v("On Destroy"," DONE");
    }

}

