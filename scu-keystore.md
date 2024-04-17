SkyClientUpdater adds the following SSL certificates manually due to issues with downloading from sources such as GitHub. This was originally created for Polyfrost and the OneConfig loader (and will be added later), but we wanted to add this to SCU now to ensure that users can download the latest versions of mods and other files from the internet.

----

As minecraft 1.8.9 is over 8 years old (at time of writing this), an unfortunate consequence is that the java version is missing many notable root CA certificates used by modern websites (notably, the polyfrost API). Due to this, in order for java to trust websites using certificates issued by providers like LetsEncrypt, the adjacent keystore (password `polyfrost`) must be manually loaded and used to override the default trust store.

NOTE: To reduce file size, this keystore DOES NOT contain any of the certificates added by default in JRE 1.8u51. When loaded in the code, this keystore must be merged with the default CA certificate store (located at `JRE_ROOT/lib/security/cacerts`).

# Editing

In order to modify this keystore, the `keytool` binary from JRE 1.8u51 can and MUST be used to ensure compatibility. To add a new certificate: `keytool -keystore polyfrost.jks -storepass polyfrost -alias CERT_ALIAS_HERE -import -file CERT_FILE_HERE`.

# Contained certificates

| Certificate name & link                                                                                                                            | SHA1 fingerprint                                              | Reason for addition                                                                                     |
| -------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------- |
| [ISRG Root X1](https://letsencrypt.org/certs/isrgrootx1.der)                                                                                       | `CA:BD:2A:79:A1:07:6A:31:F2:1D:25:36:35:CB:03:9D:43:29:A5:E8` | LetsEncrypt's main root certificate, issued by cloudflare (and thus used by the polyfrost API)          |
| [ISRG Root X2](https://letsencrypt.org/certs/isrg-root-x2.der)                                                                                     | `BD:B1:B9:3C:D5:97:8D:45:C6:26:14:55:F8:DB:95:C7:5A:D1:53:AF` | LetsEncrypt's elliptic curve root certiciate, will likely be used eventually for LetsEncrypt issuances  |
| [GTS Root R1](https://pki.goog/repo/certs/gtsr1.der)                                                                                               | `E5:8C:1C:C4:91:3B:38:63:4B:E9:10:6E:E3:AD:8E:6B:9D:D9:81:4A` | Google Trust Services' main root certificate, issued by cloudflare (and thus used by the polyfrost API) |
| [DigiCert Global Root G2](https://cacerts.digicert.com/DigiCertGlobalRootG2.crt)                                                                   | `DF:3C:24:F9:BF:D6:66:76:1B:26:80:73:FE:06:D1:CC:8D:4F:82:A4` | Used by textures.minecraft.net, added just in case                                                      |
| [Microsoft RSA Root Certificate Authority 2017](http://www.microsoft.com/pkiops/certs/Microsoft%20RSA%20Root%20Certificate%20Authority%202017.crt) | `73:A5:E6:4A:3B:FF:83:16:FF:0E:DC:CC:61:8A:90:6E:4E:AE:4D:74` | Used as a backup for DigiCert Global Root G2                                                            |
