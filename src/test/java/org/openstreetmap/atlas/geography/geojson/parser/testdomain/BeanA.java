package org.openstreetmap.atlas.geography.geojson.parser.testdomain;

import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Scalar and scalar arrays. Example,
 *
 * <pre>
 *     {
 *          id: 10,
 *          name: "Hello",
 *          score: 10.5,
 *          ids: [1,2,3],
 *          names: ["hello", "hola", "bonjour"],
 *          scores: [1.414, 3.14159, 2.718],
 *          result: true,
 *          results: [true, false, true]
 *      }
 * </pre>
 *
 * @author Yazad Khambata
 */
public class BeanA
{
    private Long id;
    private String name;
    private Double score;
    private Boolean result;
    private Long[] ids;
    private String[] names;
    private Double[] scores;
    private Boolean[] results;
    private Map<String, String> tags;

    public BeanA()
    {
    }

    @Override
    public boolean equals(final Object that)
    {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    public Long getId()
    {
        return this.id;
    }

    public Long[] getIds()
    {
        return this.ids;
    }

    public String getName()
    {
        return this.name;
    }

    public String[] getNames()
    {
        return this.names;
    }

    public Boolean getResult()
    {
        return this.result;
    }

    public Boolean[] getResults()
    {
        return this.results;
    }

    public Double getScore()
    {
        return this.score;
    }

    public Double[] getScores()
    {
        return this.scores;
    }

    public Map<String, String> getTags()
    {
        return this.tags;
    }

    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    public void setId(final Long id)
    {
        this.id = id;
    }

    public void setIds(final Long[] ids)
    {
        this.ids = ids;
    }

    public void setName(final String name)
    {
        this.name = name;
    }

    public void setNames(final String[] names)
    {
        this.names = names;
    }

    public void setResult(final Boolean result)
    {
        this.result = result;
    }

    public void setResults(final Boolean[] results)
    {
        this.results = results;
    }

    public void setScore(final Double score)
    {
        this.score = score;
    }

    public void setScores(final Double[] scores)
    {
        this.scores = scores;
    }

    public void setTags(final Map<String, String> tags)
    {
        this.tags = tags;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }
}
