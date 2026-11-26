# Debaan

Aplikasi Android (Kotlin + Jetpack Compose) untuk membaca Maulid dan Qasidah:
al-Barzanji, ad-Diba'i, Simthud Durar, adh-Dhiya'ul Lami', Syaraful Anam, dan Burdah.

## Cara build lewat GitHub

1. Buat repo **public** di GitHub, lalu unggah seluruh isi folder ini (termasuk folder tersembunyi `.github`) ke branch `main`.
   ```
   git init && git add -A && git commit -m "Debaan"
   git branch -M main
   git remote add origin https://github.com/USERNAME/NAMA_REPO.git
   git push -u origin main
   ```
2. Buka tab **Actions** — workflow *Build APK* berjalan otomatis (sekitar 3–6 menit).
3. Setelah selesai, APK ada di tab **Releases** (`Debaan-v1.0.N.apk`). Unduh dan pasang di HP.

## Pembaruan otomatis

Setiap kali kamu `git push` ke `main`, GitHub Actions membuat Release baru (nomor build naik otomatis).
Di aplikasi: **Pengaturan → Perbarui Aplikasi** (juga dicek otomatis saat aplikasi dibuka) →
**Unduh & Pasang**. Nama repo diisi otomatis saat build, tidak perlu diubah manual.

Catatan:
- Repo harus **public** agar aplikasi bisa membaca Releases tanpa login.
- Android akan meminta izin "Pasang aplikasi tidak dikenal" untuk Debaan saat pertama kali memperbarui.
- Semua APK harus ditandatangani dengan kunci yang sama agar bisa saling menimpa. Kunci ada di `app/debaan.jks`
  (password `debaan123`, alias `debaan`). Jangan diganti/dihapus.

## Mengisi teks maulid

Teks ada di `app/src/main/assets/maulid/*.txt`. Format:

```
== Judul Bab
[ar]
teks arab bait 1

teks arab bait 2
[lat]
latin bait 1

latin bait 2
[id]
terjemah bait 1

terjemah bait 2
```

Bait Arab ke-N dipasangkan dengan Latin ke-N dan terjemah ke-N. Bagian `[lat]` dan `[id]` boleh dikosongkan.
Di aplikasi, Arab / Latin / Terjemah bisa dihidupkan atau dimatikan di **Pengaturan**.

## Build lokal (opsional)

Android Studio (Hedgehog atau lebih baru), JDK 17. Buka folder ini lalu Run.
