public class NodeInfo {
    String adress;
    int port;
    int[] freeResources;

    public NodeInfo(String adress, int port, int[] freeResources) {
        this.adress = adress;
        this.port = port;
        this.freeResources = freeResources;
    }

    public NodeInfo(String adress, int port) {
        this.adress = adress;
        this.port = port;
    }

    @Override
    public String toString() {
        return adress + ":"+ port;
    }
}
