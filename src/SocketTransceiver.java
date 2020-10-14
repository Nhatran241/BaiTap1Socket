import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

public abstract class SocketTransceiver implements Runnable {
    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private InetAddress inetAddress;
    private boolean runFlag =false;

    public void start(){
        this.runFlag = true;
        new Thread(this).start();
    }

    public boolean isRunFlag() {
        return runFlag;
    }

    public void stop(){
        this.runFlag = false;
    }

    public InetAddress getInetAddress() {
        return inetAddress;
    }

    public SocketTransceiver(Socket socket) {
        this.socket = socket;
        inetAddress = socket.getInetAddress();
    }
    public boolean send(String message) {
        if (outputStream != null) {
            try {
                byte[] data=message.getBytes("UTF-8");
                outputStream.writeInt(data.length);
                outputStream.write(data);
                outputStream.flush();
                return true;
            } catch (Exception exception){
            }
        }
        return false;
    }
    @Override
    public void run() {
        try {
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());
            onSocketConnectSuccess(inetAddress);
        } catch (Exception e) {
            runFlag = false;
        }

        while (runFlag){
            /**
             * Read data
             */
            try {
                int length=inputStream.readInt();
                if(length>0) {
                    byte[] data = new byte[length];
                    inputStream.readFully(data);
                    String message = new String(data, "UTF-8");
                    this.onSocketReceiver(inetAddress, message);
                }
            } catch (Exception e) {
                /**
                 * Server hoặc Client đóng connect
                 */
                runFlag = false;
            }

        }
        try {
            inputStream.close();
            outputStream.close();
            socket.close();
            inputStream = null;
            outputStream = null;
            socket = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        onSocketDisconnect(inetAddress);
    }

    protected abstract void onSocketConnectSuccess(InetAddress inetAddress);

    protected abstract void onSocketConnectFail(IOException e);

    protected abstract void onSocketDisconnect(InetAddress inetAddress);

    public abstract void onSocketReceiver(InetAddress inetAddress, String message);

}
