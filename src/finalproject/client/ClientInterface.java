package finalproject.client;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.sql.*;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;

import finalproject.client.ClientInterface.ComboBoxItem;
import finalproject.db.DBInterface;
import finalproject.entities.Person;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.Socket;

public class ClientInterface extends JFrame {

    private static final long serialVersionUID = 1L;
    public static final int DEFAULT_PORT = 8001;
    private static final int FRAME_WIDTH = 600;
    private static final int FRAME_HEIGHT = 400;
    final int AREA_ROWS = 10;
    final int AREA_COLUMNS = 40;

    private Connection conn;
// GUI components
    private JMenuBar menuBar;

    JLabel dbName;
    JLabel activeDb;

    JLabel activeConn;
    JLabel con;

    JButton openButton;
    JButton closeButton;

    JButton sendDataButton;
    JButton queryDBButton;


    JTextArea showArea;

    queryButtonListener queryListener;

    private PreparedStatement queryStmt;

    JComboBox peopleSelect;
    JFileChooser jFileChooser;
    Socket socket;


    int port;

    public ClientInterface() {
        this(DEFAULT_PORT);
    }

    public ClientInterface(int port) {
        this.port = port;
        jFileChooser = new JFileChooser(".");
        createMenus();
        creatPanel();


    }
//create the GUI
    private void creatPanel() {
        JPanel topPanel = new JPanel(new GridLayout(5, 1));

        JPanel controlPanel1 = new JPanel();
        activeDb = new JLabel("Active DB: ");
        dbName = new JLabel("<None>");
        controlPanel1.add(activeDb);
        controlPanel1.add(dbName);
        topPanel.add(controlPanel1);

        JPanel controlPanel2 = new JPanel();
        activeConn = new JLabel("Active Connection: ");
        con = new JLabel("<None>");
        controlPanel2.add(activeConn);
        controlPanel2.add(con);
        topPanel.add(controlPanel2);

        JPanel controlPanel4 = new JPanel();
        peopleSelect = new JComboBox();
        peopleSelect.addItem("<Empty>");

        peopleSelect.setSize(40, 10);
        controlPanel4.add(peopleSelect);
        topPanel.add(controlPanel4);


        JPanel controlPanel = new JPanel();
        openButton = new JButton("Open Connection");
        closeButton = new JButton("Close Connection");
        controlPanel.add(openButton);
        controlPanel.add(closeButton);
        topPanel.add(controlPanel);

        JPanel controlPanel3 = new JPanel();
        sendDataButton = new JButton("Send Data");
        queryDBButton = new JButton("Query DB Data");
        controlPanel3.add(sendDataButton);
        controlPanel3.add(queryDBButton);
        topPanel.add(controlPanel3);

        queryDBButton.addActionListener(new queryButtonListener());
        sendDataButton.addActionListener(new SendButtonListener());

        showArea = new JTextArea();
        showArea.setEditable(false);

        JScrollPane listScroller = new JScrollPane(showArea);
        listScroller.setPreferredSize(new Dimension(250, 80));
        add(listScroller);



        this.add(topPanel, BorderLayout.NORTH);
//        this.add(showArea, BorderLayout.CENTER);
        closeButton.addActionListener((e) -> {
            try {
                socket.close();
                showArea.append("connection closed");
                con.setText("<None>");
            } catch (Exception e1) {
                System.err.println("error");
            }
        });
        openButton.addActionListener(new OpenConnectionListener());
        setSize(800, 400);
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
        menu.add(createFileOpenItem());
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


    private void fillComboBox() throws SQLException {
        List<ComboBoxItem> l = getNames();

        peopleSelect.setModel(new DefaultComboBoxModel(l.toArray()));

    }

    private void connectToDB(String fileName) {
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:"+fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JMenuItem createFileOpenItem() {
        JMenuItem item = new JMenuItem("Open DB");
        class OpenDBListener implements ActionListener {
            public void actionPerformed(ActionEvent event) {
                int returnVal = jFileChooser.showOpenDialog(getParent());
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    System.out.println("You chose to open this file: " + jFileChooser.getSelectedFile().getAbsolutePath());
                    String dbFileName = jFileChooser.getSelectedFile().getAbsolutePath();
                    try {
                        connectToDB(dbFileName);
                        showArea.append("Connected to database" + "\n");

                        dbName.setText(dbFileName.substring(dbFileName.lastIndexOf("/") + 1));

                        queryListener = new queryButtonListener();
                        queryListener.setConnection(conn);
//                        clearComboBox();
                        fillComboBox();

                    } catch (Exception e) {
                        System.err.println("error connection to db: \n" + e.getMessage());
                        e.printStackTrace();
                        dbName.setText("<None>");
//						clearComboBox();
                    }

                }
            }
        }

        item.addActionListener(new OpenDBListener());
        return item;
    }

    class queryButtonListener implements ActionListener {
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

        public void setConnection(Connection c) {
            try {
                conn = c;
                queryStmt = conn.prepareStatement("SELECT * FROM People");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    class SendButtonListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {

            ObjectOutputStream os = null;
            ObjectInputStream is = null;

            try {
                // responses are going to come over the input as text, and that's tricky,
                // which is why I've done that for you:

                os = new ObjectOutputStream(socket.getOutputStream());

                // now, get the person on the object dropdownbox we've selected
                ComboBoxItem personEntry = (ComboBoxItem) peopleSelect.getSelectedItem();
                String lastName = personEntry.getName();

                // That's tricky which is why I have included the code. the personEntry
                // contains an ID and a name. You want to get a "Person" objedatabasect out of that
                // which is stored in the
                String sql = "SELECT *FROM People WHERE last = ?";
                PreparedStatement selectStmt = conn.prepareStatement(sql);
                selectStmt.setString(1, lastName);


                ResultSet rset = selectStmt.executeQuery();

                String firstNameTemp = rset.getString(1);
                String lastNameTemp = rset.getString(2);
                int ageTemp = rset.getInt(3);
                String cityTemp = rset.getString(4);

                int sentTemp = rset.getInt(5);
                int idTemp = rset.getInt(6);

                Person pTemp = new Person(firstNameTemp, lastNameTemp, cityTemp, ageTemp, sentTemp, idTemp);


                os.writeObject(pTemp);
                os.flush();

                // Send the person object here over an output stream that you got from the socket.
//                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                is = new ObjectInputStream(socket.getInputStream());


                Object obj = is.readObject();
                String response = obj.toString();
//                System.out.println(response);
//                String response = br.readLine();


                if (response.contains("Success")) {
                    System.out.println("Success");
                    showArea.append("Success" + "\n");

                    PreparedStatement update = conn.prepareStatement("UPDATE People SET sent = 1 WHERE last = ?");
                    update.setString(1, lastName);

                    update.execute();

                    fillComboBox(); // update combobox

                    // what do you do after we know that the server has successfully
                    // received the data and written it to its own database?
                    // you will have to write the code for that.
                } else {
                    System.out.println("Failed");
                }

            } catch (Exception ee) {
                ee.printStackTrace();
            } finally {

                try {
//                    is.close();
                } catch (Exception ex) {
                }
                try {
//                    os.close();
                } catch (Exception ex) {
                }
                try {
//                    socket.close();
                } catch (Exception ex) {
                }

            }

        }


    }

    class OpenConnectionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {


            // TODO Auto-generated method stub
            try {
                socket = new Socket("localhost", 8001);
                con.setText("localhost::8001");
                showArea.append("connected to server\n");
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
                showArea.append("connection Failure\n");
            }
        }
    }


    private List<ComboBoxItem> getNames() throws SQLException {
        List<ComboBoxItem> l = new ArrayList<>();

        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM PEOPLE WHERE sent = 0");

        ResultSet rset = stmt.executeQuery();
        ResultSetMetaData rsmd = rset.getMetaData();

        rset = stmt.executeQuery();

        int numColumns = rsmd.getColumnCount();

        while (rset.next()) {

            int tempId = 0;
            String tempLast = "";

            for (int i = 1; i <= numColumns; i++) {
                Object o = rset.getObject(i);
                if (i == 2) {
                    tempLast = o.toString();
                }
                if (i == 6) {
                    tempId = Integer.parseInt(o.toString());
                }
            }
            ComboBoxItem temp = new ComboBoxItem(tempId, tempLast);
            l.add(temp);
        }

        return l;
    }

    // a JComboBox will take a bunch of objects and use the "toString()" method
// of those objects to print out what's in there.
// So I have provided to you an object to put people's names and ids in
// and the combo box will print out their names.
// now you will want to get the ComboBoxItem object that is selected in the combo box
// and get the corresponding row in the People table and make a person object out of that.
    class ComboBoxItem {
        private int id;
        private String name;

        public ComboBoxItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return this.id;
        }

        public String getName() {
            return this.name;
        }

        public String toString() {
            return this.name;
        }
    }

    /* the "open db" menu item in the client should use this ActionListener */
    class OpenDBListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            int returnVal = jFileChooser.showOpenDialog(getParent());
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                System.out.println("You chose to open this file: " + jFileChooser.getSelectedFile().getAbsolutePath());
                String dbFileName = jFileChooser.getSelectedFile().getAbsolutePath();
                try {
                    /* now that you have the dbFileName, you should probably connect to the DB */
                    /* maybe think about filling the contents of the dropdown box listing names
                     * and indicating the name of the Active DB
                     */

                } catch (Exception e) {
                    System.err.println("error connection to db: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        ClientInterface ci = new ClientInterface();
        ci.setVisible(true);
    }
}
