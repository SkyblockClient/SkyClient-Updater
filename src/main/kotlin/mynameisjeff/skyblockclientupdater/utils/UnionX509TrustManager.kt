package mynameisjeff.skyblockclientupdater.utils

import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

class UnionX509TrustManager(private vararg val trustManagers: X509TrustManager) : X509TrustManager {
    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        val it = trustManagers.iterator()
        while (it.hasNext()) {
            try {
                it.next().checkClientTrusted(chain, authType)
                return
            } catch (e: CertificateException) {
                if (!it.hasNext()) {
                    throw e
                }
            }
        }
    }

    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        val it = trustManagers.iterator()
        while (it.hasNext()) {
            try {
                it.next().checkServerTrusted(chain, authType)
                return
            } catch (e: CertificateException) {
                if (!it.hasNext()) {
                    throw e
                }
            }
        }
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> = trustManagers.flatMap { it.acceptedIssuers.asIterable() }.toTypedArray()
}