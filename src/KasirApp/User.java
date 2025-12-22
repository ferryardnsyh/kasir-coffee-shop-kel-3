package KasirApp;

public class User {
    private final String username = "admin";
    private final String password = "123";

    public boolean login(String user, String pass) {
        return username.equals(user) && password.equals(pass);
    }
}
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */

/**
 *
 * @author naufal
 */

