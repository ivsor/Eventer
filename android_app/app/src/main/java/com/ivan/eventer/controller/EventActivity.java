package com.ivan.eventer.controller;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.ivan.eventer.R;
import com.ivan.eventer.backend.Commands;
import com.ivan.eventer.model.Event;
import com.ivan.eventer.model.EventPreview;
import com.ivan.eventer.view.Event.EventFragment;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class EventActivity extends AppCompatActivity {

    public static EventPreview sEventPreview;

    public static ArrayList<Thread> sThreadsFrom;

    public static ArrayList<Thread> sThreadsTo;

    public static HashMap<String, Socket> sSocketMap;

    static {
        sThreadsTo = new ArrayList<>();
        sThreadsFrom = new ArrayList<>();
        sSocketMap = new HashMap<>();
    }

    static int serverPort = 6667; // здесь обязательно нужно указать порт к которому привязывается сервер.

    static String address = "37.230.113.214"; // это IP-адрес компьютера, где исполняется наша серверная программа.
    private String mEventId;
    // ID в массиве сокетов
    private int mSocketId;

    private FragmentManager mFragmentManager;

    private Fragment mContainer;
    private Fragment mFragment;

    private DataOutputStream out;
    private DataInputStream in;

    private ArrayList<ConnectionListener> mConnectionListeners;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        mEventId = getID();
        makeEvent(mEventId);

        mSocketId = sThreadsTo.size();

        sThreadsTo.add(
                new Thread() {
                    @Override
                    public void run() {
                        makeConnection();
                    }
                }
        );



        sThreadsTo.get(mSocketId).start();

        try {
            sThreadsTo.get(mSocketId).join();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.eventFragmentContainer);

        if (fragment == null) {

            fragment = new EventFragment();
            fm.beginTransaction()
                    .add(R.id.eventFragmentContainer, fragment)
                    .commit();

        }

        mConnectionListeners = new ArrayList<>();

    }

    private void makeEvent(String id) {

        Event event = Commands.findEventById(id);
        EventActivity.sEventPreview = new EventPreview(
                event.getID(),
                event.getTitle(),
                event.getDescribe(),
                event.getAuthor(),
                event.getImage(),
                event.getKind(),
                event.getTime(),
                event.getPosition(),
                event.getAddress(),
                event.getDate()

        );

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();


    }

    private String getID() {
        return getIntent().getStringExtra("ID");

    }

    public void makeConnection() {

        try {

            InetAddress ipAddress = InetAddress.getByName(address); // создаем объект который отображает вышеописанный IP-адрес.
            System.out.println("Any of you heard of a socket with IP address " + address + " and port " + serverPort + "?");

            Socket socket = sSocketMap.get(mEventId);
            if (socket == null || socket.isClosed()) {
                System.out.println("Yes! I just got hold of the program.");
                socket = new Socket(ipAddress, serverPort); // создаем сокет используя IP-адрес и порт сервера.
                sSocketMap.put(mEventId, socket);

                // Берем входной и выходной потоки сокета, теперь можем получать и отсылать данные клиентом.
                InputStream sin = socket.getInputStream();
                OutputStream sout = socket.getOutputStream();

                // Конвертируем потоки в другой тип, чтоб легче обрабатывать текстовые сообщения.
                in = new DataInputStream(sin);
                out = new DataOutputStream(sout);

                out.writeUTF(getID());

                System.err.println(getID());

                out.flush();

                System.out.println("Type in something and press enter. Will send it to the server and tell ya what it thinks.");
                System.out.println();

                sThreadsFrom.add(new Thread(() -> {

                    while (true) {

                        String messageString = "0 "; // ждем пока сервер отошлет строку текста.
                        try {
                            messageString = in.readUTF();
                        } catch (IOException e) {
                            break;
                        }

                        Log.d("DEBUG", messageString);
                        char commandType = messageString.charAt(0);

                        String data = "";
                        if (messageString.length() >= 2) {
                            data = messageString.substring(2, messageString.length() - 1);
                        }

                        for (ConnectionListener listener : mConnectionListeners) {
                            if (listener.getCommandType() == commandType) {
                                listener.getData(data);
                            }
                        }
                    }

                }));
                sThreadsFrom.get(mSocketId).start();
            }
        } catch (Exception x) {

            x.printStackTrace();
        }
    }

    public void addConnectionListener(ConnectionListener listener) {
        mConnectionListeners.add(listener);
    }

    public void sendData(String data) {
        try {
            out.writeUTF(data); // отсылаем введенную строку текста серверу.
            out.flush(); // заставляем поток закончить передачу данных.
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            sSocketMap.get(mEventId).close();
            try {
                sThreadsFrom.get(mSocketId).stop();

            } catch (Exception ex) {

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
//    mThreadTo.stop();
    }

    public interface ConnectionListener {
        char getCommandType();

        void getData(String data);
    }
}
