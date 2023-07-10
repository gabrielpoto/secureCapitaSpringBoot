package io.getarrays.securecapita.utils;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import io.github.cdimascio.dotenv.Dotenv;


import static com.twilio.rest.api.v2010.account.Message.creator;


public class SmsUtils {

    static Dotenv dotenv = Dotenv.load();

    public static final String FROM_NUMBER = "+14504890981";
    public static final String SID_KEY = dotenv.get("SID_KEY");
    public static final String TOKEN_KEY = dotenv.get("TOKEN_KEY");

    public static void sendSMS(String to, String messageBody){
        Twilio.init(SID_KEY,TOKEN_KEY);

        Message message  = creator(new PhoneNumber("+" + to), new PhoneNumber(FROM_NUMBER), messageBody).create();
        System.out.println(message);
    }

}
