package com.fcaronte.telecameracasa;

import static com.fcaronte.telecameracasa.ServerData.address;
import static com.fcaronte.telecameracasa.ServerData.password;
import static com.fcaronte.telecameracasa.ServerData.telegramBot;
import static com.fcaronte.telecameracasa.ServerData.user;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    
    private TextView mStato;
    private final String TAG = "TelecameraCasa";
    private final String backgroud = "/param.cgi?action=update&root.BackgroundMode=";
    private final String status = "http://" + address + "/api/v1/get_status?";
    private final String login = "http://" + address + "/api/v1/login?user=" + user + "&pwd=" + password;
    private String token, reply = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mStato = findViewById(R.id.stato);
        callSite(login);

        TextView sito = findViewById(R.id.sito);
        sito.setHapticFeedbackEnabled(true);
        sito.setOnClickListener(view -> {
            try {
                sito.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://" + address + "/login"));
                startActivity(myIntent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, "No application can handle this request."
                        + " Please install a webbrowser",  Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        } );

        TextView notifiche = findViewById(R.id.notifiche);
        notifiche.setHapticFeedbackEnabled(true);
        notifiche.setOnClickListener(view -> {
            try {
                notifiche.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("tg://resolve?domain=" + telegramBot));
                startActivity(myIntent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, "No application can handle this request."
                        + " Please install a webbrowser",  Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        } );

        Button attiva = findViewById(R.id.attiva);
        attiva.setHapticFeedbackEnabled(true);
        attiva.setOnClickListener(view -> {
            if (token !=null) {
                attiva.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                String attiva1 = "http://" + address + backgroud + "on&token=" + token;
                callSite(attiva1);
                callSite(login);
            } else {
                Log.d(TAG, " can't get token");
            }
        });

        Button disattiva = findViewById(R.id.disattiva);
        disattiva.setHapticFeedbackEnabled(true);
        disattiva.setOnClickListener(view -> {
            if (token !=null) {
                disattiva.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                String disattiva1 = "http://" + address + backgroud + "off&token=" + token;
                callSite(disattiva1);
                callSite(login);
            } else {
                Log.d(TAG, " can't get token");
            }
        });
    }

    public void callSite(String url) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();
        Log.d(TAG + " Site Request url: ", url);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.d(TAG + " Site Request failed: ", e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    if (response.isSuccessful()) {
                        final String myResponse = response.body().string();
                        Log.d(TAG + " Site Response: ", myResponse);
                        if (myResponse.contains("token")) {
                            token = myResponse.substring(18, 58);
                            Log.d(TAG, " Token is: " + token);
                            trylogin(token);
                        } else {
                            reply = myResponse;
                            setStatus(reply);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, e.toString());
                }
            }
        });
    }

    public void trylogin (String token) {
        if (token != null) {
            callSite(status + "token=" + token);
            setStatus(reply);
        }
    }

    public void setStatus(String reply) {
        MainActivity.this.runOnUiThread(() -> {
            if (reply != null) {
                if (reply.contains("\"backgroundMode\":false")) {
                    mStato.setText(R.string.notifiche_disattivate);
                } else if (reply.contains("\"backgroundMode\":true")) {
                    mStato.setText(R.string.notifiche_attivate);
                } else
                mStato.setText(reply);
            }
            Log.d(TAG + " Status: ", reply);
        });
    }
}