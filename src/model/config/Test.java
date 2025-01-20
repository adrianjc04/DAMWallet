/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model.config;

/**
 *
 * @author DAM2
 */
public class Test {
    static Thread hilo = new Thread(new Runnable() {
        @Override
        public void run() {
            System.out.println(Configurable.seleccionarArchivo("db","C:\\"));
        }
    });
    
    public static void main(String[] args) {
        hilo.start();
    }
}
