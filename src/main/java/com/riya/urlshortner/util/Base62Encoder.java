package com.riya.urlshortner.util;

public final class Base62Encoder {
    private static final String   ALPHABET="0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int BASE=62;

    private Base62Encoder() {}

    public static String encode(long id) {
        if (id==0)return String.valueOf(ALPHABET.charAt(0));
        StringBuilder sb=new StringBuilder();
        while (id>0){
            sb.append(ALPHABET.charAt((int)id%BASE));
            id/=BASE;
        }
        return sb.reverse().toString();
    }
}
