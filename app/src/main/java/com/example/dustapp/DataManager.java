package com.example.dustapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DataManager extends AsyncTask<Double, Boolean, Boolean> {

    private DataPackage dpkg;
    private Context mcontext;
    private boolean foregroundtask;

    public DataManager(Context context, boolean foregroundtask) {
        dpkg = new DataPackage();
        mcontext = context;
        this.foregroundtask = foregroundtask;
    }

    public static String getStackTrace(final Throwable throwable) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }

    protected Boolean doInBackground(Double... coordinates) {
        if (coordinates.length != 0) {
            updateData(coordinates[0], coordinates[1]);
        } else {
            updateData();
        }
        return true;
    }

    protected void onPostExecute(Boolean result) {
        readData();
        if (foregroundtask) {
            ((AsyncTaskListener) mcontext).getData(this.dpkg);
            ((AsyncTaskListener) mcontext).updateUI(result);
        }
    }

    private String getJSON(String requestURL, boolean header) {
        try {
            Request request;
            OkHttpClient client = new OkHttpClient();
            if (header) {
                request = new Request.Builder()
                        .addHeader("Authorization", "KakaoAK 67e71f627508c5bf2bdab023699a4c11")
                        .url(requestURL)
                        .build();
            } else {
                request = new Request.Builder()
                        .url(requestURL)
                        .build();
            }


            Response response = client.newCall(request).execute();
            String responsebody = response.body().string();

            return responsebody;
        } catch (Exception e) {
            return "error";
        }
    }

    public DataPackage readData() {
        SharedPreferences sharedPref = mcontext.getSharedPreferences("Dust Data", Context.MODE_PRIVATE);
        dpkg.longitude = getDouble(sharedPref, "longitude");
        dpkg.latitude = getDouble(sharedPref, "latitude");
        dpkg.tmLongitude = getDouble(sharedPref, "tmLongitude");
        dpkg.tmLatitude = getDouble(sharedPref, "tmLatitude");
        dpkg.address = sharedPref.getString("address", "");
        dpkg.station = sharedPref.getString("station", "");
        dpkg.bigDust = sharedPref.getInt("bigDust", -1);
        dpkg.smallDust = sharedPref.getInt("smallDust", -1);
        dpkg.bigDustRating = sharedPref.getInt("bigDustRating", -1);
        dpkg.smallDustRating = sharedPref.getInt("smallDustRating", -1);
        return dpkg;
    }

    private void saveData() {
        SharedPreferences sharedPref = mcontext.getSharedPreferences("Dust Data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        putDouble(editor, "longitude", dpkg.longitude);
        putDouble(editor, "latitude", dpkg.latitude);
        putDouble(editor, "tmLongitude", dpkg.tmLongitude);
        putDouble(editor, "tmLatitude", dpkg.tmLatitude);
        editor.putString("address", dpkg.address);
        editor.putString("station", dpkg.station);
        editor.putInt("bigDust", dpkg.bigDust);
        editor.putInt("smallDust", dpkg.smallDust);
        editor.putInt("bigDustRating", dpkg.bigDustRating);
        editor.putInt("smallDustRating", dpkg.smallDustRating);
        editor.commit();
    }

    private void putDouble(SharedPreferences.Editor editor, String key, double value) {
        editor.putLong(key, java.lang.Double.doubleToRawLongBits(value));
    }

    private double getDouble(SharedPreferences sharedPref, String key) {
        return java.lang.Double.longBitsToDouble(sharedPref.getLong(key, -1));
    }

    public boolean updateData(double longitude, double latitude) {
        dpkg.longitude = longitude;
        dpkg.latitude = latitude;
        if (updateTM() && updateStation() && updateDust()) {
            saveData();
            readData();
            return true;
        }
        return false;
    }

    public boolean updateData() {
        SharedPreferences sharedPref = mcontext.getSharedPreferences("Dust Data", Context.MODE_PRIVATE);
        dpkg.longitude = getDouble(sharedPref, "longitude");
        dpkg.latitude = getDouble(sharedPref, "latitude");
        if (updateTM() && updateStation() && updateDust()) {
            saveData();
            readData();
            return true;
        }
        return false;
    }

    public boolean updateTM() {
        String initialUrl = "https://dapi.kakao.com/v2/local/geo/coord2regioncode.json?";
        String longitudeString = "x=" + dpkg.longitude;
        String latitudeString = "y=" + dpkg.latitude;
        String inputCoord = "input_coord=WGS84";
        String outputCoord = "output_coord=TM";
        String finalurl = initialUrl + longitudeString + "&" + latitudeString + "&" + inputCoord + "&" + outputCoord;


        String data = getJSON(finalurl, true);

        if (data == "error") {
            return false;
        }

        try {
            JSONObject jdata = new JSONObject(data);
            dpkg.tmLongitude = jdata.getJSONArray("documents").getJSONObject(1).getDouble("x");
            dpkg.tmLatitude = jdata.getJSONArray("documents").getJSONObject(1).getDouble("y");
        } catch (JSONException e) {
            return false;
        }

        return true;
    }

    public boolean updateStation() {
        String initialUrl = "http://openapi.airkorea.or.kr/openapi/services/rest/MsrstnInfoInqireSvc/getNearbyMsrstnList?";
        String tmLongitudeString = "tmX=" + dpkg.tmLongitude;
        String tmLatitudeString = "tmY=" + dpkg.tmLatitude;
        String serviceKey = "ServiceKey=5bfyvpoHUg17gwuIq6DxhcUo4nM%2BgFjLzbbNiVEwvwrKddtqYRIWF%2BJAOLBnYArH%2BeE0tQd8nN0iPCDa9lbEow%3D%3D";
        String jsontype = "_returnType=json";
        String finalurl = initialUrl + tmLongitudeString + "&" + tmLatitudeString + "&" + serviceKey + "&" + jsontype;

        String data = getJSON(finalurl, false);

        if (data == "error") {
            return false;
        }

        try {
            JSONObject jdata = new JSONObject(data);
            JSONArray jarr = jdata.getJSONArray("list");
            int closest = -1;
            double least = -1;
            for (int i = 0; i < jarr.length(); i++) {
                JSONObject obj = jarr.getJSONObject(i);
                if (Double.valueOf(obj.getString("tm")) < least || least == -1) {
                    least = Double.valueOf(obj.getString("tm"));
                    closest = i;
                }
            }
            dpkg.station = jarr.getJSONObject(closest).getString("stationName");
            dpkg.address = jarr.getJSONObject(closest).getString("addr");
        } catch (JSONException e) {
            return false;
        }

        return true;
    }

    public boolean updateDust() {
        String initialUrl = "http://openapi.airkorea.or.kr/openapi/services/rest/ArpltnInforInqireSvc/getMsrstnAcctoRltmMesureDnsty?";
        String stationName = "stationName=" + dpkg.station;
        String dataTerm = "dataTerm=daily";
        String pageNo = "pageNo=1";
        String numOfRows = "numOfRows=1";
        String serviceKey = "ServiceKey=5bfyvpoHUg17gwuIq6DxhcUo4nM%2BgFjLzbbNiVEwvwrKddtqYRIWF%2BJAOLBnYArH%2BeE0tQd8nN0iPCDa9lbEow%3D%3D";
        String ver = "ver=1.3";
        String jsontype = "_returnType=json";
        String finalurl = initialUrl + stationName + "&" + dataTerm + "&" + pageNo + "&" + numOfRows + "&" + serviceKey + "&" + ver + "&" + jsontype;

        String data = getJSON(finalurl, false);

        if (data == "error") {
            return false;
        }

        try {
            JSONObject jdata = new JSONObject(data);
            jdata = jdata.getJSONArray("list").getJSONObject(0);
            dpkg.bigDust = Integer.valueOf(jdata.getString("pm10Value"));
            dpkg.smallDust = Integer.valueOf(jdata.getString("pm25Value"));
            dpkg.bigDustRating = Integer.valueOf(jdata.getString("pm10Grade1h"));
            dpkg.bigDustRating = Integer.valueOf(jdata.getString("pm25Grade1h"));
        } catch (JSONException e) {
            return false;
        }

        return true;
    }
}
