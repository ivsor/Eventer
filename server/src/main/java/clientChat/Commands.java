package clientChat;

import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;

/**
 * Created by user on 30.04.2018.
 */
public class Commands {
    // Create a new RestTemplate instance
    public static String IP = "192.168.180.58:8080";
    static int serverPort = 6667; // здесь обязательно нужно указать порт к которому привязывается сервер.
    static String address = "192.168.180.58"; // это IP-адрес компьютера, где исполняется наша серверная программа.
    public static Long createUser(String name, String email, String password) {
        // The connection URL
        String url = "http://" + IP + "/add?name=" + name + "&email=" + email + "&password=" + password;
// Add the String message converter
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new StringHttpMessageConverter());

// Make the HTTP GET request, marshaling the response to a String
        Long result = restTemplate.getForObject(url, Long.class);

        System.out.println(result);
        return result;
    }

    public static String createEvent(Integer maxPeople, String name, String description, String place) {
        String url = "http://" + IP + "/createEvent?maxPeople=" + maxPeople +
                "&name=" + name +
                "&description=" + description +
                "&place=" + place;
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
        String s = restTemplate.getForObject(url, String.class);
        System.out.println(s);
        return s;
    }

    /* public static void makeConnection() {
         WebSocketClient webSocketClient = new StandardWebSocketClient();
         WebSocketStompClient stompClient = new WebSocketStompClient(webSocketClient);
         stompClient.setMessageConverter(new MappingJackson2MessageConverter());
         stompClient.setTaskScheduler(new ConcurrentTaskScheduler());
         String urlChat = "ws://192.168.180.58:8080/hello";
         handler2 sessionHandler = new handler2();
         sessionHandler.addListener(new Reciever());
         sessionHandler.addListener(new Sender());
         stompClient.connect(urlChat, sessionHandler);

     }*/

    public static void makeConnection(){
        try {
            InetAddress ipAddress = InetAddress.getByName(address); // создаем объект который отображает вышеописанный IP-адрес.
            System.out.println("Any of you heard of a socket with IP address " + address + " and port " + serverPort + "?");
            Socket socket = new Socket(ipAddress, serverPort); // создаем сокет используя IP-адрес и порт сервера.
            System.out.println("Yes! I just got hold of the program.");

            // Берем входной и выходной потоки сокета, теперь можем получать и отсылать данные клиентом.
            InputStream sin = socket.getInputStream();
            OutputStream sout = socket.getOutputStream();

            // Конвертируем потоки в другой тип, чтоб легче обрабатывать текстовые сообщения.
            DataInputStream in = new DataInputStream(sin);
            DataOutputStream out = new DataOutputStream(sout);

            // Создаем поток для чтения с клавиатуры.
            BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
            String line = null;
            System.out.println("Type in something and press enter. Will send it to the server and tell ya what it thinks.");
            System.out.println();
            new Thread(() -> {
                while (true) {
                    String line2 = null; // ждем пока сервер отошлет строку текста.
                    try {
                        line2 = in.readUTF();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("The server was very polite. It sent me this : " + line2);

                }
            }).start();
            while (true) {
                line = keyboard.readLine(); // ждем пока пользователь введет что-то и нажмет кнопку Enter.
                System.out.println("Sending this line to the server...");
                out.writeUTF(line); // отсылаем введенную строку текста серверу.
                out.flush(); // заставляем поток закончить передачу данных.

            }
        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    public static void update() {
        System.out.println("Ебать пацаны обновились");
    }

}