package com.chataja.model;

/**
 * Kelas Admin – dapat mengelola semua data master sistem.
 * Aksi: tambahData, hapusData, editData (CRUD data jadwal ibadah & tugas).
 */
public class Admin extends User {

    public Admin(String idUser, String nama, String username, String password) {
        super(idUser, nama, username, password, "admin");
    }

    public Admin() {
        super();
        setRole("admin");
    }

    @Override
    public void login() {
        System.out.println("[Admin] " + getNama() + " berhasil login.");
    }

    @Override
    public void logout() {
        System.out.println("[Admin] " + getNama() + " logout.");
    }

}
