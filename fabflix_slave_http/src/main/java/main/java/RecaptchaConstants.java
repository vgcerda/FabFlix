package main.java;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class RecaptchaConstants {
    private String SECRET_KEY = "";

    public RecaptchaConstants() {
        try {
//            /home/ubuntu/recaptcha_secret_key.txt
            File f = new File("/home/ubuntu/recaptcha_secret_key.txt");
            Scanner s = new Scanner(f);
            while(s.hasNextLine()) {
                SECRET_KEY = s.nextLine();
                break;
            }
            s.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getSecretKey() {
        return SECRET_KEY;
    }

//    try{
//        Scanner reader = new Scanner(new File("recaptcha_secret_key.txt"));
//        public static final String SECRET_KEY ="";
//    } catch (FileNotFoundException e) {
//        System.out.println("RecaptchaConstants.java: recaptcha_secret_key.txt not found.");
//    }

}
