package com.example.android.sunshine.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.support.v7.widget.ShareActionProvider;

/**
 * Prokeitai gia to activity to opoio emfanizei tin prognwsi gia sigkekrimeni mera.
 * Proerxetai apo to klik pou kanie o xristis se sigkekrimeno list item toy FOrecastFragment
 * Perilamvanei Fragment.
 * Ylopoiei SHare Action Provider.
 */


public class DetailActivity extends ActionBarActivity {

    //FIELDS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        getMenuInflater().inflate(R.menu.detail, menu);

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

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.Apotelei fragment
     */
    public static class DetailFragment extends Fragment {

        //FIELDS
        private ShareActionProvider mShareActionProvider;
        private static final String FORECAST_SHARE_HASHTAG="#SunshineApp";
        private String mFOrecastStr;

        public DetailFragment() {
            //tin entoli auti tha mporousa na tin entaksw kai stin onCreate()
            setHasOptionsMenu(true);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            // Inflate the menu; this adds items to the action bar if it is present.
            inflater.inflate(R.menu.detailfragment, menu);

            //Retrieve the share menu item
            MenuItem menuItem=menu.findItem(R.id.menu_item_share);

            //Get the provider and hold on to it  to set/change  the share intent
            // Fetch reference to the share action provider
            mShareActionProvider=(ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

            if(mShareActionProvider!=null){
                //ShareActionProvider does not respond to onOptionsItemSelected() events, so you set the share action provider as soon as it is possible.
                mShareActionProvider.setShareIntent(createshareForecastIntent());
            }
            else{
                Log.e("Error","Share Action Provider is null?");
            }

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            //inflating the fragment layout to Mainactivity container (reference and associate)
            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

            //sos kanonika se activity den xreiazomai to getactivity() dioti eimai idi se activity
            //edw mesa omws prepei na xrisimopoihsw auti ti methodo dioti prepei na anaferthw sto activity
            Intent intent=getActivity().getIntent();
            if(intent!=null && intent.hasExtra(Intent.EXTRA_TEXT)){
                mFOrecastStr=intent.getStringExtra(Intent.EXTRA_TEXT);
                ((TextView)rootView.findViewById(R.id.details_Text)).setText(mFOrecastStr);
            }

            return rootView;
        }

        private Intent createshareForecastIntent(){
            Intent sharedintent= new Intent(Intent.ACTION_SEND);
            //i epomeni entoli simantiki. SImainei oti to app pou tha anoiksei gia na kanei handle to intent
            //den tha prosthesei to activity sto stack. An ginotan kati tetoio, otan o xristis patouse back tha ekleine tin efarmogi handler.
            //Wstoso twra tha epistrepsei stin efarmogi mas!!!
            sharedintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

            //we are going to share plain text
            sharedintent.setType("text/plain");
            sharedintent.putExtra(Intent.EXTRA_TEXT,mFOrecastStr+FORECAST_SHARE_HASHTAG);

            return sharedintent;
        }
    }
}