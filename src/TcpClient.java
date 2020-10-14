import com.sun.net.httpserver.Authenticator;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public abstract class TcpClient implements Runnable {
    private int serverPort;
    private String ipHost;
    private boolean runFlag;
    private boolean isConnected=false;
    private SocketTransceiver socketTransceiver;
    public TcpClient(String ip,int serverPort) {
        this.ipHost = ip;
        this.serverPort = serverPort;
    }
    public void connect() {
        this.runFlag = true;
        (new Thread(this)).start();
    }
    public boolean isConnected() {
        return isConnected;
    }

    public void stop() {
        this.runFlag = false;
    }
    @Override
    public void run() {
        try {
            Socket socket = new Socket(ipHost, serverPort);
            socketTransceiver = new SocketTransceiver(socket) {
                @Override
                protected void onSocketConnectSuccess(InetAddress inetAddress) {
                    isConnected=true;
                    onClientConnectSuccess(this);
                }

                @Override
                protected void onSocketConnectFail(IOException e) {
                    isConnected=false;
                    onClientConnectFailed(e);
                }

                @Override
                protected void onSocketDisconnect(InetAddress inetAddress) {
                    isConnected =false;
                    onDisconnect();
                }

                @Override
                public void onSocketReceiver(InetAddress inetAddress, String message) {
                    onReceive(this,message);
                }
            };
            socketTransceiver.start();
        } catch (Exception e) {
            onClientError(e);
        }
    }

    protected abstract void onClientConnectSuccess(SocketTransceiver socketTransceiver);

    protected abstract void onDisconnect();

    protected abstract void onClientError(Exception e);

    public void disconnect() {
        if (socketTransceiver != null) {
            socketTransceiver.stop();
            socketTransceiver = null;
        }
        isConnected=false;
        onClientStop();
    }

    protected abstract void onClientStop();
    protected abstract void onClientConnectFailed(IOException e);
    protected abstract void onReceive(SocketTransceiver client, String message);


}
