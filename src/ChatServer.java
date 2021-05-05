import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ChatServer {
    public static Lock lockObj = new ReentrantLock();

    public static void main(String[] args) {
        Logger log = new Logger("./log.txt");
        HashMap<String, ChatClient> usersMap = new HashMap<>();
        ArrayList<ChatClient> users = new ArrayList<>();
        try (ServerSocket ss = new ServerSocket(5678)) {
            System.out.println("Server started");

            while (true) {
                try {
                    System.out.println("waiting for clients...");
                    Socket client = ss.accept();
                    System.out.println("Client connected");

                    ChatClient cc = new ChatClient(users, client, log, usersMap);
                    if (lockObj.tryLock()) {
                        users.add(cc);
                        lockObj.unlock();
                    }
                    System.out.println(users);
                    Thread t = new Thread(cc);
                    System.out.println("Thread started");
                    t.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.close();
    }
}
