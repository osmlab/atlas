package org.openstreetmap.atlas.geography.atlas.dsl.authentication.impl

import org.apache.commons.lang3.Validate
import org.openstreetmap.atlas.geography.atlas.dsl.authentication.Authenticator
import org.openstreetmap.atlas.geography.atlas.dsl.util.Valid

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * This implementation performs message authentication uses <a href="https://en.wikipedia.org/wiki/HMAC">HMAC</a>
 * with <a href="https://en.wikipedia.org/wiki/SHA-2">SHA512</a>
 * (read more about MACs on <a href="https://en.wikipedia.org/wiki/Message_authentication_code">wiki</a>).
 *
 * HMAC here is implemented utilizing Java Cryptography Architecture or JCA. Read Oracle documentation on JCA
 * <a href="https://docs.oracle.com/javase/8/docs/technotes/guides/security/crypto/CryptoSpec.html">here</a>.
 *
 * @author Yazad Khambata
 */
class SHA512HMACAuthenticatorImpl implements Authenticator {

    public static final int MIN_KEY_SIZE = 10
    private String key

    private static final String HMAC_SHA512 = "HmacSHA512"

    SHA512HMACAuthenticatorImpl(final String key) {
        super()
        Valid.notEmpty key

        Valid.isTrue(key.size() >= MIN_KEY_SIZE, "key should be at least ${MIN_KEY_SIZE}.")
        this.key = key
    }

    @Override
    String sign(final String message) {
        final byte[] byteKey = key.getBytes("UTF-8")
        final Mac sha512_HMAC = Mac.getInstance(HMAC_SHA512)
        final SecretKeySpec keySpec = new SecretKeySpec(byteKey, HMAC_SHA512)
        sha512_HMAC.init(keySpec)

        final byte[] mac_data = sha512_HMAC.doFinal(message.getBytes("UTF-8"))
        final String result = Base64.encoder.encodeToString(mac_data)

        result
    }

    @Override
    void verify(final String message, final String signature) {
        Validate.isTrue(sign(message) == signature, "Signature Mismatch; message: [${message}]; signature: [${signature}].")
    }
}
