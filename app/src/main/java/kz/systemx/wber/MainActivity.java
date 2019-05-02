package kz.systemx.wber;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    JSONObject jObject;
    double aJsonDouble;
    double halykDouble;
    String aJsonStr;
    Button mUpdateButton;
    TextView mNumberToSend;
    TextView mTaxes;
    TextView mResult;
    TextView mSendingRate;
    TextView mReceivingRate;
    TextView mResultHalyk;


    public static final String CHAT_PREFS = "ChatPrefs";
    public static final String exchange_rate = "exchange_rate";
    public static final String exchange_rate_halyk = "exchange_rate_halyk";
    public static final String update_time = "update_time";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_main);

        mNumberToSend = findViewById(R.id.number_to_send);
        mResultHalyk = findViewById(R.id.result_halyk);

        update();

        mNumberToSend = findViewById(R.id.number_to_send);
        mTaxes = findViewById(R.id.taxes);
        mSendingRate = findViewById(R.id.sending);
        mReceivingRate = findViewById(R.id.receiving);
        mResult = findViewById(R.id.result);
        mResultHalyk = findViewById(R.id.result_halyk);

        mNumberToSend.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                String value = s.toString();

                if (value.equals("") || (value.length() == 1 && !(value.charAt(0) >= '0' && value.charAt(0)<='9'))) {
                    mTaxes.setText("0 $");
                    mResult.setText("0 $");
                } else {
                    int number_to_send = Integer.parseInt(mNumberToSend.getText().toString());
                    String str = mSendingRate.getText().toString();
                    while (str.charAt(0) != '1'){
                        str = str.substring(1);
                    }
                    double sending_rate = Double.valueOf(str);
                    int taxes;
                    double result = (number_to_send * 1.0)/sending_rate;
                    if (result - 10.0 < 500.01) {
                        taxes = 10;
                    } else if (result - 14.0 < 2000.01) {
                        taxes = 14;
                    } else if (result - 18.0 < 3000.01) {
                        taxes = 18;
                    } else if (result - 20.0 < 5000.01) {
                        taxes = 20;
                    } else {
                        taxes = 25;
                    }

                    double final_result = result - taxes;
                    if (final_result < 0) {
                        final_result = 0;
                    }

                    DecimalFormat dff = new DecimalFormat("#.##");

                    SharedPreferences prefs = getSharedPreferences(CHAT_PREFS, MODE_PRIVATE);
                    String exchange_rate_halyk_sp = prefs.getString(MainActivity.exchange_rate_halyk, null);
                    mResultHalyk.setText(String.valueOf(dff.format(Double.parseDouble(exchange_rate_halyk_sp) * final_result)) + " ₸");

                    mResult.setText(String.valueOf(dff.format(final_result)) + " $");
                    mTaxes.setText(String.valueOf(taxes) + " $");
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, int start, int before, int count){
                Log.d("Wber_app", "start:" + start + " s:" + s + " before:" + before + " count:" + count);
                String value = s.toString();

                if (value.equals("") || (value.length() == 1 && !(value.charAt(0) >= '0' && value.charAt(0)<='9'))) {
                    mTaxes.setText("0 $");
                    mResult.setText("0 $");
                } else {
                    int number_to_send = Integer.parseInt(mNumberToSend.getText().toString());
                    String str = mSendingRate.getText().toString();
                    while (str.charAt(0) != '1'){
                        str = str.substring(1);
                    }
                    double sending_rate = Double.valueOf(str);
                    int taxes;
                    double result = (number_to_send * 1.0)/sending_rate;
                    if (result - 10.0 < 500.01) {
                        taxes = 10;
                    } else if (result - 14.0 < 2000.01) {
                        taxes = 14;
                    } else if (result - 18.0 < 3000.01) {
                        taxes = 18;
                    } else if (result - 20.0 < 5000.01) {
                        taxes = 20;
                    } else {
                        taxes = 25;
                    }

                    double final_result = result - taxes;
                    if (final_result < 0) {
                        final_result = 0;
                    }

                    DecimalFormat dff = new DecimalFormat("#.##");

                    SharedPreferences prefs = getSharedPreferences(CHAT_PREFS, MODE_PRIVATE);
                    String exchange_rate_halyk_sp = prefs.getString(MainActivity.exchange_rate_halyk, null);
                    mResultHalyk.setText(String.valueOf(dff.format(Double.parseDouble(exchange_rate_halyk_sp) * final_result)) + " ₸");

                    mResult.setText(String.valueOf(dff.format(final_result)) + " $");
                    mTaxes.setText(String.valueOf(taxes) + " $");
                }
            }
        });

        mUpdateButton = findViewById(R.id.update_button);
        mUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                update();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void update(){
        DefaultHttpClient httpclient = new DefaultHttpClient(new BasicHttpParams());
        HttpGet httpget = new HttpGet("http://159.65.15.112/api/v1/core/converter_v2");
        // Depends on your web service
        httpget.setHeader("Content-type", "application/json");

        InputStream inputStream = null;
        String result = null;
        try {
            HttpResponse response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();

            inputStream = entity.getContent();
            // json is UTF-8 by default
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
            StringBuilder sb = new StringBuilder();

            String line = null;
            while ((line = reader.readLine()) != null)
            {
                sb.append(line + "\n");
            }
            result = sb.toString();
        } catch (Exception e) {
            Log.d("wber_app", e.toString());
        }
        finally {
            try{if(inputStream != null)inputStream.close();}
            catch(Exception squish){}
        }

        if (result == null) {
            // NO INTERNET
            Log.d("wber_app", "No Internet");
            SharedPreferences prefs = getSharedPreferences(CHAT_PREFS, MODE_PRIVATE);

            String exchange_rate_sp = prefs.getString(MainActivity.exchange_rate, null);
            String exchange_rate_halyk_sp = prefs.getString(MainActivity.exchange_rate_halyk, null);
            String update_time_sp = prefs.getString(MainActivity.update_time, null);

            if (exchange_rate_sp == null) {
                exchange_rate_sp = "NO INTERNET";
            }
            TextView sending = findViewById(R.id.sending);
            sending.setText(getResources().getString(R.string.won_rate) + ":  " + exchange_rate_sp);
            TextView update_time = findViewById(R.id.update_time);
            update_time.setText(this.getString(R.string.update_time) + ":  " + update_time_sp);
            TextView result_halyk = findViewById(R.id.receiving);
            result_halyk.setText(getResources().getString(R.string.tenge_rate) + ":  " + exchange_rate_halyk_sp);


            int number_to_send = Integer.parseInt(mNumberToSend.getText().toString());

            double sending_rate = Double.parseDouble(exchange_rate_sp);
            int taxes;
            double r_result = (number_to_send * 1.0)/sending_rate;
            if (r_result - 10.0 < 500.01) {
                taxes = 10;
            } else if (r_result - 14.0 < 2000.01) {
                taxes = 14;
            } else if (r_result - 18.0 < 3000.01) {
                taxes = 18;
            } else if (r_result - 20.0 < 5000.01) {
                taxes = 20;
            } else {
                taxes = 25;
            }

            double final_result = r_result - taxes;
            if (final_result < 0) {
                final_result = 0;
            }

            DecimalFormat dff = new DecimalFormat("#.##");
            mResultHalyk.setText(String.valueOf(dff.format(Double.parseDouble(exchange_rate_halyk_sp) * final_result)) + " ₸");

            mUpdateButton = findViewById(R.id.update_button);
            mUpdateButton.setText(R.string.update_button);
            mUpdateButton.setBackgroundColor(getResources().getColor(R.color.colorRed));

        } else {
            try {
                jObject = new JSONObject(result);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                aJsonDouble = jObject.getDouble("sending");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                halykDouble = jObject.getDouble("receiving");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                aJsonStr = jObject.getString("data_and_time");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            TextView sending = findViewById(R.id.sending);
            sending.setText(getResources().getString(R.string.won_rate) + ":  " + Double.toString(aJsonDouble));

            TextView receiving = findViewById(R.id.receiving);
            receiving.setText(getResources().getString(R.string.tenge_rate) + ":  " + Double.toString(halykDouble));

            int number_to_send = Integer.parseInt(mNumberToSend.getText().toString());

            double sending_rate = aJsonDouble;
            int taxes;
            double r_result = (number_to_send * 1.0)/sending_rate;
            if (r_result - 10.0 < 500.01) {
                taxes = 10;
            } else if (r_result - 14.0 < 2000.01) {
                taxes = 14;
            } else if (r_result - 18.0 < 3000.01) {
                taxes = 18;
            } else if (r_result - 20.0 < 5000.01) {
                taxes = 20;
            } else {
                taxes = 25;
            }

            double final_result = r_result - taxes;
            if (final_result < 0) {
                final_result = 0;
            }


            DecimalFormat dff = new DecimalFormat("#.##");
            mResultHalyk.setText(String.valueOf(dff.format(halykDouble * final_result)) + " ₸");

            TextView update_time = findViewById(R.id.update_time);
            update_time.setText(this.getString(R.string.update_time) + ":  " + aJsonStr);

            // saving data
            String exchange_rate_sp = Double.toString(aJsonDouble);
            String exchange_rate_halyk_sp = Double.toString(halykDouble);
            String update_time_sp = aJsonStr;
            SharedPreferences prefs = getSharedPreferences(CHAT_PREFS, 0);
            prefs.edit().putString(MainActivity.exchange_rate, exchange_rate_sp).apply();
            prefs.edit().putString(MainActivity.update_time, update_time_sp).apply();
            prefs.edit().putString(MainActivity.exchange_rate_halyk, exchange_rate_halyk_sp).apply();
            mUpdateButton = findViewById(R.id.update_button);
            mUpdateButton.setText(R.string.updated_button);
            mUpdateButton.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        }
    }
}