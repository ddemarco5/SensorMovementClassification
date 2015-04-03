package jsat.com.sensormovementclassification;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Dominic on 4/2/2015.
 */



public class NetworkServer {


    private TextView serverStatus;
    public final String SERVERIP; //gets set in constructor
    public final int SERVERPORT = 8080;

    public NetworkServer(TextView textview, Context context){
        serverStatus = textview;
        //This needs to be done in here for some unknown reason. Otherwise we will lose the context
        SERVERIP = getIpAddress(context);
        //readTrainingData();
    }


    private Handler handler = new Handler();


    ServerThread server = new ServerThread();



    protected void startService(){
        serverStatus.append("Starting network server...\n");
        new Thread(server).start();
    }
    protected void stopService(){
        serverStatus.append("Stopping network server...\n");
        server.stop();
        if(!server.isStopped) serverStatus.append("Network thread didn't stop correctly\n");
    }

    public class ServerThread implements Runnable {

        protected boolean isStopped;
        private ServerSocket serverSocket;
        private Socket clientSocket;
        //protected Thread runningThread= null;

        ServerThread(){
            isStopped=false;
        }

        public boolean isStopped(){
            return isStopped;
        }

        public synchronized void stop(){
            this.isStopped = true;
            try {
                serverSocket.close();
                clientSocket.close();
            } catch (IOException e) {
                throw new RuntimeException("Error closing server", e);
            }
        }

        private void openServerSocket() {
            try {
                this.serverSocket = new ServerSocket(SERVERPORT);
            } catch (IOException e) {
                throw new RuntimeException("Cannot open port " + SERVERPORT, e);
            }
        }

        //All the handler.post nasty stuff in this function is to write to textviews of another thread. I don't like it either.
        public void run() {
            //synchronized(this){
            //    this.runningThread = Thread.currentThread();
            //}
            try {
                if (SERVERIP != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            serverStatus.append("Listening on IP: " + SERVERIP + ":" + SERVERPORT + "\n");
                        }
                    });
                    //serverSocket = new ServerSocket(SERVERPORT);
                    openServerSocket();
                    while (!isStopped) {
                        Log.d("NETWORK", "Network thread started");
                        // LISTEN FOR INCOMING CLIENTS
                        clientSocket = serverSocket.accept();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                serverStatus.append("Connected.\n");
                                PrintWriter output = null;
                                try {
                                    output = new PrintWriter(clientSocket.getOutputStream());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                output.print("Welcome.\n");
                                output.flush();
                            }
                        });
                        BufferedReader in = null;
                            try {
                                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                                String line = null;
                                while ((line = in.readLine()) != null) {
                                    Log.d("ServerActivity", line);
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            // DO WHATEVER YOU WANT TO THE FRONT END
                                            // THIS IS WHERE YOU CAN BE CREATIVE
                                        }
                                    });
                                }
                                break;
                            } catch (Exception e) {
                                in.close();
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        //serverStatus.append("Oops. Connection interrupted. Please reconnect your phones.\n");
                                        serverStatus.append("Connection Terminated, inputBuffer closing.\n");
                                    }
                                });
                                //e.printStackTrace();
                         }
                    }
                    //Debug
                    Log.d("NETWORK", "Network thread stopped");
                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            serverStatus .append("Couldn't detect internet connection.\n");
                        }
                    });
                }
            } catch (final Exception e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        serverStatus.append("Error" + e.getMessage());
                    }
                });
                e.printStackTrace();
            }
        }
    }
    private String getIpAddress(Context context) {
        if(context == null){
            Log.d("Stuff", "WFM is null biatch");
            return "poop";
        }
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ip = wifiInfo.getIpAddress();

        String ipString = String.format(
                "%d.%d.%d.%d",
                (ip & 0xff),
                (ip >> 8 & 0xff),
                (ip >> 16 & 0xff),
                (ip >> 24 & 0xff));

        return ipString;
    }


}

