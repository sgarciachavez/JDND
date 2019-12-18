package com.example.demo;

import java.lang.reflect.Field;

public class TestUtils {

    public static String USER = "testuser";
    public static String PASSWORD = "password123";
    public static boolean CREATE = true;
    public static String TOKEN = null;

    public static String getUSER() {
        return USER;
    }

    public  void setUSER(String USER) {
        this.USER = USER;
    }

    public static String getPASSWORD() {
        return PASSWORD;
    }

    public void setPASSWORD(String PASSWORD) {
        this.PASSWORD = PASSWORD;
    }

    public static boolean isCREATE() {
        return CREATE;
    }

    public static void setCREATE(boolean CREATE) {
        TestUtils.CREATE = CREATE;
    }

    public static String getTOKEN() {
        return TOKEN;
    }

    public static void setTOKEN(String TOKEN) {
        TestUtils.TOKEN = TOKEN;
    }

    public static String getUserContent(boolean login){
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"username\" :  \"" + USER + "\",");
        sb.append("\"password\" :  \"" + PASSWORD + "\"");

        if(login){
            sb.append("}");
            return sb.toString();
        }

        sb.append(", \"confirmPassword\" :  \"" + PASSWORD + "\"");
        sb.append("}");
        return sb.toString();
    }

    public static void injectObject(Object target, String fieldName, Object toInject){
        boolean wasPrivate = false;
        try {
            Field field = target.getClass().getDeclaredField(fieldName);

            if(!field.isAccessible()){
                field.setAccessible(true);
                wasPrivate = true;
            }

            field.set(target, toInject);
            if(wasPrivate){
                field.setAccessible(false);
            }

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }
}
