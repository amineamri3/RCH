package com.foxdogstudios.peepers;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.*;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.webkit.ConsoleMessage.MessageLevel.LOG;


/**
 * Created by Amine on 27/10/2017.
 */

public class StartUp extends Activity {
    public static Context context;
    Button bt1;
    Button bt2;
    Intent startStream;
    String msg;
    byte buffer[] = new byte[16];
    UsbSerialPort port;
    UsbDeviceConnection connection;
    boolean isDone = false;
    Thread t;
    UsbSerialDriver driver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.startup);
        context= this;
        bt1 = (Button)findViewById(R.id.button);
        bt2 = (Button)findViewById(R.id.button3);
        startStream = new Intent(this,StreamCameraActivity.class);
       /* Runnable run = new CommuThread();
        new Thread(run).start();*/

        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              startActivity(startStream);
            }
        });

        bt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UsbDevice device;
              if(connection == null) {
                  UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
                  HashMap<String, UsbDevice> usbDevices = manager.getDeviceList();
                  if (!usbDevices.isEmpty()) {
                      boolean keep = true;
                      for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                          device = entry.getValue();
                          int deviceVID = device.getVendorId();
                          int devicePID = device.getProductId();
                          if (deviceVID != 0x1d6b || (devicePID != 0x0001 || devicePID != 0x0002 || devicePID != 0x0003)) {
                              // We are supposing here there is only one device connected and it is our serial device
                              connection = manager.openDevice(device);
                              keep = false;
                          } else {
                              connection = null;
                              device = null;
                          }

                          if (!keep)
                              break;
                      }
                  }

                  List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
                  if (availableDrivers.isEmpty()) {
                      Toast.makeText(getApplicationContext(), "No Devices Found !", Toast.LENGTH_LONG).show();
                      return;
                  }
                  Log.d("CHECK", "1");
                  // Open a connection to the first available driver.
                  driver = availableDrivers.get(0);
                  Toast.makeText(getApplicationContext(), "Requesting permission", Toast.LENGTH_SHORT);
                  // manager.requestPermission(driver.getDevice(),null);
                  Log.d("CHECK", "2");
              }


                if (connection == null) {
                    Toast.makeText(getApplicationContext(),"Connection Failed !",Toast.LENGTH_LONG).show();
                    // You probably need to call UsbManager.requestPermission(driver.getDevice(), ..)
                    return;
                }else{
                    Toast.makeText(getApplicationContext(),"Connected",Toast.LENGTH_SHORT);
                 port =driver.getPorts().get(0);


        if(isDone == false){
                  isDone = true;
            t = new Thread() {
                      public void run() {
                          try {


                           ServerSocket serverSocket = new ServerSocket(8888);
                            Socket socket = serverSocket.accept();
                            InputStream is = socket.getInputStream();
                            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                              port.open(connection);
                              port.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

                              while (true) {
                                   msg = reader.readLine();
                                  Log.d("recived",msg);
                                  if(msg != "") {
                                      buffer = msg.getBytes();
                                      port.write(buffer, 1000);
                                  }
                              }

                          } catch (IOException io) {
                              io.printStackTrace();
                          } finally {
                              try {
                                  port.close();
                              } catch (IOException e) {
                                  e.printStackTrace();
                              }
                          }
                      }
                  };}
                    if(! t.isAlive()) {
                  t.start();
              }else{
                  Toast.makeText(getApplicationContext(),"Usb Already Running",Toast.LENGTH_SHORT);

              }


        }}

        });

    }
}
