package finalproject.client;

import finalproject.entities.Person;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * ClassName:clientDemo
 * PackgeName:finalproject.client
 * Description:
 *
 * @Date:2020-05-09 00:52
 * Author:ningzhy3@gmail.com
 */
public class clientDemo {

    public static void main(String[] args) {
        Socket socket = null;
        ObjectInputStream is = null;
        ObjectOutputStream os = null;


        try{
            

            System.out.println(2);
            os = new ObjectOutputStream(socket.getOutputStream());
            Person person = new Person("jj","redick","texs",40,1,33);

            os.writeObject(person);
            os.flush();


        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                is.close();
            } catch(Exception ex) {}
            try {
                os.close();
            } catch(Exception ex) {}
            try {
                socket.close();
            } catch(Exception ex) {}

        }
    }
}
