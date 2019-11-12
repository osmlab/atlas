package org.openstreetmap.atlas.geography.atlas.dsl.authentication.impl

import org.junit.Test
import org.openstreetmap.atlas.geography.atlas.dsl.authentication.Authenticator

/**
 * @author Yazad Khambata
 */
class SHA512HMACAuthenticatorImplTest {
    @Test
    void sanity() {
        final String message = "select relation.id, relation.osmId, relation.tags from atlasAIA.relation where relation.hasTagLike(name: /Pond/) or relation.hasTagLike(/natur/)"
        final String preSignedMessage = "BnO2lgNrPIXxPEkSPwK5wFBsCwXXx+e+Md0nRbjhG8HXZ0zLTd9gqJ9FKrt9WKxgLrepRkCo2LAwJoKCMlX7KA=="

        final Authenticator authenticator = new SHA512HMACAuthenticatorImpl("DUMMY_SECRET")

        final String signedMessage = authenticator.sign(message)
        assert signedMessage == preSignedMessage

        authenticator.verify(message, signedMessage)

        try {
            authenticator.verify(message, signedMessage + " ")
            assert false
        } catch (Exception e) {}
    }
}
