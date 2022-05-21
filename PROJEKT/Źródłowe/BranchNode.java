import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;

public class BranchNode {
    int NodeId;
    int tcpPort;
    String gatewayAddress;
    int gatewayPort;
    volatile NodeInfo rootAddress=null;
    volatile LinkedList<AllocatedValue> allocatedValues;

    public BranchNode(int NodeId,int tcpPort, String gatewayAddress, int gatewayPort, int[] freeResources) throws UnknownHostException {
        this.NodeId = NodeId;
        this.tcpPort = tcpPort;
        this.gatewayAddress = gatewayAddress;
        this.gatewayPort = gatewayPort;

        allocatedValues=new LinkedList<>();


        //PODLACZENIE WEZLA DO SIECI
        Socket socketCon = null;
        PrintWriter outCon = null;
        try {
            socketCon = new Socket(gatewayAddress, gatewayPort);
            outCon = new PrintWriter(socketCon.getOutputStream(), true);
        }
        catch (UnknownHostException e) {
            System.out.println("Unknown host");
            System.exit(-1);
        }
        catch  (IOException e) {
            System.out.println("No I/O");
            System.exit(-1);
        }
        String res=" ";
        for(int i=0;i<freeResources.length;i++){
            if(freeResources[i]!=0)res+=((char)('A'+i)+":"+freeResources[i]+" ");
        }
        outCon.println("node new "+tcpPort+" "+InetAddress.getLocalHost().getHostAddress()+res);

        try {
            socketCon.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //INICJACJA SERWERA
        ServerSocket server = null;
        Socket client = null;
        try {
            server = new ServerSocket(tcpPort);
        }
        catch (IOException e) {
            System.out.println("Could not listen");
            System.exit(-1);
        }
        System.out.println("Server listens on port: " + server.getLocalPort());
        while(true) {
            try {
                client = server.accept();
            }
            catch (IOException e) {
                System.out.println("Accept failed");
                System.exit(-1);
            }


            Socket finalClient = client;
            new Thread(()->{
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(finalClient.getInputStream()));
                    PrintWriter out = new PrintWriter(finalClient.getOutputStream(), true);
                    String line;
                    if ((line = in.readLine()) != null && !line.isEmpty()) {
                        String[] msg=line.split(" ");
                        if(msg[0].equals("node")){
                            if(msg[1].equals("new")){
                                //PRZEKAZANIE DANYCH NOWEGO WEZLA DO WEZLA GLOWNEGO
                                System.out.println("Forwarding node " +finalClient.getInetAddress().toString().substring(1)+":"+msg[2]+" to "+gatewayAddress+":"+gatewayPort);
                                Socket socketForward = null;
                                PrintWriter outForward = null;
                                try {
                                    if(rootAddress!=null){
                                        socketForward = new Socket(rootAddress.adress, rootAddress.port);
                                    }else {
                                        socketForward = new Socket(gatewayAddress, gatewayPort);
                                    }
                                    outForward = new PrintWriter(socketForward.getOutputStream(), true);
                                }
                                catch (UnknownHostException e) {
                                    System.out.println("Unknown host");
                                    System.exit(-1);
                                }
                                catch  (IOException e) {
                                    System.out.println("No I/O");
                                    System.exit(-1);
                                }
                                outForward.println(line);

                                socketForward.close();
                            }else if(msg[1].equals("addRoot")){
                                //DODAWANIE ADRESU GLOWNEGO WEZLA
                                rootAddress=new NodeInfo(finalClient.getInetAddress().toString().substring(1),Integer.parseInt(msg[2]));
                                System.out.println("Root: "+rootAddress);
                            }else if(msg[1].equals("allocate")){
                                //ALOKACJA ZASOBOW
                                System.out.println("Allocating "+msg[3]+" pieces of "+msg[2]+" for client "+msg[4]);
                                allocatedValues.add(new AllocatedValue(msg[2].toCharArray()[0],Integer.parseInt(msg[3]),Integer.parseInt(msg[4])));

                            }else if(msg[1].equals("terminate")){
                                //kończenie pracy
                                System.out.println("Terminating..");
                                System.exit(0);

                            }
                        }else{
                            //USTANAWIANIE POLACZENIA MIEDZY KLIENTEM A GLOWNYM WEZLEM


                            //JESLI WEZEL NIE ZDAZYL POLACZYC SIE DO SIECI PRZEKAZUJE ZAPYTANIE DO SWOJEJ BRAMY
                            if(rootAddress==null){
                                rootAddress=new NodeInfo(gatewayAddress,gatewayPort);
                            }

                            Socket socketForward = null;
                            BufferedReader inForward = null;
                            PrintWriter outForward = null;
                            try {
                                socketForward = new Socket(rootAddress.adress, rootAddress.port);
                                inForward = new BufferedReader(new InputStreamReader(socketForward.getInputStream()));
                                outForward = new PrintWriter(socketForward.getOutputStream(), true);
                            }
                            catch (UnknownHostException e) {
                                System.out.println("Unknown host");
                                System.exit(-1);
                            }
                            catch  (IOException e) {
                                System.out.println("No I/O");
                                System.exit(-1);
                            }
                            outForward.println(line);
                            String lineServer;
                            while (true) {
                                try {
                                    if (!((lineServer = inForward.readLine()) != null && !lineServer.isEmpty())) break;
                                    out.println(lineServer);

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }

                            socketForward.close();
                        }
                    }
                    finalClient.close();
                } catch (IOException e1) {
                    // do nothing
                }
            }).start();
        }

    }
}
