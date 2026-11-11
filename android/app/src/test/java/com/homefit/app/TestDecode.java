package com.homefit.app;
import java.nio.charset.StandardCharsets;
public class TestDecode {
    public static void main(String[] args) {
        byte[] bytes = {(byte)0xC6, (byte)0xB0, (byte)0xE1, (byte)0xBB, 0x69};
        String s = new String(bytes, StandardCharsets.UTF_8);
        System.out.println("Length: " + s.length());
        for (int i = 0; i < s.length(); i++) {
            System.out.printf("%c : %04x\n", s.charAt(i), (int)s.charAt(i));
        }
    }
}