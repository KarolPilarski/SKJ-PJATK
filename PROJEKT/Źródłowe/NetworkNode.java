import java.io.IOException;
import java.net.*;


public class NetworkNode {
    private static int tcpPort;
    private static String gatewayAddress;
    private static int gatewayPort;
    private static int nodeId;
    private static int[] freeResources = new int[26];


    public static void main(String[] args) throws UnknownHostException {
        gatewayAddress = null;

        //PODSTAWOWA WERYFIKACJA SKLADNI
        if (args.length < 4) {
            System.err.println("Niepoprawna składnia: ilość argumentów");
            System.exit(1);
        }
        if (args[0].equals("-ident")) {
            nodeId = Integer.parseInt(args[1]);
        } else {
            System.err.println("Niepoprawna składnia: identyfikator");
            System.exit(2);
        }
        if (args[2].equals("-tcpport")) {
            tcpPort = Integer.parseInt(args[3]);
        } else {
            System.err.println("Niepoprawna składnia: port węzła");
            System.exit(4);
        }
        int count = 4;
        String tmp[];
        if (args[count].equals("-gateway")) {
            tmp = args[count + 1].split(":");
            gatewayAddress = tmp[0];
            gatewayPort = Integer.parseInt(tmp[1]);
            count = count + 2;
        }
        for (int i = 0; i < freeResources.length; i++) {
            freeResources[i] = 0;
        }
        while (count < (args.length)) {
            tmp = args[count].split(":");
            freeResources[tmp[0].toCharArray()[0] - 65] += Integer.parseInt(tmp[1]);
            count++;
        }

        //INICJACJA DANEGO TYPU WEZLA
        if (gatewayAddress == null) new RootNode(nodeId, tcpPort, freeResources);
        else new BranchNode(nodeId, tcpPort, gatewayAddress, gatewayPort, freeResources);
    }

}

