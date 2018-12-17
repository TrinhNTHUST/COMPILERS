import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Key;
import java.util.*;

public class Run {
    private static final String SOURCE_FILE_NAME = "/Users/toring/Desktop/test/Complier/src/Input.lay";

    private static List<String> tokens = new ArrayList();
    private static Map<String, String> symbols = new HashMap<>();

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

        int varStarted = 0;
        String var = "";

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
                } else if (!var.equals("")) {
                    tokens.add("VAR:" + var);
                    var = "";
                    varStarted = 0;
                }
                token = "";
            } else if (token.equals("=") && state == 0) {
                if (!expr.equals("") && isexpr == 0) {
                    tokens.add("NUM:" + expr);
                    expr = "";
                }
                if (!var.equals("")) {
                    tokens.add("VAR:" + var);
                    var = "";
                    varStarted = 0;
                }
                if (tokens.get(tokens.size() - 1).equals("EQUALS")) {
                    tokens.set(tokens.size() - 1, "EQEQ");
                } else {
                    tokens.add("EQUALS");
                }
                token = "";
            } else if (token.equals("$") && state == 0) {
                varStarted = 1;
                var = var + token;
                token = "";
            } else if (varStarted == 1) {
                if (token.equals("<") || token.equals(">")) {
                    if (var != "") {
                        tokens.add("VAR:" + var);
                        var = "";
                        varStarted = 0;
                    }
                }
                var = var + token;
                token = "";
            } else if (token.equals(Keyword.KEY_PRINT) || token.equals(Keyword.KEY_PRINT.toLowerCase())) {
                tokens.add(Keyword.KEY_PRINT);
                token = "";
            } else if (token.equals(Keyword.KEY_ENDIF) || token.equals(Keyword.KEY_ENDIF.toLowerCase())) {
                tokens.add(Keyword.KEY_ENDIF);
                token = "";
            } else if (token.equals(Keyword.KEY_IF) || token.equals(Keyword.KEY_IF.toLowerCase())) {
                tokens.add(Keyword.KEY_IF);
                token = "";
            } else if (token.equals(Keyword.KEY_THEN) || token.equals(Keyword.KEY_THEN.toLowerCase())) {
                if (!expr.equals("") && isexpr == 0) {
                    tokens.add("NUM:" + expr);
                    expr = "";
                }
                tokens.add(Keyword.KEY_THEN);
                token = "";
            } else if (token.equals(Keyword.KEY_INPUT) || token.equals(Keyword.KEY_INPUT.toLowerCase())) {
                tokens.add(Keyword.KEY_INPUT);
                token = "";
            } else if (Character.isDigit(token.charAt(0))) {
                expr = expr + token;
                token = "";
            } else if (token.equals("+") || token.equals("-") || token.equals("*") || token.equals("/") || token.equals("%")) {
                isexpr = 1;
                expr = expr + token;
                token = "";
            } else if (token.equals("\t")) {
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
        System.out.println(tokens);
        return tokens;
    }

    private static void parse(List<String> tokens) {
        int i = 0;
        while (i < tokens.size()) {
            if (tokens.get(i).equals(Keyword.KEY_ENDIF)) {
                i++;
            } else if (((tokens.get(i + 1).length() >= 7) && (tokens.get(i) + " " + tokens.get(i + 1).substring(0, 6)).equals(Keyword.KEY_PRINT + " " + "STRING"))
                    || ((tokens.get(i + 1).length() >= 5) && (tokens.get(i) + " " + tokens.get(i + 1).substring(0, 4)).equals(Keyword.KEY_PRINT + " " + "EXPR"))
                    || ((tokens.get(i + 1).length() >= 4) && (tokens.get(i) + " " + tokens.get(i + 1).substring(0, 3)).equals(Keyword.KEY_PRINT + " " + "NUM"))
                    || ((tokens.get(i + 1).length() >= 4) && (tokens.get(i) + " " + tokens.get(i + 1).substring(0, 3)).equals(Keyword.KEY_PRINT + " " + "VAR"))) {

                String string = tokens.get(i + 1);
                String value = "";
                if (string.substring(0, 6).equals("STRING")) {
                    value = string;
                } else if (string.substring(0, 3).equals("NUM")) {
                    value = string;
                } else if (string.substring(0, 4).equals("EXPR")) {
                    value = string;
                    value = "NUM:" + evalExpression(value.substring(5));
                } else if (string.substring(0, 3).equals("VAR")) {
                    value = getVARIABLE(string);
                }
                doPrint(value);

                i += 2;
            } else if (((tokens.get(i).length() >= 4 && tokens.get(i + 2).length() >= 7) && (tokens.get(i).substring(0, 3) + " " + tokens.get(i + 1) + " " + tokens.get(i + 2).substring(0, 6)).equals("VAR EQUALS STRING"))
                    || ((tokens.get(i).length() >= 4 && tokens.get(i + 2).length() >= 5) && (tokens.get(i).substring(0, 3) + " " + tokens.get(i + 1) + " " + tokens.get(i + 2).substring(0, 4)).equals("VAR EQUALS EXPR"))
                    || ((tokens.get(i).length() >= 4 && tokens.get(i + 2).length() >= 4) && (tokens.get(i).substring(0, 3) + " " + tokens.get(i + 1) + " " + tokens.get(i + 2).substring(0, 3)).equals("VAR EQUALS NUM"))
                    || ((tokens.get(i).length() >= 4 && tokens.get(i + 2).length() >= 4) && (tokens.get(i).substring(0, 3) + " " + tokens.get(i + 1) + " " + tokens.get(i + 2).substring(0, 3)).equals("VAR EQUALS VAR"))) {

                String varValue = tokens.get(i + 2);
                String value = "";
                if (varValue.substring(0, 6).equals("STRING")) {
                    value = varValue;
                } else if (varValue.substring(0, 3).equals("NUM")) {
                    value = varValue;
                } else if (varValue.substring(0, 4).equals("EXPR")) {
                    value = "NUM:" + evalExpression(varValue);
                } else if (varValue.substring(0, 3).equals("VAR")) {
                    value = getVARIABLE(varValue);
                }
                doAssign(tokens.get(i).substring(4), value);

                i += 3;
            } else if (((tokens.get(i + 1).length() >= 7 && tokens.get(i + 2).length() >= 4)
                    && (tokens.get(i) + " " + tokens.get(i + 1).substring(0, 6) + " " + tokens.get(i + 2).substring(0, 3)).equals("INPUT STRING VAR"))) {
                getINPUT(tokens.get(i + 1).substring(7), tokens.get(i + 2).substring(4));
                i += 3;
            } else if ((tokens.get(i) + " " + tokens.get(i + 1).substring(0, 3)
                    + " " + tokens.get(i + 2) + " " + tokens.get(i + 3).substring(0, 3)
                    + " " + tokens.get(i + 4)).equals("IF NUM EQEQ NUM THEN")) {
                if (tokens.get(i + 1).substring(4).equals(tokens.get(i + 3).substring(4))) {
                    System.out.println("asdifsdfsodfas");
                } else {
                    System.out.println("aaaaaaaaaa");
                }
                i += 5;
            }
        }
    }

    private static String getVARIABLE(String string) {
        String varName = string.substring(4);
        return symbols.getOrDefault(varName, "Undefine Variable");
    }

    private static void getINPUT(String string, String varName) {
        System.out.println(string.substring(1, string.length() - 1));
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        symbols.put(varName, "STRING:\"" + input + "\"");
    }

    private static void doPrint(String value) {
        String result = "";
        if (value.substring(0, 3).equals("NUM")) {
            result = value.substring(4);
        } else if (value.substring(0, 3).equals("VAR")) {
            result = getVARIABLE(value);
        } else if (value.substring(0, 6).equals("STRING")) {
            result = value.substring(8, value.length() - 1);
        }
        System.out.println(result);
    }

    private static void doAssign(String varName, String varValue) {
        symbols.put(varName, varValue);
    }

    private static String evalExpression(String expr) {
//        expr = "," + expr;
//
//        String num = "";
//        List<String> numStack = new ArrayList<>();
//
//        int i = expr.length() - 1;
//        while (i >= 0) {
//            if (expr.charAt(i) == '+' || expr.charAt(i) == '-'
//                    || expr.charAt(i) == '*' || expr.charAt(i) == '/' || expr.charAt(i) == '%') {
//                numStack.add(num);
//                numStack.add(String.valueOf(expr.charAt(i)));
//                num = "";
//            }else if(String.valueOf(expr.charAt(i)).equals(",")){
//                numStack.add(num);
//                num = "";
//            }else{
//                num = num + String.valueOf(expr.charAt(i));
//            }
//            i --;
//        }
//        System.out.println(numStack);

        String result = "";
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByName("JavaScript");
        try {
            result = String.valueOf(engine.eval(expr));
        } catch (ScriptException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void main(String[] args) {
        String data = readFile();
        List<String> tokens = lex(data);
        parse(tokens);
    }
}
