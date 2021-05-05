import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringJoiner;

public class ChatClient implements Runnable {
    private final Logger logger;
    private String name;
    private ArrayList<ChatClient> clients;
    private Socket client;
    private BufferedReader reader;
    private PrintWriter printWriter;


    public ChatClient(ArrayList<ChatClient> clients, Socket client, Logger logger,
                      HashMap<String, ChatClient> clientMap) {
        this.clients = clients;
        this.client = client;
        this.logger = logger;

        try {
            reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            printWriter = new PrintWriter(new OutputStreamWriter(client.getOutputStream()));

            sendMessage("Please state your name: ");
            name = reader.readLine();

            this.broadcast(this.name + " has entered the room.", true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void sendMessage(String message) {
        printWriter.println(message);
        printWriter.flush();
    }

    public void close() {
        try {
            if (printWriter != null) {
                printWriter.flush();
                printWriter.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            System.out.println("interrupting thread....");
            this.clients.remove(this); //threadsafety??
            this.broadcast(this.name + " has left the room.", true);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }

    public String printClients(){
        StringJoiner clients = new StringJoiner(", ");
        for (ChatClient c: this.clients) {
            if (!c.name.equals(this.name)){
                clients.add(c.getName());
            }
        }
        return clients.toString();
    }


    public void setName(String name) {
        this.name = name;
    }

    private void broadcast(String msg, boolean toLog){
        for (ChatClient c: this.clients) {
            if (!c.name.equals(this.name)){
                c.sendMessage(msg);
            }
        }
        if(toLog){
            logger.writeLogEntry("Broadcast from "+ this.name + ": " + msg);
        }
    }

    @Override
    public void run() {
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                String[] msg = line.split(":");
                String cmd = msg[0];
                if (msg.length == 2) {
                    String arg = msg[1];
                    // implement name, msg
                    if (cmd.equals("<name>")) {
                        this.setName(arg);
                    } else if (cmd.equals("<msg>")) {
                        for (ChatClient c : this.clients) {
                            if (!c.name.equals(this.name)) {
                                logger.writeLogEntry("Sender: " + this.name + "; Recipient: " + c.name + ": " + arg);
                                c.sendMessage(arg);
                            }
                        }
                    } else {
                        printWriter.println("Not a command. Try <name>, <msg>, <msgto>, <bye>.");
                        printWriter.flush();
                    }
                } else if (msg.length == 3) {
                    String recipient = msg[1];
                    String message = msg[2];
                    // implement msgto
                    for (ChatClient c : clients) {
                        if (c.name.equals(recipient)) {
                            logger.writeLogEntry("Sender: " + this.name + "; Recipient: " + c.name + ": " + message);
                            c.sendMessage(message);
                        }
                    }
                } else if (msg.length == 1) {
                    if (cmd.equals("<bye>")) {
                        close();
                        break;
                    }
                    else if(cmd.equals("<list>")){
                        this.sendMessage(printClients());
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return;
    }
}
