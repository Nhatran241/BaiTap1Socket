import java.io.IOException;
import java.net.InetAddress;
import java.util.Scanner;

public class Client {
    public static void main(String[] args){
        Scanner myScanner = new Scanner(System.in, "UTF-8");
        TcpClient tcpClient=new TcpClient("localhost", 2401) {
            @Override
            protected void onDisconnect() {
                System.out.println("Disconnected");
            }

            @Override
            protected void onClientError(Exception e) {
                System.out.println("Không connect được tới server");
            }

            @Override
            protected void onClientStop() {
            }

            @Override
            protected void onClientConnectSuccess(SocketTransceiver socketTransceiver) {
                System.out.println("Connected to "+socketTransceiver.getInetAddress());
               new Thread(() -> {
                    while (socketTransceiver.isRunFlag()){
                        String message = myScanner.nextLine().trim().toLowerCase();
                        if(message.length()!=0){
                            socketTransceiver.send(message);
                        }
                    }
                }).start();
            }

            @Override
            protected void onClientConnectFailed(IOException e) {

            }

            @Override
            protected void onReceive(SocketTransceiver client, String message) {
                System.out.println("Ket qua :"+message);
            }
        };
        tcpClient.connect();
    }
}
