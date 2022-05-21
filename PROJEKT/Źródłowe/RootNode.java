import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;

public class RootNode {
    int NodeId;
    int tcpPort;
    volatile int[] freeResources;
    volatile int[] webFreeResources;
    volatile LinkedList<NodeInfo> Branches;
    volatile LinkedList<AllocatedValue> allocatedValues;

    public RootNode(int NodeId,int tcpPort, int[] freeResources) {
        this.NodeId = NodeId;
        this.tcpPort = tcpPort;
        this.freeResources = freeResources.clone();
        this.webFreeResources=freeResources.clone();

        Branches = new LinkedList<>();
        allocatedValues= new LinkedList<>();

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
                        String[] msg = line.split(" ");
                        if (msg[0].equals("node")) {
                            if (msg[1].equals("new")) {
                                //DODAWANIE NOWEGO SERWERA DO SIECI
                                System.out.println("Adding node " + finalClient.getInetAddress().getHostAddress() + ":" + msg[2]);

                                boolean exists = false;

                                for (NodeInfo NI : Branches) {
                                    if (finalClient.getInetAddress().getHostAddress().equals(NI.adress) && Integer.parseInt(msg[2]) == NI.port)
                                        exists = true;
                                }


                                if (!exists) {
                                    int[] branchFreeResources = new int[26];
                                    for (int i = 0; i < branchFreeResources.length; i++) {
                                        branchFreeResources[i] = 0;
                                    }

                                    for (int i = 4; i < msg.length; i++) {
                                        webFreeResources[msg[i].split(":")[0].toCharArray()[0] - 65] += Integer.parseInt(msg[i].split(":")[1]);
                                        branchFreeResources[msg[i].split(":")[0].toCharArray()[0] - 65] += Integer.parseInt(msg[i].split(":")[1]);
                                    }

                                    Branches.add(new NodeInfo(finalClient.getInetAddress().getHostAddress(), Integer.parseInt(msg[2]), branchFreeResources));


                                    //WYSYLANIE ADRESU SERWERA DO NOWEGO WEZLA
                                    if (!msg[2].equals(finalClient.getPort() + "")) {
                                        Socket socketAddRoot = null;
                                        PrintWriter outAddRoot = null;
                                        try {
                                            socketAddRoot = new Socket(msg[3], Integer.parseInt(msg[2]));
                                            outAddRoot = new PrintWriter(socketAddRoot.getOutputStream(), true);
                                        } catch (UnknownHostException e) {
                                            System.out.println("Unknown host");
                                            System.exit(-1);
                                        } catch (IOException e) {
                                            System.out.println("No I/O");
                                            System.exit(-1);
                                        }
                                        outAddRoot.println("node addRoot " + tcpPort);
                                    }
                                }
                            } else System.out.println("Adres i port tego węzła są już zajęte");
                        }else if(line.equals("TERMINATE")){

                            for(NodeInfo NI:Branches) {
                                Socket socketAllocate = null;
                                PrintWriter outAllocate = null;
                                try {
                                    socketAllocate = new Socket(NI.adress, NI.port);
                                    outAllocate = new PrintWriter(socketAllocate.getOutputStream(), true);
                                } catch (UnknownHostException e) {
                                    System.out.println("Unknown host");
                                    System.exit(-1);
                                } catch (IOException e) {
                                    System.out.println("No I/O");
                                    System.exit(-1);
                                }
                                outAllocate.println("node terminate");
                                socketAllocate.close();
                            }
                            System.out.println("Terminating..");
                            System.exit(0);

                        }else{
                            //ODEBRANIE ZADANIA OD KLIENTA
                            System.out.println("Processing client: "+line.split(" ")[0]);
                            int[] resourcesToAllocate = new int[26];

                            //SPRAWDZENIE CZY ZADANE ZASOBY SĄ DOSTEPNE
                            for(int i=0; i<resourcesToAllocate.length;i++){
                                resourcesToAllocate[i]=0;
                            }
                            boolean fit=true;
                            String[] values = line.split(" ");
                            for(int i=1;i<values.length;i++){
                                resourcesToAllocate[values[i].split(":")[0].toCharArray()[0]-65]+=Integer.parseInt(values[i].split(":")[1]);
                            }
                            for(int i=0; i<resourcesToAllocate.length;i++){
                                if(resourcesToAllocate[i]>webFreeResources[i]) fit=false;
                            }
                            //ALOKACJA
                            if(fit){
                                out.println("ALLOCATED");
                                for(int i=0;i<resourcesToAllocate.length;i++){
                                    //ALOKACJA NA SERWERZE ROOT
                                    if(freeResources[i]>0&&resourcesToAllocate[i]>0){
                                        if(freeResources[i]>=resourcesToAllocate[i]){
                                            System.out.println("Allocating "+resourcesToAllocate[i]+" pieces of "+(char)('A'+i)+" for client "+values[0]+".");
                                            allocatedValues.add(new AllocatedValue((char)('A'+i),resourcesToAllocate[i],Integer.parseInt(values[0])));
                                            out.println((char)('A'+i)+":"+resourcesToAllocate[i]+":"+ InetAddress.getLocalHost().getHostAddress()+":"+tcpPort);
                                            freeResources[i]-=resourcesToAllocate[i];
                                            webFreeResources[i]-=resourcesToAllocate[i];
                                            resourcesToAllocate[i]=0;
                                        }else{
                                            System.out.println("Allocating "+freeResources[i]+" pieces of "+(char)('A'+i)+" for client "+values[0]+".");
                                            allocatedValues.add(new AllocatedValue((char)('A'+i),freeResources[i],Integer.parseInt(values[0])));
                                            out.println((char)('A'+i)+":"+freeResources[i]+":"+ InetAddress.getLocalHost().getHostAddress()+":"+tcpPort);
                                            resourcesToAllocate[i]-=freeResources[i];
                                            webFreeResources[i]-=freeResources[i];
                                            freeResources[i]=0;
                                        }
                                    }
                                    //ALOKACJA NA POZOSTALYCH SERWERACH
                                    if(resourcesToAllocate[i]>0){
                                        for(NodeInfo NI:Branches){
                                            if(NI.freeResources[i]>0&&resourcesToAllocate[i]>0){
                                                if(NI.freeResources[i]>=resourcesToAllocate[i]){
                                                    Socket socketAllocate = null;
                                                    PrintWriter outAllocate = null;
                                                    try {
                                                        socketAllocate = new Socket(NI.adress, NI.port);
                                                        outAllocate = new PrintWriter(socketAllocate.getOutputStream(), true);
                                                    }
                                                    catch (UnknownHostException e) {
                                                        System.out.println("Unknown host");
                                                        System.exit(-1);
                                                    }
                                                    catch  (IOException e) {
                                                        System.out.println("No I/O");
                                                        System.exit(-1);
                                                    }
                                                    outAllocate.println("node allocate "+(char)('A'+i)+" "+resourcesToAllocate[i]+" "+values[0]);
                                                    socketAllocate.close();

                                                    out.println((char)('A'+i)+":"+resourcesToAllocate[i]+":"+ NI.adress+":"+NI.port);
                                                    NI.freeResources[i]-=resourcesToAllocate[i];
                                                    webFreeResources[i]-=resourcesToAllocate[i];
                                                    resourcesToAllocate[i]=0;
                                                }else{
                                                    Socket socketAllocate = null;
                                                    PrintWriter outAllocate = null;
                                                    try {
                                                        socketAllocate = new Socket(NI.adress, NI.port);
                                                        outAllocate = new PrintWriter(socketAllocate.getOutputStream(), true);
                                                    }
                                                    catch (UnknownHostException e) {
                                                        System.out.println("Unknown host");
                                                        System.exit(-1);
                                                    }
                                                    catch  (IOException e) {
                                                        System.out.println("No I/O");
                                                        System.exit(-1);
                                                    }
                                                    outAllocate.println("node allocate "+(char)('A'+i)+" "+NI.freeResources[i]+" "+values[0]);
                                                    socketAllocate.close();

                                                    out.println((char)('A'+i)+":"+NI.freeResources[i]+":"+ NI.adress+":"+NI.port);
                                                    resourcesToAllocate[i]-=NI.freeResources[i];
                                                    webFreeResources[i]-=NI.freeResources[i];
                                                    NI.freeResources[i]=0;
                                                }
                                            }
                                        }
                                    }
                                }
                            }else out.println("FAILED");
                        }
                    }
                    finalClient.close();
                } catch (IOException e1) {
                    System.err.println("IO Exception");
                }
            }).start();
        }

    }
}
