package com.example.Saprkin1;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    DataExchangePackage DEP;
    byte[] sendPackageBuffer = new byte[520];
    byte[] receivePackageBuffer = new byte[520];
    DatagramSocket socket;
    DatagramPacket sendPacket;
    TextView tv;
    EditText etYourMessage, etMessages;
    Boolean receiveCycleStopper;
    InetAddress localNetwork;
    InetSocketAddress localAddress;
    Thread receiverThread;
    Message message = new Message();
    User sender = new User();
    User receiver = new User();
    int messageNumber = 0;
    Context cts;
    ArrayList<Message> ALM = new ArrayList<Message>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cts = this;
        g.Messenger = new DataBase(cts, "Messenger.db", null, 1);
        tv = findViewById(R.id.tvMessages);
        receiveCycleStopper = true;
        etMessages = findViewById(R.id.etMessages);
        etMessages.setText("");
        etYourMessage = findViewById(R.id.etYourMessage);
        DEP = new DataExchangePackage();
        try {
            WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
            DEP.SetSenderIPaddress(Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress()).toString());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        DataBaseWorker(0);
        sender.Type = "sender";
        receiver.Type = "receiver";
        DEP.SetSenderPort(String.valueOf(9000));
        DEP.SetSenderNickName("User1");
        DEP.SetIPaddress(DEP.GetSenderIPaddress());
        DEP.SetPort(DEP.GetSenderPort());
        DEP.SetNickName("User2");
        if (!DataBaseWorker(4)) {
            sender.IPaddress = DEP.GetSenderIPaddress();
            sender.Port = Integer.parseInt(DEP.GetSenderPort());
            sender.NickName = DEP.GetSenderNickName();
            receiver.IPaddress = DEP.GetIPaddress();
            receiver.Port = Integer.parseInt(DEP.GetPort());
            receiver.NickName = DEP.GetNickName();
        }

        try {
            localNetwork = InetAddress.getByName("0.0.0.0");
            localAddress = new InetSocketAddress(localNetwork, sender.Port);
            socket = new DatagramSocket(null);
            socket.bind(localAddress);
            socket.setBroadcast(true);
        } catch (UnknownHostException | SocketException e) {
            e.printStackTrace();
        }
        Runnable receiverTask = new Runnable() {
            @Override
            public void run() {
                Log.e("TEST", "RECEIVER THREAD IS RUNNING");
                DatagramPacket receivedPacket = new DatagramPacket(receivePackageBuffer, receivePackageBuffer.length);
                while (receiveCycleStopper) {
                    if (!receiveCycleStopper) {

                        Log.e("TEST5", receiveCycleStopper.toString() + " REC");
                        break;
                    } else {
                        Log.e("TEST", receiveCycleStopper.toString() + " REC");
                        try {
                            socket.receive(receivedPacket);
                            String SIPP = receivedPacket.getAddress().toString().substring(1) + ":" + receivedPacket.getPort();
                            String[] SIPPMessage = SIPP.split(":");
                            String timeText = new SimpleDateFormat("dd-MM-YYYY HH:mm:ss", Locale.getDefault()).format(new Date());
                            String datetime[] = timeText.split(" ");
                            String sdata = timeText + " " + SIPP;
                            String s = new String(receivedPacket.getData(), 0, receivedPacket.getLength());
                            String[] mes = s.split(":", DEP.GetReceivePackageLength());
                            Log.e("TEST", receiveCycleStopper.toString() + " RECEIVED: " + s);
                            if ((g.Messenger.getMaxNumber()) > 0) {
                                messageNumber = g.Messenger.getMaxNumber() + 1;
                            } else {
                                messageNumber = 0;
                            }
                            message.number = messageNumber + 1;
                            message.date = datetime[0];
                            message.time = datetime[1];
                            message.senderNickName = mes[1];
                            message.senderIPaddress = SIPPMessage[0];
                            message.senderPort = Integer.parseInt(SIPPMessage[1]);
                            message.content = mes[5];
                            DataBaseWorker(1);
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    etMessages.setText(etMessages.getText().toString() + sdata + "\n" + mes[1] + " > " + mes[5] + "\n\n");
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                return;
            }
        };
        receiverThread = new Thread(receiverTask);
        receiverThread.start();
    }

    public void BtnSettingsOnClick(View v) {
        receiveCycleStopper = false;
        LayoutInflater myLayout = LayoutInflater.from(this);
        View dialogView = myLayout.inflate(R.layout.alertdialog_settings, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        AlertDialog SettingAlertDialog = builder.create();
        SettingAlertDialog.show();
        Button BtnCancelSettingAld = SettingAlertDialog.findViewById(R.id.btnCancelSetting);
        Button BtnSetSettingAld = SettingAlertDialog.findViewById(R.id.btnSetSetting);
        EditText etReceiverIP = SettingAlertDialog.findViewById(R.id.etReceiverIP);
        EditText etYourIP = SettingAlertDialog.findViewById(R.id.etYourIP);
        EditText etReceiverNickName = SettingAlertDialog.findViewById(R.id.etReceiverNickName);
        EditText etYourNickName = SettingAlertDialog.findViewById(R.id.etYourNickName);
        EditText etReceiverPort = SettingAlertDialog.findViewById(R.id.etReceiverPort);
        EditText etYourPort = SettingAlertDialog.findViewById(R.id.etYourPort);
        try {
            WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
            String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
            etYourIP.setText(ip);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        etReceiverIP.setText(receiver.IPaddress);
        etReceiverPort.setText(String.valueOf(receiver.Port));
        etReceiverNickName.setText(receiver.NickName);
        etYourIP.setText(sender.IPaddress);
        etYourPort.setText(String.valueOf(sender.Port));
        etYourNickName.setText(sender.NickName);
        BtnSetSettingAld.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    receiveCycleStopper = false;
                    Integer.parseInt(etReceiverPort.getText().toString());
                    Integer.parseInt(etYourPort.getText().toString());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    return;
                }
                receiveCycleStopper = false;
                if ((!etReceiverNickName.getText().toString().equals("") && !etYourNickName.getText().toString().equals("")) && (!etReceiverNickName.getText().toString().equals(etYourNickName.getText().toString()))) {
                    DEP.SetIPaddress(etReceiverIP.getText().toString());
                    DEP.SetPort(etReceiverPort.getText().toString());
                    DEP.SetNickName(etReceiverNickName.getText().toString());
                    DEP.SetSenderIPaddress(etYourIP.getText().toString());
                    DEP.SetSenderPort(etYourPort.getText().toString());
                    DEP.SetSenderNickName(etYourNickName.getText().toString());
                    sender = new User();
                    receiver = new User();
                    sender.Type = "sender";
                    sender.IPaddress = DEP.GetSenderIPaddress();
                    sender.Port = Integer.parseInt(DEP.GetSenderPort());
                    sender.NickName = DEP.GetSenderNickName();
                    receiver.Type = "receiver";
                    receiver.IPaddress = DEP.GetIPaddress();
                    receiver.Port = Integer.parseInt(DEP.GetPort());
                    receiver.NickName = DEP.GetNickName();
                    DataBaseWorker(3);
                    try {
                        if (socket != null) {
                            socket.close();
                            localNetwork = InetAddress.getByName("0.0.0.0");
                            localAddress = new InetSocketAddress(localNetwork, Integer.parseInt(DEP.GetSenderPort()));
                            socket = new DatagramSocket(null);
                            socket.bind(localAddress);
                            socket.setBroadcast(true);
                        } else {
                            localNetwork = InetAddress.getByName("0.0.0.0");
                            localAddress = new InetSocketAddress(localNetwork, Integer.parseInt(DEP.GetSenderPort()));
                            socket = new DatagramSocket(null);
                            socket.bind(localAddress);
                            socket.setBroadcast(true);
                        }
                    } catch (UnknownHostException | SocketException e) {
                        e.printStackTrace();
                    }

                    receiveCycleStopper = true;
                    SettingAlertDialog.dismiss();
                } else {
                    Toast.makeText(cts, R.string.NickNameError, Toast.LENGTH_SHORT).show();
                    SettingAlertDialog.dismiss();
                }

            }
        });
        BtnCancelSettingAld.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {//Button Cancel when click
                SettingAlertDialog.dismiss();
            }
        });
    }

    public void BtnSendOnClick(View v) {
        if (etYourMessage.getText().toString().equals("")) {
            return;
        }
        DEP.SetSenderMessage(etYourMessage.getText().toString());
        String sendContent = String.valueOf(sender.NickName.length()) + ':' + sender.NickName + ':' + String.valueOf(receiver.NickName.length()) + ':' + receiver.NickName + ':' + String.valueOf(DEP.GetSenderMessage().length()) + ':' + DEP.GetSenderMessage();
        etYourMessage.setText("");

        try {
            InetAddress remoteAddress = InetAddress.getByName(receiver.IPaddress);
            sendPackageBuffer = sendContent.getBytes(StandardCharsets.UTF_8);
            sendPacket = new DatagramPacket(sendPackageBuffer, sendPackageBuffer.length, remoteAddress, receiver.Port);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return;
        }
        sendPacket.setLength(sendContent.length());
        Runnable sender = new Runnable() {
            @Override
            public void run() {
                Log.e("TEST", "SENDING THREAD IS RUNNING");
                try {
                    socket.send(sendPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread senderThread = new Thread(sender);
        senderThread.start();
        etYourMessage.setText("");
    }

    public boolean DataBaseWorker(int dbFunctionSwitcher) {
        boolean result = false;
        switch (dbFunctionSwitcher) {
            case (0):
                g.Messenger.getAllMessages(ALM);
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                for (int i = 0; i < ALM.size(); i++) {
                                    String sdata = ALM.get(i).date + " " + ALM.get(i).time + " " + ALM.get(i).senderIPaddress + ":" + ALM.get(i).senderPort + "\n" + ALM.get(i).senderNickName + " > " + ALM.get(i).content + "\n\n";
                                    etMessages.setText(etMessages.getText().toString() + sdata);
                                }
                            }
                        });
                    }
                };
                Thread rt = new Thread(r);
                rt.start();
                break;
            case (1):
                g.Messenger.addMessage(message.number, message.date, message.time, message.senderNickName, message.senderIPaddress, message.senderPort, message.content);
                break;
            case (2):
                g.Messenger.delAllMessages();
                messageNumber = 0;
                break;
            case (3):
                g.Messenger.delAllUser();
                g.Messenger.addUser(sender.Type, sender.NickName, sender.IPaddress, sender.Port);
                g.Messenger.addUser(receiver.Type, receiver.NickName, receiver.IPaddress, receiver.Port);
                break;
            case (4):
                g.Messenger.getUser("sender", sender);
                g.Messenger.getUser("receiver", receiver);
                if (g.Messenger.getUser("sender", sender) && g.Messenger.getUser("receiver", receiver)) {
                    result = true;
                }
                break;
        }
        return result;
    }

    public void BtnClearHistoryOnClick(View v) {
        LayoutInflater myLayout = LayoutInflater.from(this);
        View dialogView = myLayout.inflate(R.layout.alertdialog_alert, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        AlertDialog AlertAlertDialog = builder.create();
        AlertAlertDialog.show();
        Button btnOkAlert = AlertAlertDialog.findViewById(R.id.btnOkAlert);
        Button btnCancelAlert = AlertAlertDialog.findViewById(R.id.btnCancelAlert);
        btnOkAlert.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                etMessages.setText("");
                DataBaseWorker(2);

                AlertAlertDialog.dismiss();
            }
        });
        btnCancelAlert.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertAlertDialog.dismiss();
            }
        });
    }

    public void BtnExitOnClick(View v) {
        LayoutInflater myLayout = LayoutInflater.from(this);
        View dialogView = myLayout.inflate(R.layout.alertdialog_exit, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        AlertDialog ExitAlertDialog = builder.create();
        ExitAlertDialog.show();
        Button BtnCancelExitAld = ExitAlertDialog.findViewById(R.id.btnCancelExit);
        Button BtnOkExitAld = ExitAlertDialog.findViewById(R.id.btnOkExit);
        BtnOkExitAld.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                System.exit(0);
            }
        });
        BtnCancelExitAld.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ExitAlertDialog.dismiss();
            }
        });
    }
}