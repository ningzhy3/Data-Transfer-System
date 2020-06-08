package finalproject.server;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.Date;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import finalproject.db.DBInterface;
import finalproject.entities.Person;

public class Server extends JFrame implements Runnable {
    //server component
    private int clientNo = 0;
    private JTextArea ta;

    //swing component
    private JMenuBar menuBar;
    private JPanel upper;
    private JLabel nameOfDb;
    private JTextArea showArea;
    private JButton queryButton;

    //database component
    private Connection conn;
    private PreparedStatement queryStmt;
    private PreparedStatement insertStmt;

    public static final int DEFAULT_PORT = 8001;
    private static final int FRAME_WIDTH = 600;
    private static final int FRAME_HEIGHT = 800;
    final int AREA_ROWS = 10;
    final int AREA_COLUMNS = 40;

    public Server() throws IOException, SQLException {
        this(DEFAULT_PORT, "server.db");
    }

    public Server(String dbFile) throws IOException, SQLException {
        this(DEFAULT_PORT, dbFile);
    }

    public Server(int port, String dbFile) throws IOException, SQLException {

        this.setSize(Server.FRAME_WIDTH, Server.FRAME_HEIGHT);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        creatPanel();
        createMenus();

        try {
            conn = DriverManager.getConnection("jdbc:sqlite:server.db");
            queryStmt = conn.prepareStatement("SELECT * FROM People");
            insertStmt = conn.prepareStatement("Insert Into People (First,last,age,city,sent,id) " +
                    "Values (?,?,?,?,?,?)");
        } catch (SQLException e) {
            System.err.println("Connection error: " + e);
            System.exit(1);
        }

        queryButton.addActionListener(new QueryButtonListener());

        Thread t = new Thread(this);
        t.start();


    }

    class QueryButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {

            try {

                PreparedStatement stmt = queryStmt;

                ResultSet rset = stmt.executeQuery();
                ResultSetMetaData rsmd = rset.getMetaData();

                int numColumns = rsmd.getColumnCount();
                System.out.println("numcolumns is " + numColumns);

                // print the column names in the first line
                String columnName = "";
                for (int i = 1; i <= numColumns; i++) {
                    columnName += rsmd.getColumnName(i) + "\t";
                }
                columnName += "\n";
                System.out.println(columnName);
                showArea.append(columnName);

                //Return all rows

                rset = queryStmt.executeQuery();

                String rowString = "";
                while (rset.next()) {
                    for (int i = 1; i <= numColumns; i++) {
                        Object o = rset.getObject(i);
                        rowString += o.toString() + "\t";
                    }
                    rowString += "\n";
                }
                System.out.print("rowString  is  " + "\n" + rowString);

                showArea.append(rowString);

            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }


    private void createMenus() {
        /* add a "File" menu with:
         * "Open" item which allows you to choose a new file
         * "Exit" item which ends the process with System.exit(0);
         * Key shortcuts are optional
         */
        menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");

        menu.add(createFileExitItem());
        menuBar.add(menu);
        this.setJMenuBar(menuBar);
    }

    public JMenuItem createFileExitItem() {
        JMenuItem item = new JMenuItem("Exit");

        class MenuItemListener implements ActionListener {
            public void actionPerformed(ActionEvent event) {
                System.exit(0);
            }
        }
        ActionListener listener = new MenuItemListener();
        item.addActionListener(listener);
        return item;
    }

    private void creatPanel() {
        JPanel labelPanel = new JPanel();
        nameOfDb = new JLabel("DB: server.db");
        labelPanel.add(nameOfDb, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        queryButton = new JButton("Query DB");
        buttonPanel.add(queryButton, BorderLayout.CENTER);

        showArea = new JTextArea(
                AREA_ROWS, AREA_COLUMNS
        );
        showArea.setEditable(false);

        JScrollPane listScroller = new JScrollPane(showArea);
        listScroller.setPreferredSize(new Dimension(250, 80));
        add(listScroller);

        upper = new JPanel(new GridLayout(2, 1));
        upper.add(labelPanel);
        upper.add(buttonPanel);
        this.add(upper, BorderLayout.NORTH);


    }

    public static void main(String[] args) {
        Server sv;
        try {
            sv = new Server("server.db");
            sv.setVisible(true);
        } catch (IOException | SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        try {
            // Create a server socket
            ServerSocket serverSocket = new ServerSocket(8001);
            showArea.append("Listening on port 8001\n");

            while (true) {
                // Listen for a new connection request
                Socket socket = serverSocket.accept();

                // Increment clientNo
                clientNo++;
                showArea.append("Starting thread for client " + clientNo +
                        " at " + new Date() + '\n');

                // Find the client's host name, and IP address
                InetAddress inetAddress = socket.getInetAddress();
                showArea.append("Client " + clientNo + "'s host name is "
                        + inetAddress.getHostName() + "\n");
                showArea.append("Client " + clientNo + "'s IP Address is "
                        + inetAddress.getHostAddress() + "\n");

                // Create and start a new thread for the connection
                new Thread(new HandleAClient(socket, clientNo)).start();
            }
        } catch (IOException ex) {
            System.err.println(ex);

        }
    }

    class HandleAClient implements Runnable {
        private Socket socket; // A connected socket
        private int clientNum;

        /**
         * Construct a thread
         */
        public HandleAClient(Socket socket, int clientNum) {
            this.socket = socket;
            this.clientNum = clientNum;
        }

        /**
         * Run a thread
         */
        public void run() {
            try {
                // Create data input and output streams

                // Continuously serve the client
                while (true) {

                    try {
                        ObjectInputStream inputFromClient = null;
                        ObjectOutputStream outputToClient = null;

                        outputToClient = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));

                        inputFromClient = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));

                        Object obj = inputFromClient.readObject();
                        Person person = (Person) obj;

                        String first = person.getFirst();
                        String last = person.getLast();
                        String city = person.getCity();
                        int sent = person.getSent();
                        int age = person.getAge();
                        int id = person.getId();

                        insertStmt.setString(1, first);
                        insertStmt.setString(2, last);
                        insertStmt.setInt(3, age);
                        insertStmt.setString(4, city);
                        insertStmt.setInt(5, 1);
                        insertStmt.setInt(6, id);
                        insertStmt.execute();
                        System.out.println("Inserted Successfully");

                        outputToClient.writeObject("Success\n");
                        outputToClient.flush();

                        showArea.append("person received from client: " + this.clientNum + " " +
                                person.toString() + '\n');
//if there are some IO or network error, break
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                        break;
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
