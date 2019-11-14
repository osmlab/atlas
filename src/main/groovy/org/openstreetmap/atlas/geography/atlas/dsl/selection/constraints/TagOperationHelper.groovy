package org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints

import groovy.transform.PackageScope
import org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints.regex.RegexSupport
import org.openstreetmap.atlas.geography.atlas.dsl.util.SingletonMap
import org.openstreetmap.atlas.geography.atlas.dsl.util.Valid

/**
 * @author Yazad Khambata
 */
@PackageScope
@Singleton
class TagOperationHelper {

    boolean has(final Map<String, String> actualTags, final String key) {
        actualTags.containsKey(key)
    }

    boolean has(final Map<String, String> actualTags, final String key, String value) {
        actualTags[key] == value
    }

    /**
     *
     * @param actualTags
     * @param keyValueToLookFor - Supports Map of String, String or Map of String and List Of Strings.
     * @return
     */
    boolean has(final Map<String, String> actualTags, final Map<String, Object> keyValueToLookFor) {
        final SingletonMap<String, Object> keyValueToLookForAsSingletonMap = keyValueToLookFor as SingletonMap

        def value = keyValueToLookForAsSingletonMap.getValue()

        if (value instanceof String) {
            return hasTheValue(actualTags, (SingletonMap<String, String>)keyValueToLookForAsSingletonMap)
        } else if (value instanceof List<String>) {
            return hasAnyValue(actualTags, (SingletonMap<String, List<String>>)keyValueToLookForAsSingletonMap)
        } else {
            throw new IllegalArgumentException("Param value of type ${value?.class} is not supported. keyValueToLookFor: ${keyValueToLookFor}.")
        }
    }

    boolean hasTheValue(final Map<String, String> actualTags, final SingletonMap<String, String> keyValueToLookFor) {
        final String keyToLookFor = keyValueToLookFor.getKey()
        final String valueToLookFor = keyValueToLookFor.getValue()

        has(actualTags, keyToLookFor, valueToLookFor)
    }

    private boolean hasAnyValue(final Map<String, String> actualTags, final SingletonMap<String, List<String>> keyValuesToLookFor) {
        final String keyToLookFor = keyValuesToLookFor.getKey()
        final List<String> valuesToLookFor = keyValuesToLookFor.getValue()

        Valid.isTrue !valuesToLookFor.isEmpty()

        for (String valueToLookFor : valuesToLookFor) {
            boolean found = has(actualTags, keyToLookFor, valueToLookFor)

            if (found) {
                return true
            }
        }

        return false
    }

    boolean like(final Map<String, String> actualTags, final String keyRegex) {
        !(actualTags.keySet().findAll { it =~ keyRegex }.isEmpty())
    }

    boolean like(final Map<String, String> actualTags, final String key, String valueRegex) {
        RegexSupport.instance.matches(actualTags.get(key), valueRegex)
    }

    boolean like(final Map<String, String> actualTags, final Map<String, String> keyValueToLookFor) {
        //Assumes keyValueToLookFor has ONE entry
        final String keyToLookFor = keyValueToLookFor.keySet().iterator().next()

        final String valueRegexToLookFor = keyValueToLookFor[keyToLookFor]

        like(actualTags, keyToLookFor, valueRegexToLookFor)
    }
}
