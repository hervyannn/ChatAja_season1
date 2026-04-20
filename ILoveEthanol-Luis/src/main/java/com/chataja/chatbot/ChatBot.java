package com.chataja.chatbot;

import com.chataja.dao.*;
import com.chataja.model.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Mesin chatbot ChatAja.
 * Memproses pertanyaan pengguna menggunakan keyword/intent matching
 * dan mengembalikan jawaban berdasarkan data dari database.
 *
 * Sesuai class diagram: ChatBot memiliki namaBot dan metode
 * prosesPertanyaan(), validasiInput(), dan displayJawaban().
 */
public class ChatBot {

    private final String namaBot = "ChatAja";
    private User loggedUser;  // null berarti Jemaat (tidak login)

    // ── DAO ──────────────────────────────────────────────────────────────
    private final JadwalIbadahDAO jadwalIbadahDAO = new JadwalIbadahDAO();


    // ── Enum Intent ───────────────────────────────────────────────────────
    private enum Intent {
        JADWAL_IBADAH, LOKASI, RENUNGAN, PENGUMUMAN, KONTAK,
        JADWAL_TUGAS, SAPAAN, BANTUAN, TIDAK_DIKENALI
    }

    public ChatBot() {}

    public ChatBot(User loggedUser) {
        this.loggedUser = loggedUser;
    }

    public void setLoggedUser(User user) {
        this.loggedUser = user;
    }

    public User getLoggedUser() {
        return loggedUser;
    }

    public String getNamaBot() {
        return namaBot;
    }

    // ────────────────────────────────────────────────────────────────────
    //  PUBLIC INTERFACE
    // ────────────────────────────────────────────────────────────────────

    /**
     * Proses pertanyaan dari pengguna.
     * @param input teks dari pengguna
     * @return respon chatbot
     */
    public String prosesPertanyaan(String input) {
        if (!validasiInput(input)) {
            return "⚠️ Mohon masukkan pertanyaan yang valid (tidak boleh kosong).";
        }

        Intent intent = deteksiIntent(input.toLowerCase().trim());
        return displayJawaban(intent, input);
    }

    /**
     * Validasi input pengguna.
     * @return false jika input null atau kosong
     */
    public boolean validasiInput(String input) {
        return input != null && !input.trim().isEmpty();
    }

    /**
     * Format dan tampilkan jawaban sesuai intent.
     */
    public String displayJawaban(Intent intent, String input) {
        return switch (intent) {
            case JADWAL_IBADAH  -> responJadwalIbadah();
            case KONTAK         -> responKontak();
            case SAPAAN         -> responSapaan();
            case BANTUAN        -> responBantuan();
            default             -> responTidakDikenali(input);
        };
    }

    // ────────────────────────────────────────────────────────────────────
    //  INTENT DETECTION – keyword matching
    // ────────────────────────────────────────────────────────────────────

    private Intent deteksiIntent(String text) {
        // Sapaan
        if (containsAny(text, "halo", "hai", "hi", "hello", "selamat", "hei")) {
            return Intent.SAPAAN;
        }
        // Bantuan
        if (containsAny(text, "bantuan", "help", "menu", "bisa apa", "apa saja", "fitur")) {
            return Intent.BANTUAN;
        }
        // Jadwal ibadah
        if (containsAny(text, "jadwal ibadah", "ibadah", "kebaktian", "misa",
                "jadwal minggu", "ibadah minggu", "jadwal gereja")) {
            return Intent.JADWAL_IBADAH;
        }
        // Lokasi
        if (containsAny(text, "lokasi", "alamat", "di mana", "dimana",
                "tempat ibadah", "rumah ibadah", "gereja mana", "ada di")) {
            return Intent.LOKASI;
        }
        // Renungan
        if (containsAny(text, "renungan", "devotion", "firman", "renungan harian",
                "bacaan", "kotbah", "khotbah", "firman hari ini")) {
            return Intent.RENUNGAN;
        }
        // Pengumuman
        if (containsAny(text, "pengumuman", "info", "berita", "kabar", "pemberitahuan",
                "agenda", "acara gereja")) {
            return Intent.PENGUMUMAN;
        }
        // Kontak
        if (containsAny(text, "kontak", "hubungi", "telepon", "tlp", "nomor",
                "pengurus", "contact", "hp", "whatsapp", "wa")) {
            return Intent.KONTAK;
        }
        // Jadwal tugas (hanya untuk Majelis)
        if (containsAny(text, "tugas", "jadwal tugas", "pelayanan saya",
                "jadwal pelayanan", "tugas majelis", "jadwal saya")) {
            return Intent.JADWAL_TUGAS;
        }

        return Intent.TIDAK_DIKENALI;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String kw : keywords) {
            if (text.contains(kw)) return true;
        }
        return false;
    }

    // ────────────────────────────────────────────────────────────────────
    //  RESPONSE BUILDERS (UC-1 s.d. UC-8)
    // ────────────────────────────────────────────────────────────────────

    /** UC-1: Jadwal Ibadah */
    private String responJadwalIbadah() {
        List<JadwalIbadah> list = jadwalIbadahDAO.getUpcoming();
        if (list.isEmpty()) {
            return "📅 Jadwal ibadah belum tersedia saat ini.\n" +
                   "Silakan hubungi pengurus gereja untuk informasi lebih lanjut.";
        }

        // Kelompokkan per tanggal
        StringBuilder sb = new StringBuilder();
        sb.append("📅 JADWAL IBADAH GEREJA\n");
        sb.append("─".repeat(35)).append("\n\n");

        String lastDate = "";
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", new Locale("id", "ID"));
        for (JadwalIbadah j : list) {
            String dateKey = j.getTanggal() != null ? j.getTanggal().toString() : "";
            if (!dateKey.equals(lastDate)) {
                if (!lastDate.isEmpty()) sb.append("\n");
                sb.append("📆 ")
                  .append(j.getTanggal() != null ? j.getTanggal().format(dateFmt) : "-")
                  .append("\n");
                lastDate = dateKey;
            }
            sb.append(j.tampilkan()).append("\n");
        }
        return sb.toString().trim();
    }




    /** Kontak Pengurus (bagian dari UC-2) */
    private String responKontak() {

        return "📞 Informasi kontak pengurus belum tersedia.\nSilakan kunjungi gereja secara langsung.";

    }


    /** Respon sapaan */
    private String responSapaan() {
        String nama = (loggedUser != null) ? ", " + loggedUser.getNama() : "";
        return "👋 Halo" + nama + "! Saya " + namaBot + ", asisten informasi gereja Anda.\n\n" +
               "Saya siap membantu Anda dengan:\n" +
               "• Jadwal ibadah\n" +
               "• Lokasi & alamat gereja\n" +
               "• Renungan harian\n" +
               "• Pengumuman gereja\n" +
               "• Kontak pengurus\n\n" +
               "Silakan ketik pertanyaan Anda! 😊";
    }

    /** Respon bantuan */
    private String responBantuan() {
        return "ℹ️ PANDUAN PENGGUNAAN ChatAja\n" +
               "─".repeat(35) + "\n\n" +
               "Anda dapat bertanya dengan kata kunci:\n\n" +
               "📅 Jadwal Ibadah\n" +
               "   → \"Jadwal ibadah minggu ini\"\n\n" +
               "📍 Lokasi Gereja\n" +
               "   → \"Di mana alamat gereja?\"\n\n" +
               "📖 Renungan Harian\n" +
               "   → \"Berikan renungan hari ini\"\n\n" +
               "📢 Pengumuman\n" +
               "   → \"Pengumuman gereja terbaru\"\n\n" +
               "📞 Kontak Pengurus\n" +
               "   → \"Informasi kontak pengurus\"\n\n" +
               "📋 Jadwal Tugas (Majelis)\n" +
               "   → \"Jadwal tugas pelayanan saya\"\n" +
               "   ⚠️ Harus login sebagai Majelis";
    }

    /** Respon tidak dikenali */
    private String responTidakDikenali(String input) {
        return "🤔 Maaf, saya tidak memahami pertanyaan: \"" + input + "\"\n\n" +
               "Ketik \"bantuan\" untuk melihat daftar topik yang bisa saya bantu. 😊";
    }
}
