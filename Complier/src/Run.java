import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Run {
    private static final String SOURE_FILE_NAME = "Input.lay";

    private static String readFile(){
        String data = "";
        try {
            data = new String(Files.readAllBytes(Paths.get(SOURE_FILE_NAME)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    private static String lex(){
        String data = "";
        try {
            data = new String(Files.readAllBytes(Paths.get(SOURE_FILE_NAME)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }



    public static void main(String[] args) {
        String data = readFile();
    }
}
