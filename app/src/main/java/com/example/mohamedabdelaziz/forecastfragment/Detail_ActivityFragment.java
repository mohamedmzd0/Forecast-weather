package com.example.mohamedabdelaziz.forecastfragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;





public class Detail_ActivityFragment extends Fragment {

    public Detail_ActivityFragment() {
setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
Intent intent=  getActivity().getIntent() ;
        String data = intent.getStringExtra("data").toString() ;
        View rootView = inflater.inflate(R.layout.fragment_detail_, container, false);
        TextView textView = (TextView) rootView.findViewById(R.id.textView) ;
        textView.setText(data);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_detail_,menu);
       }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {

            startActivity(new Intent(getContext(), SettingsActivity.class));

            return true;
        }
        if (id == android.R.id.home) {
           getActivity().finish();
        }

        if ((id==R.id.share))
        {


            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
