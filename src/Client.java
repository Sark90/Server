public class Client {
    private String login;
    private String password;
    //private ArrayList<Login> contactList;

    public Client(String login, String password) {
        this.login = login;
        this.password = password;   //TODO: encode password
        //contactList = new ArrayList<>();    // get from server / remove var
    }

    public String getLogin() {
        return login;
    }
}
