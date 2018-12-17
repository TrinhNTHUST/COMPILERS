import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Run {
    private static final String SOURCE_FILE_NAME = "/Users/toring/Desktop/test/Complier/src/Input.lay";

    private static List<String> tokens = new ArrayList();

    private static String readFile() {
        String data = "";
        try {
            data = new String(Files.readAllBytes(Paths.get(SOURCE_FILE_NAME)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        data = data + Keyword.EOF;
        return data;
    }

    private static List<String> lex(String data) {
        String token = "";
        int state = 0;
        String string = "";

        String expr = "";
        String number = "";
        int isexpr = 0;

        char[] fileContents = data.toCharArray();
        for (Character c : fileContents) {
            token = token + c;
            if (token.equals(" ")) {
                if (state == 0) {
                    token = "";
                } else {
                    token = " ";
                }
            } else if (token.equals("\n") || token.equals(Keyword.EOF)) {
                if (!expr.equals("") && isexpr == 1) {
                    tokens.add("EXPR:" + expr);
                    expr = "";
                } else if (!expr.equals("") && isexpr == 0) {
                    tokens.add("NUM:" + expr);
                    expr = "";
                }
                token = "";
            } else if (token.equals(Keyword.KEY_PRINT) || token.equals(Keyword.KEY_PRINT.toLowerCase())) {
                tokens.add(Keyword.KEY_PRINT);
                token = "";
            } else if (Character.isDigit(token.charAt(0))) {
                expr = expr + token;
                token = "";
            } else if (token.equals("+")) {
                isexpr = 1;
                expr = expr + token;
                token = "";
            } else if (token.equals("\"")) {
                if (state == 0) {
                    state = 1;
                } else if (state == 1) {
                    tokens.add("STRING:" + string + "\"");
                    string = "";
                    state = 0;
                    token = "";
                }
            } else if (state == 1) {
                string = string + token;
                token = "";
            }
        }
        return tokens;
    }

    private static void parse(List<String> tokens) {
        System.out.println(tokens);
        int i = 0;
        while (i < tokens.size()) {
            if ((tokens.get(i) + " " + tokens.get(i + 1).substring(0, 6)).equals(Keyword.KEY_PRINT + " " + "STRING")
                    || (tokens.get(i) + " " + tokens.get(i + 1).substring(0, 4)).equals(Keyword.KEY_PRINT + " " + "EXPR")
                    || (tokens.get(i) + " " + tokens.get(i + 1).substring(0, 3 )).equals(Keyword.KEY_PRINT + " " + "NUM")) {
                doPrint(tokens.get(i+1));
                i+= 2;
            }
        }
    }

    private static void doPrint(String string) {
        String s = "";
        if (string.substring(0, 6).equals("STRING")) {
            s = string.substring(7);
        } else if (string.substring(0, 3).equals("NUM")) {
            s = string.substring(4);
        } else if (string.substring(0, 4).equals("EXPR")) {
            s = string.substring(5);
        }
        System.out.println(s);
    }

    public static void main(String[] args) {
        String data = readFile();
        List<String> tokens = lex(data);
        parse(tokens);
    }
}
