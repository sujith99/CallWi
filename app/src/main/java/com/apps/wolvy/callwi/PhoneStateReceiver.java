package com.apps.wolvy.callwi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.StrictMode;
import android.telephony.TelephonyManager;
import android.widget.Toast;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

/**
 * Created by sujit on 25-02-2018.
 */

public class PhoneStateReceiver extends BroadcastReceiver {
    String mac;
    String serverAdress;
    String ledStatus;
    @Override
    public void onReceive(Context context, Intent intent) {
        mac = getMACAddress("wlan0");
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        serverAdress = "192.168.0.5:80";
        try {
            System.out.println("Receiver start");
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

            if(state.equals(TelephonyManager.EXTRA_STATE_RINGING)){
                {
                    Toast.makeText(context,mac,Toast.LENGTH_SHORT).show();
                    ledStatus = mac;
                }
            }
        /*    if ((state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK))){
                Toast.makeText(context,"Call Received State",Toast.LENGTH_SHORT).show();
            }
         */   if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)){

                    ledStatus = "END";
            }
            HttpRequestTask requestTask = new HttpRequestTask(serverAdress);
            requestTask.execute(ledStatus);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    public static String getMACAddress(String interfaceName) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                if (interfaceName != null) {
                    if (!intf.getName().equalsIgnoreCase(interfaceName)) continue;
                }
                byte[] mac = intf.getHardwareAddress();
                if (mac==null) return "";
                StringBuilder buf = new StringBuilder();
                for (int idx=0; idx<mac.length; idx++)
                    buf.append(String.format("%02X:", mac[idx]));
                if (buf.length()>0) buf.deleteCharAt(buf.length()-1);
                return buf.toString();
            }
        } catch (Exception ex) { } // for now eat exceptions
        return "";

    }
    private class HttpRequestTask extends AsyncTask<String, Void, String> {

        private String serverAdress;
        private String serverResponse = "";

        public HttpRequestTask(String serverAdress) {
            this.serverAdress = serverAdress;
            System.out.println("HTTP Response from Ip Address:");
        }

        @Override
        protected String doInBackground(String... params) {
            System.out.println("Data sent , waiting response from server...");


            String val = params[0];
            final String url = "http://" + serverAdress + "/led/" + val;

            try {
                HttpClient client = new DefaultHttpClient();
                HttpGet getRequest = new HttpGet();
                getRequest.setURI(new URI(url));
                HttpResponse response = client.execute(getRequest);

                InputStream inputStream = null;
                inputStream = response.getEntity().getContent();
                BufferedReader bufferedReader =
                        new BufferedReader(new InputStreamReader(inputStream));

                serverResponse = bufferedReader.readLine();
                inputStream.close();

            } catch (URISyntaxException e) {
                e.printStackTrace();
                serverResponse = e.getMessage();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
                serverResponse = e.getMessage();
            } catch (IOException e) {
                e.printStackTrace();
                serverResponse = e.getMessage();
            }

            return serverResponse;
        }

        @Override
        protected void onPostExecute(String s) {
            System.out.println(serverResponse);

        }

        @Override
        protected void onPreExecute() {
            System.out.println("Sending data to server, please wait...");

        }
    }

}
