import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TuDienManager {
    private static TuDienManager instance;
    private File file;
    private String filePath ="dictionary.txt";
    private Map<String,String> map = new HashMap<>();

    public static TuDienManager getTuDienManager() {
        if (instance==null)
            instance = new TuDienManager();
        return instance;
    }
    public void loadTuDien(OnTuDienListener onTuDienListener){
            file = new File(filePath);
            if (!file.exists()) {
                try {
                    file.createNewFile();
                    onTuDienListener.onSuccess("load thành công");
                } catch (IOException exception) {
                    onTuDienListener.onFail(exception);
                }
            } else {
                    try {
                        List<String> lines = Files.readAllLines(Paths.get(file.toURI()), Charset.forName("UTF-8"));
                        for (String lineString : lines) {
                            String[] strings = lineString.split(";");
                            map.put(strings[0], strings[1]);
                        }
                        onTuDienListener.onSuccess("load thành công");
                    } catch (IOException exception) {
                        onTuDienListener.onFail(exception);
                    }
            }
    }
    public void themTuDien(String s, String s1,OnTuDienListener onTuDienListener) {
            if(map.containsKey(s)){
                onTuDienListener.onFail(new Exception("từ "+s+" đã có trong từ điển"));
            }else {
                map.put(s,s1);
                onTuDienListener.onSuccess("thêm thành công");
            }
    }

    public void xoaTuDien(String s,OnTuDienListener onTuDienListener) {
            if(!map.containsKey(s)){
                onTuDienListener.onFail(new Exception("từ "+s+" không có trong từ điển"));
            }else {
                map.remove(s);
                onTuDienListener.onSuccess("xóa thành công");
            }
    }
    public void luuKetQua(OnTuDienListener onTuDienListener){
        try {
            PrintWriter pw = new PrintWriter(file);
            pw.close();
            FileWriter fileWriter = new FileWriter(file);
            Iterator<Map.Entry<String,String>> iter = map.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String,String> entry = iter.next();
                fileWriter.write(entry.getKey()+";"+entry.getValue()+"\n");
            }
            fileWriter.close();
            onTuDienListener.onSuccess("lưu kết quả thành công");
        } catch (Exception exception){
            onTuDienListener.onFail(exception);
        }
    }

    public void traTuDien(String s,OnTuDienListener onTuDienListener) {
        if(map.containsKey(s)){
            /**
             * Tìm thấy từ tiếng anh trả về tiếng việt
             */
            onTuDienListener.onSuccess(map.get(s));
        }else if(map.containsValue(s)){
            /**
             * Tìm thấy từ tiếng Việt trả về tiếng anh
             */
            Iterator<Map.Entry<String,String>> iter = map.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String,String> entry = iter.next();
                if (entry.getValue().equals(s)) {
                    onTuDienListener.onSuccess(entry.getKey());
                    return;
                }
            }
        }else {
            /**
             * ko tìm thấy gì luôn
             */
            String error = "Không tìm thấy từ "+s;
            onTuDienListener.onFail(new Exception(error));
        }
    }
    public interface OnTuDienListener{
        void onSuccess(String kequa);
        void onFail(Exception e);
    }
}
