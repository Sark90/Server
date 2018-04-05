public class Server {   //abstract

    private static ServerThread[] stArr = new ServerThread[2];  //[10]

    public static void run() {
        for (int i=0; i<stArr.length; i++) {
            stArr[i] = new ServerThread("ServerThread " + i);
        }
    }

    public static void sendMessage(String client, String message) {
        for (ServerThread st: stArr) {
            if (st.getName().equals(client)) {
                st.sendMessage(message);
            }
        }
    }
}
