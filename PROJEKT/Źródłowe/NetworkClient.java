import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class NetworkClient {
    public static void main(String[] args) {

        Socket socket = null;
        BufferedReader in = null;
        PrintWriter out = null;
        try {
            socket = new Socket(args[3].split(":")[0], Integer.parseInt(args[3].split(":")[1]));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        }
        catch (UnknownHostException e) {
            System.out.println("Unknown host");
            System.exit(-1);
        }
        catch  (IOException e) {
            System.out.println("No I/O");
            System.exit(-1);
        }
        String tmp=" ";
        for(int i=4;i<args.length;i++) {
            tmp+=(args[i]+" ");
        }
        out.println(args[1]+tmp);
        String line;
        while (true) {
            try {
                if (!((line = in.readLine()) != null && !line.isEmpty())) break;
                System.out.println(line);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
