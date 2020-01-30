package com.showoff.tech.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Utils {

    public static final String APP_ID = "491742534863796";
    public static final String CLIENT_SECRET_KEY = "a4453ed01707d35f0cc7bd86c6e30a69";
    public static final String REDIRECT_URI= "https://socialsizzle.heroku.com/auth/";

    public static String streamToString(InputStream is) throws IOException {
        String str = "";

        if (is != null) {
            StringBuilder sb = new StringBuilder();
            String line;

            try {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(is));

                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }

                reader.close();
            } finally {
                is.close();
            }

            str = sb.toString();
        }

        return str;
    }
}
