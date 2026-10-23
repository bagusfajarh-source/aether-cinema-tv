# AETHER CINEMA TV — Android TV / Google TV

Aplikasi Android TV mandiri bertema bioskop/streaming dengan inspirasi tata letak katalog TV. **Ini bukan Netflix atau klien Netflix.** Source code dibuat dari awal (tidak melakukan copy/fork langsung dari JetStreamCompose). Referensi resmi untuk pengembangan Android TV lebih lanjut: https://github.com/android/tv-samples/tree/main/JetStreamCompose (repositori contoh Google, Apache 2.0).

> STATUS: source code v1.0.0 siap di-build. **APK belum dikompilasi atau diuji di perangkat di lingkungan pembuatan ini**, karena Android SDK dan Gradle tidak tersedia. GitHub Actions disertakan agar menghasilkan APK secara otomatis.

## Fitur v1

- Beranda sinematik: hero banner unggulan, carousel horizontal, baris Film, Serial, Favorit, Continue Watching.
- Menu sidebar TV, fokus remote DPAD, animasi fokus kartu/tombol.
- Katalog Film / Serial, pencarian judul/genre/tahun, layar detail film.
- Daftar Saya di SharedPreferences; resume playback per judul.
- Player bawaan Android `VideoView`: MP4 via HTTP/HTTPS; sebagian HLS bisa bekerja bila perangkat mendukung. Video/codec/DRM lain **belum dijamin**. Tidak ada dukungan Widevine, login Jellyfin, SMB langsung atau subtitle eksternal dalam versi ini.
- Sumber katalog asset JSON lokal **atau** URL katalog JSON jarak jauh (settings). Poster/backdrop dapat memakai drawable bawaan atau URL HTTP(S).
- Desain dan poster demo disertakan, dapat diganti.

## Cara paling mudah mendapatkan APK (GitHub Actions)

1. **Ekstrak ZIP ini**. Buat repository GitHub baru, contohnya `aether-cinema-tv`.
2. Upload **isi folder `AetherCinemaTV`**, bukan folder induknya. Pastikan `.github/workflows/build-apk.yml` ikut ter-upload. Folder diawali titik dapat tersembunyi di beberapa file manager.
3. Buka tab **Actions → Build Android TV APK → Run workflow**, atau push ke branch `main`/`master`.
4. Jika build sukses, buka job/build tersebut dan unduh artifact **AetherCinemaTV-debug-APK**.
5. Ekstrak artifact, lalu install `app-debug.apk` di Google TV, misalnya kirim APK ke TV (Send Files to TV / USB) kemudian buka dengan file manager dan izinkan instalasi dari sumber tersebut.
6. Alternatif ADB untuk TV dengan wireless debugging yang sudah disetel: `adb install -r app-debug.apk`.

**Catatan:** APK debug adalah APK yang bisa dipasang/sideload untuk uji sendiri, bukan APK rilis yang ditandatangani untuk Google Play.

## Build dari komputer sendiri

Butuh Android Studio / Android SDK Platform 35, Build Tools 35, JDK 17, dan Gradle 8.9. Project belum menyertakan official Gradle wrapper JAR. Jika menggunakan Gradle terpasang:

```bash
export ANDROID_HOME=/path/to/Android/Sdk
gradle --no-daemon :app:assembleDebug
# hasil: app/build/outputs/apk/debug/app-debug.apk
```

Untuk membuat wrapper resmi di lingkungan dengan Gradle terpasang, jalankan `gradle wrapper --gradle-version 8.9` lalu gunakan `./gradlew :app:assembleDebug`. GitHub Actions **tidak** membutuhkan wrapper karena memasang Gradle 8.9 pada workflow.

## Custom katalog

Edit `app/src/main/assets/catalog.json`, bagian `movies`. Setiap item:

```json
{
  "id": "film-pribadi-001",
  "title": "Film Saya",
  "year": "2026",
  "type": "Film",
  "genre": "Petualangan",
  "rating": "SU",
  "runtime": "45 menit",
  "summary": "Deskripsi film koleksi sendiri.",
  "poster": "https://example.com/poster.jpg",
  "backdrop": "https://example.com/backdrop.jpg",
  "videoUrl": "https://example.com/videos/film-saya.mp4",
  "featured": true
}
```

- Nilai `type`: **`Film`** atau **`Serial`**, harus persis untuk filter.
- `poster`: nama gambar drawable lokal tanpa ekstensi (contoh `poster_bunny`) atau URL langsung gambar.
- `backdrop`: sama seperti poster, untuk banner detail/hero.
- `videoUrl`: URL langsung ke MP4 atau format yang didukung `VideoView` Google TV. **Bukan URL website halaman tontonan/YouTube.**
- `featured`: salah satu film `true` jadi banner unggulan.
- `id` harus unik agar progres video dan favorit tidak tertukar.
- Maksimal 250 entri, ukuran JSON maksimal 2 MB.

Simpan `catalog.json` di server sendiri, misalnya `https://media.example.com/catalog.json`, lalu buka **Pengaturan → Sumber Katalog** pada TV untuk memasukkan URL dan memuat tanpa rebuild APK. Jika memuat dari IP lokal (`http://192.168.x.x/...`), pastikan Google TV dan server bisa saling akses. Gunakan HTTPS untuk koneksi internet publik dan batasi akses apabila konten privat. Aplikasi mengizinkan HTTP untuk kebutuhan jaringan LAN.

## Modifikasi desain

| Ingin mengubah | Lokasi |
|---|---|
| Warna, jenis font, gaya tombol dan fokus | `app/src/main/java/com/aether/cinematv/Ui.java` |
| Beranda, sidebar, kartu, carousel, detail, pencarian | `app/src/main/java/com/aether/cinematv/MainActivity.java` |
| Player, resume, tombol seek | `app/src/main/java/com/aether/cinematv/PlayerActivity.java` |
| Parsing JSON & koneksi katalog | `app/src/main/java/com/aether/cinematv/CatalogRepository.java` |
| Judul, kategori, URL video | `app/src/main/assets/catalog.json` |
| Poster & hero gambar | `app/src/main/res/drawable-nodpi/` |
| Generator artwork contoh | `scripts/make_assets.py` (Pillow) |
| Nama aplikasi | `app/src/main/res/values/strings.xml` |
| App ID | `app/build.gradle.kts` dan package Java |
| Build APK otomatis | `.github/workflows/build-apk.yml` |

## Pilihan integrasi NAS / TrueNAS

Tahap 1: expose koleksi video sendiri melalui HTTP/HTTPS. Contoh jaringan privat:

```text
Google TV ──Wi-Fi──> 192.168.1.50:8080/catalog.json
                          └── video/film1.mp4
```

Katalog bisa di-host lewat nginx static / storage gateway dan diakses dengan URL. Server SMB langsung belum didukung aplikasi ini; kalau ingin library Jellyfin beserta metadata, transcoding, multi-profile dan subtitle, diperlukan integrasi Jellyfin API dan pemain Media3/ExoPlayer pada versi berikutnya.

## Konten demo & lisensi

Semua poster dan banner **merupakan desain abstrak original yang dibuat khusus untuk template ini**. Entri di katalog **hanya contoh**; judul beberapa kartu adalah label demonstrasi dan link streaming merupakan cuplikan/test file publik, bukan film sesuai ilustrasi posternya. URL demo bergantung pihak ketiga sehingga dapat mati/berubah. Sumber video uji:

- https://github.com/video-commander/public-test-streams
- https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4

Gunakan video yang dimiliki sendiri atau memiliki izin streaming. Proyek ini tidak menyediakan streaming film berhak cipta dari layanan berbayar.

## Troubleshooting

- **Aplikasi tidak muncul di launcher TV:** pastikan APK dari workflow berhasil diinstal. AndroidManifest sudah memuat `LEANBACK_LAUNCHER` dan banner.
- **Video tidak berjalan:** coba MP4 H.264/AAC URL langsung yang bisa diakses dari TV. Periksa firewall, HTTP status, SSL, codec atau transcoding. Konten dengan DRM tidak didukung.
- **Poster tidak tampil:** nama drawable harus sesuai file tanpa `.jpg`, atau URL gambar harus langsung mengembalikan gambar JPEG/PNG dengan akses publik untuk TV.
- **Katalog URL gagal:** harus membalas JSON valid dengan kunci `movies`, HTTP 200, tidak memakai autentikasi. Jangan arahkan ke halaman login HTML.
- **Gradle gagal saat build:** lihat log GitHub Actions dan pastikan berkas `.github/workflows/build-apk.yml` dan direktori `app/` di root repository.

## Peningkatan yang bisa dibuat

Media3 ExoPlayer, dukungan subtitle dan audio track, autentikasi Jellyfin, pencarian voice Android TV, login multi-profile, rekomendasi, trailer otomatis, kontrol PIN anak, dan pagination katalog besar.
