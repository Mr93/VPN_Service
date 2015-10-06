package com.example.dendimon.vpn_service;

import android.content.Intent;
import android.net.VpnService;
import android.os.Handler;
import android.os.ParcelFileDescriptor;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;

/**
 * Created by Dendimon on 10/2/2015.
 */
public class VPN_service extends VpnService {

 //   private static final String TAG = "Packet_capture";
  //  private Handler mHandler;
    private Thread mthread;
    private ParcelFileDescriptor mInterface;

    //a. Configure a builder for interface
    Builder builder = new Builder();

    //Service interface
    @Override
    public int onStartCommand (Intent intent,int flags, int startid){
        //Start a new session by creating a new theard
        mthread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    //a. configure the TUN and get the interface
                    mInterface = builder.setSession("VPN_service")
                            .addAddress("192.168.0.1",24)
                            .addDnsServer("8.8.8.8")
                            .addRoute("0.0.0.0",0).establish();

                    //b. packets to be sent are queued in this input stream
                    FileInputStream in = new FileInputStream(mInterface.getFileDescriptor());

                    //b. packets received need to written to this output stream
                    FileOutputStream ou = new FileOutputStream(mInterface.getFileDescriptor());

                    //c. the udp channel can be used to pass/get ip package to/from server
                    DatagramChannel tunnel = DatagramChannel.open();

                    //connect to the server, localhost is used for demonstration only
                    tunnel.connect(new InetSocketAddress("127.0.0.1",8087));

                    //d. protect this socket, so package send by it will not be feedback to the vpn service
                    protect(tunnel.socket());

                    //e. use a loop to pass packets
                    while (true) {
                        //get packet with in
                        //put packet to tunnel
                        //get packet form tunnel
                        //return packet with out
                        //sleep is a must
                        Thread.sleep(100);
                    }

                } catch (Exception e) {
                    // Catch any exception
                    e.printStackTrace();
                } finally {
                    try {
                        if (mInterface != null) {
                            mInterface.close();
                            mInterface = null;
                        }
                    } catch (Exception e) {

                    }
                }
            }

        }, "MyVpnRunnable");

        //start the service
        mthread.start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        if (mthread != null) {
            mthread.interrupt();
        }
        super.onDestroy();
    }
}

