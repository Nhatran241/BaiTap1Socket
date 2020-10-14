import com.sun.net.httpserver.Authenticator;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public abstract class TcpServer implements Runnable {
    private List<SocketTransceiver> listClient = new ArrayList<>();
    private int serverPort = 80;
    private boolean runFlag;
;
    public TcpServer() {
    }
    public TcpServer(int serverPort) {
        this.serverPort = serverPort;
    }
    public void start() {
        this.runFlag = true;
        (new Thread(this)).start();
    }

    public void stop() {
        this.runFlag = false;
    }
    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(serverPort);
            while(runFlag) {
                try {
                    /**
                     * liên tục accept socket kết nối đến server
                     */
                    Socket socket = serverSocket.accept();
                    createClientConnection(socket);
                } catch (IOException exception) {
                    onAcceptSocketFail(exception);
                }
            }
            /**
             * Khi server không còn hoạt động thì hủy hết tất cả socket đang kết nối đến server
             */
            try {
                for (SocketTransceiver socketTransceiver : listClient){
                    socketTransceiver.stop();
                }
                serverSocket.close();
            } catch (Exception exception) {
                onServerDestroyClientFail(exception);
            }
        } catch (IOException exception) {
            onServerError(exception);
        }
        this.stop();
        this.onServerStop();
    }

    protected abstract void onAcceptSocketFail(IOException exception);

    protected abstract void onServerError(IOException exception);

    protected abstract void onServerDestroyClientFail(Exception exception);

    protected abstract void onClientConnectSuccess(InetAddress inetAddress);

    protected abstract void onClientConnectFailed(IOException e);

    protected abstract void onDisconnect(InetAddress inetAddress);

    protected abstract void onReceive(SocketTransceiver client, String message);

    protected abstract void onServerStop();

    private void createClientConnection(Socket socket) {
        new SocketTransceiver(socket) {
            @Override
            protected void onSocketConnectSuccess(InetAddress inetAddress) {
                listClient.add(this);
                TcpServer.this.onClientConnectSuccess(inetAddress);
            }

            @Override
            protected void onSocketConnectFail(IOException e) {
                TcpServer.this.onClientConnectFailed(e);
            }

            @Override
            protected void onSocketDisconnect(InetAddress inetAddress) {
                TcpServer.this.onDisconnect(inetAddress);

            }

            @Override
            public void onSocketReceiver(InetAddress inetAddress, String message) {
                TcpServer.this.onReceive(this,message);
            }
        }.start();
    }
}
