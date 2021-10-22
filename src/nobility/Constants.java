package nobility;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Constants {

    public final static Charset standard = StandardCharsets.UTF_8;
    public final static String transform = "AES/ECB/PKCS5Padding";
    public final static String type = "AES";
    public final static int mode = 2; //Cipher.DECRYPT_MODE
    public final static byte[] keyBytes = new byte[]{65, 68, 66, 83, 74, 72, 80,
            83, 50, 50, 53, 57, 51, 56, 55, 50};
    public final static byte[] coreBytes = new byte[]{75, 50, 48, 76, 65, 54, 76,
            70, 57, 50, 75, 77, 48, 50, 77, 52};
}
