import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class ServerThread extends Thread {
    private static final int SERV_PORT = 4444;
    private static ServerSocket servers = null;
    static {
        try {
            servers = new ServerSocket(SERV_PORT);
        } catch (IOException e) {
            System.out.println("Couldn't listen to port " + SERV_PORT);
        }
    }
    private static ArrayList<Client> connectedClients = new ArrayList<>();
    private static HashMap<String, String> clientList = new HashMap<>();    // TODO: serialize
    private String sign;
    private BufferedReader in = null;
    private PrintWriter out = null;
    private Socket fromclient = null;
    private Client client;
    private Receiver receiver;
    //private static final ServerThread instance = new ServerThread();

    public class Receiver extends Thread {
        public Receiver(String sign) {
            super(sign + ".Receiver");
            start();
        }
        @Override
        public void run() {
            waitClient();
            initIO();
            authentication();
            message();
            closeIO();
        }
    }

    public ServerThread(String sign) {
        super();
        this.sign = "[" + sign + "] ";
        start();
    }
/*    public static ServerThread getInstance() {
        return instance;
    }*/
    @Override
    public void run() {
        receiver = new Receiver(sign);
        try {
            receiver.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void sendMessage(String message, String sender) {
        out.println(sender + message);
    }

    private void closeIO() {
        try {
            out.close();
            in.close();
            fromclient.close();
            servers.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initIO() {
        try {
            in = new BufferedReader(new
                    InputStreamReader(fromclient.getInputStream()));
            out = new PrintWriter(fromclient.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void waitClient() {
       { //??? while + sleep doesn't help if disconnect
            try {
                System.out.println(sign + "Waiting for a client...");
                fromclient = servers.accept();
                System.out.println(sign + "Client connected");
            } catch (IOException | NullPointerException e) {
                System.out.println(sign + "Can't accept");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
       } while (fromclient == null);
    }

    private boolean loginExists(String login) {
        if (clientList.containsKey(login)) {
            return true;
        } else return false;
    }

    private boolean authOK(String login, String password) {
        if (clientList.get(login).equals(password)) return true;
        else return false;
    }

    private boolean passwordExists(String password) {
        if (clientList.containsKey(password)) {
            return true;
        } else return false;
    }

    private void authentication() {
        try {
            String login, password, output;
            //System.out.println(SIGN + "Wait for messages");
            System.out.println(sign + "Sign in/registration");
            out.println(sign + "Sign in (registration). Type your nickname:");
//            while ((login = in.readLine()) != null) {
            login = in.readLine();
            out.println(sign + "Type your password:");
            password = in.readLine();
            if (loginExists(login)) {
                System.out.println(sign + "User " + login + " exists. Sign in.");
                while (!authOK(login, password)) {
                    out.println(sign + "Wrong password. Try again.");
                    password = in.readLine();
                }
                System.out.println(sign + "User " + login + " signed in.");
            } else {
                System.out.println(sign + "User " + login + " doesn't exist. Registration.");
                while (passwordExists(password)) {
                    out.println(sign + "Password already exists. Try another password.");
                    password = in.readLine();
                }
                addUser(login, password);
                System.out.println(sign + "User " + login + " registered.");
            }
            //if (login.equalsIgnoreCase("exit")) break;
            //out.println("from ServerThread ::: " + input);
//            }
        } catch (IOException e) {
            //e.printStackTrace();
            removeFromConnected();  //what next?
            System.out.println(e);
        }
    }

    private void removeFromConnected() {
        if (client != null && connectedClients.contains(client)) {
            System.out.println(sign + client.getLogin() + " removed from connected clients list.");
            connectedClients.remove(client);
        }
    }

    private void addUser(String login, String password) {
        clientList.put(login, password);
        //serialize clientList
        client = new Client(login, password);
        connectedClients.add(client);
        this.setName(client.getLogin());
    }

    private void message() {
        String user, message = "";
        try {
            //System.out.println(SIGN + "Wait for messages");
            System.out.println(sign + "Waiting for a message...");
            out.print(sign + "Connected users:");
            for (Client client: connectedClients) {
                out.print("\t" + client.getLogin());
            }
            out.println(". Whom are you writing to?");
            user = in.readLine();
            while (!loginExists(user)) {
                out.println(sign + "User " + user + " doesn't exist. Try another name.");
                user = in.readLine();
            }
            while (!userConnected(user)) {
                out.println(sign + "User " + user + "is not connected. Try another name.");
                user = in.readLine();
            }
            out.println(sign + "OK");
            while ((message = in.readLine()) != null) {
                Server.sendMessage(user, message, "[" + client.getLogin() + "] ");
            }
        } catch (IOException e) {
            //e.printStackTrace();
            System.out.println(sign + e);
            removeFromConnected();  //what next?
        }
    }

    private boolean userConnected(String user) {
        for (Client client: connectedClients) {
            if (client.getLogin().equals(user)) return true;
        }
        return false;
    }
}
