package com.example.dustapp;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity implements AsyncTaskListener, View.OnClickListener {

    private final static String RED = "#fc0303";
    private final static String YELLOW = "##fafa00";
    private final static String BLUE = "#0000fa";
    private final static String GREEN = "#00fa00";
    private final static String BADWEATHER = "@drawable/img_bad";
    private final static String GOODWEATHER = "@drawable/img_good";
    DataPackage dpkg;
    private FloatingActionButton floating;
    private FusedLocationProviderClient fusedLocationClient;
    private ProgressBar pbarbig;
    private ProgressBar pbarsmall;
    private ImageView weatherimage;
    private TextView addressview;
    private TextView dustvalueview1;
    private TextView dustvalueview2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        askper();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        dpkg = new DataPackage();
        floating = (FloatingActionButton) findViewById(R.id.floating);
        floating.setOnClickListener(this); // calling onClick() method
        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(this);
        pbarbig = (ProgressBar) findViewById(R.id.progressBar1);
        pbarsmall = (ProgressBar) findViewById(R.id.progressBar2);
        weatherimage = (ImageView) findViewById(R.id.imageView);
        addressview = (TextView) findViewById(R.id.textView2);
        dustvalueview1 = (TextView) findViewById(R.id.dustnum1);
        dustvalueview2 = (TextView) findViewById(R.id.dustnum2);
        DataManager dman = new DataManager(this, true);
        dpkg = dman.readData();
        updateUI(true);
        getloc();
    }

    public void openSetting() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.button:
                getloc();
                break;

            case R.id.floating:
                openSetting();
                break;

            default:
                break;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void askper() {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 100);
    }

    public void getloc() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            Double[] coordinates = {location.getLongitude(), location.getLatitude()};
                            new DataManager(MainActivity.this, true).execute(coordinates);
                        }
                    }
                });
    }

    @Override
    public void updateUI(boolean result) {
        int adjbigDust;
        int adjsmallDust;
        if (dpkg.bigDust > 200) {
            adjbigDust = 100;
        } else {
            adjbigDust = dpkg.bigDust / 2;
        }

        if (dpkg.smallDust > 100) {
            adjsmallDust = 100;
        } else {
            adjsmallDust = dpkg.smallDust;
        }
        pbarbig.setProgress(adjbigDust);
        pbarsmall.setProgress(adjsmallDust);
        if (dpkg.bigDust <= 30) {
            pbarbig.getProgressDrawable().setColorFilter(
                    Color.parseColor(MainActivity.GREEN), android.graphics.PorterDuff.Mode.SRC_IN);
        } else if (dpkg.bigDust <= 80) {
            pbarbig.getProgressDrawable().setColorFilter(
                    Color.parseColor(MainActivity.BLUE), android.graphics.PorterDuff.Mode.SRC_IN);
        } else if (dpkg.bigDust <= 150) {
            pbarbig.getProgressDrawable().setColorFilter(
                    Color.parseColor(MainActivity.YELLOW), android.graphics.PorterDuff.Mode.SRC_IN);
        } else {
            pbarbig.getProgressDrawable().setColorFilter(
                    Color.parseColor(MainActivity.RED), android.graphics.PorterDuff.Mode.SRC_IN);
        }

        if (dpkg.smallDust <= 15) {
            pbarsmall.getProgressDrawable().setColorFilter(
                    Color.parseColor(MainActivity.GREEN), android.graphics.PorterDuff.Mode.SRC_IN);
        } else if (dpkg.smallDust <= 35) {
            pbarsmall.getProgressDrawable().setColorFilter(
                    Color.parseColor(MainActivity.BLUE), android.graphics.PorterDuff.Mode.SRC_IN);
        } else if (dpkg.smallDust <= 75) {
            pbarsmall.getProgressDrawable().setColorFilter(
                    Color.parseColor(MainActivity.YELLOW), android.graphics.PorterDuff.Mode.SRC_IN);
        } else {
            pbarsmall.getProgressDrawable().setColorFilter(
                    Color.parseColor(MainActivity.RED), android.graphics.PorterDuff.Mode.SRC_IN);
        }

        if (dpkg.bigDust <= 80 && dpkg.smallDust <= 35) {
            int imageResource = getResources().getIdentifier(GOODWEATHER, null, getPackageName());
            Drawable res = getResources().getDrawable(imageResource);
            weatherimage.setImageDrawable(res);
        } else {
            int imageResource = getResources().getIdentifier(BADWEATHER, null, getPackageName());
            Drawable res = getResources().getDrawable(imageResource);
            weatherimage.setImageDrawable(res);
        }

        addressview.setText(dpkg.address);
        dustvalueview1.setText(String.valueOf(dpkg.bigDust));
        dustvalueview2.setText(String.valueOf(dpkg.smallDust));
    }

    @Override
    public void getData(DataPackage dpkg) {
        this.dpkg = dpkg;
    }

    public void showError() {
    }
}
