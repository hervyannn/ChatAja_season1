package com.chataja.model;

/**
 * Kelas Majelis – anggota majelis gereja yang mengelola pengumuman dan renungan.
 */
public class Majelis extends User {

    public Majelis(String idUser, String nama, String username, String password) {
        super(idUser, nama, username, password, "majelis");
    }

    public Majelis() {
        super();
        setRole("majelis");
    }

    @Override
    public void login() {
        System.out.println("[Majelis] " + getNama() + " berhasil login.");
    }

    @Override
    public void logout() {
        System.out.println("[Majelis] " + getNama() + " logout.");
    }


}
