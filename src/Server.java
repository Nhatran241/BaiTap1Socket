import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;

public class Server {
    public static void main(String[] args){
        TuDienManager.getTuDienManager().loadTuDien(new TuDienManager.OnTuDienListener() {
            @Override
            public void onSuccess(String kequa) {
                new TcpServer(2401) {
                    @Override
                    protected void onAcceptSocketFail(IOException exception) {
                        System.out.println("Serverlog| Accept Socket Fail :"+exception.toString());
                    }

                    @Override
                    protected void onServerError(IOException exception) {
                        System.out.println("Serverlog| Server Error :"+exception.toString());
                    }

                    @Override
                    protected void onServerDestroyClientFail(Exception exception) {
                        System.out.println("Serverlog| Server Destroy Client Fail :"+exception.toString());
                    }

                    @Override
                    protected void onClientConnectSuccess(InetAddress inetAddress) {
                        System.out.println("Serverlog| Client "+ inetAddress.getHostAddress() +" connected");

                    }

                    @Override
                    protected void onClientConnectFailed(IOException e) {
                        System.out.println("Serverlog| CLient connect failed :"+e.toString());

                    }

                    @Override
                    protected void onDisconnect(InetAddress inetAddress) {
                        System.out.println("Serverlog| Client "+inetAddress.getHostAddress()+" disconnected");
                    }

                    @Override
                    protected void onReceive(SocketTransceiver client, String message) {
                        handlerClientMessage(client,message);
                    }

                    @Override
                    protected void onServerStop() {
                        System.out.println("Serverlog| Server stop");
                    }
                }.start();
            }

            @Override
            public void onFail(Exception e) {
                System.out.println("Load từ điển thất bại");
            }
        });
        /**
         * Vì tất cả client cùng thao tác trên Hashmap nên khi 1 client gọi bye để lưu dữ liệu
         * thì sẽ không ảnh hưởng đến những client khác
         */
    }

    private static void handlerClientMessage(SocketTransceiver client, String message) {
        TuDienManager tuDienManager = TuDienManager.getTuDienManager();
        if(message.trim().toLowerCase().equals("bye")){
            TuDienManager.getTuDienManager().luuKetQua(new TuDienManager.OnTuDienListener() {
                @Override
                public void onSuccess(String kequa) {
                    client.send(kequa);
                    client.stop();
                }

                @Override
                public void onFail(Exception e) {
                    client.send(e.getMessage());
                }
            });
        }else {
            /**
             * trường hợp này có thể là lệnh thêm xóa hoặc tra từ điển
             */
            String[] arrayMessage = message.split(";");
            if(arrayMessage.length==1){
                /**
                 * message không có ';' tức là cần tra từ điển
                 */
                tuDienManager.traTuDien(arrayMessage[0], new TuDienManager.OnTuDienListener() {

                    @Override
                    public void onSuccess(String kequa) {
                        client.send(kequa);
                    }

                    @Override
                    public void onFail(Exception e) {
                        client.send(e.getMessage());
                    }
                });
            }else if (arrayMessage.length==2&&arrayMessage[0].equals("del")&&!arrayMessage[1].isEmpty()){
                /**
                 * xóa từ
                 */
                tuDienManager.xoaTuDien(arrayMessage[1], new TuDienManager.OnTuDienListener() {

                    @Override
                    public void onSuccess(String kequa) {
                        client.send(kequa);
                    }

                    @Override
                    public void onFail(Exception e) {
                        client.send(e.getMessage());
                    }
                });
            }else if(arrayMessage.length==3&&arrayMessage[0].equals("add")&&!arrayMessage[1].isEmpty()&&!arrayMessage[2].isEmpty()){
                /**
                 * Thêm từ
                 */
                tuDienManager.themTuDien(arrayMessage[1], arrayMessage[2], new TuDienManager.OnTuDienListener() {

                    @Override
                    public void onSuccess(String kequa) {
                        client.send(kequa);

                    }

                    @Override
                    public void onFail(Exception e) {
                        client.send(e.getMessage());

                    }
                });
            }else {
                client.send("Sai cú pháp vui lòng nhập lại");
            }

        }
    }



}
