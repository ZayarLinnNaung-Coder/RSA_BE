package atdl.thesis.rsa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LZWUtils {

    private static double MAX_TABLE_SIZE = Math.pow(2, 16);

    public static String compress(String text){

        double table_Size =  255;

        Map<String, Integer> TABLE = new HashMap<String, Integer>();

        for (int i = 0; i < 255 ; i++)
            TABLE.put("" + (char) i, i);

        System.out.println(TABLE);

        String initString = "";

        List<Integer> encoded_values = new ArrayList<Integer>();

        for (char symbol : text.toCharArray()) {
            System.out.println("For iteration: " + symbol);

            String Str_Symbol = initString + symbol;
            System.out.println("InitString " + initString);
            System.out.println("Str_Symbol " + Str_Symbol);

            if (TABLE.containsKey(Str_Symbol)){
                System.out.println(Str_Symbol + " is in table");
                initString = Str_Symbol;
                System.out.println("Set Str_Symbol value to InitString " + initString);
            }
            else {
                System.out.println(Str_Symbol + " is not in table");
                encoded_values.add(TABLE.get(initString));
                System.out.println("The value of initString : " + TABLE.get(initString) + " is added to encoded_values");
                System.out.println("Now the encoded_values is ");
                System.out.println(encoded_values);

                if(table_Size < MAX_TABLE_SIZE)
                    TABLE.put(Str_Symbol, (int) table_Size++);

                initString = "" + symbol;
                System.out.println("Now init string: " + initString);
            }

            System.out.println();
        }

        if (!initString.equals("")){
            encoded_values.add(TABLE.get(initString));
            System.out.println("InitString :" + initString + " is not equal to ''");
            System.out.println("Added to encoded_values");
            System.out.println(encoded_values);
            System.out.println();
        }

        StringBuilder encodedString = new StringBuilder();
        encoded_values.forEach(v -> encodedString.append((char) v.intValue()));

        System.out.println(encoded_values);

        return encodedString.toString();

    }

    public static String decompress(String compressString){

        List<Integer> get_compress_values = new ArrayList<>();
        int table_Size = 255;

        for (int i = 0; i < compressString.length(); i++) {
            get_compress_values.add((int) compressString.charAt(i));
        }

        Map<Integer, String> TABLE = new HashMap<>();
        for (int i = 0; i < 255; i++)
            TABLE.put(i, "" + (char) i);

        System.out.println(TABLE);

        String Encode_values = "" + (char) (int) get_compress_values.remove(0);

        StringBuffer decoded_values = new StringBuffer(Encode_values);
        System.out.println(decoded_values);

        String get_value_from_table = null;
        for (int check_key : get_compress_values) {
            System.out.println("For iteration check_key: " + check_key);

            if (TABLE.containsKey(check_key)) {
                get_value_from_table = TABLE.get(check_key);
                System.out.println(TABLE);
                System.out.println(check_key);
                System.out.println("check_key is present in TABLE");
                System.out.println("get_value_from_table: " + get_value_from_table);
            }
            else if (check_key == table_Size){
                get_value_from_table = Encode_values + Encode_values.charAt(0);
                System.out.println("check_key is equal to table_size");
                System.out.println("get_value_from_table: " + get_value_from_table);
            }

            decoded_values.append(get_value_from_table);
            System.out.println(get_value_from_table + " is appended to decoded_value");
            System.out.println("Decoded values is");
            System.out.println(decoded_values);

            if(table_Size < MAX_TABLE_SIZE )
                TABLE.put(table_Size++, Encode_values + get_value_from_table.charAt(0));

            Encode_values = get_value_from_table;
            System.out.println("Encode_values is " + Encode_values);
            System.out.println();
        }

        return decoded_values.toString();
    }

    public static void main(String[] args) {
//        String originalString = "Here is the demo version of LZW algorithm. This is the original string.";
//        String compress = compress(originalString);
//        System.out.println(compress);
//        System.out.format("Original %s, Compressed %s \n", originalString.length(), compress.length());

        String decompress = decompress("Here is thĂdemo vĀsion of LZW algorićm. ThĄăąćĂĠiginĝ stġng.");
        System.out.println(decompress);
    }

}
