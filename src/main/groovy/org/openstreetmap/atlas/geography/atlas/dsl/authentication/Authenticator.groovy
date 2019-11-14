package org.openstreetmap.atlas.geography.atlas.dsl.authentication

/**
 * A general purpose message authenticator. See <a href="https://en.wikipedia.org/wiki/Message_authentication">wiki</a>
 * for more details on message authentication.
 *
 * @author Yazad Khambata
 */
interface Authenticator {
    String sign(String message)

    void verify(String message, String signature)
}
