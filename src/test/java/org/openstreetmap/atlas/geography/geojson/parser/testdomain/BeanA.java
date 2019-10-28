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
    public boolean equals(Object that)
    {
        return EqualsBuilder.reflectionEquals(this, that);
    }
    
    public Long getId()
    {
        return id;
    }
    
    public Long[] getIds()
    {
        return ids;
    }
    
    public String getName()
    {
        return name;
    }
    
    public String[] getNames()
    {
        return names;
    }
    
    public Boolean getResult()
    {
        return result;
    }
    
    public Boolean[] getResults()
    {
        return results;
    }
    
    public Double getScore()
    {
        return score;
    }
    
    public Double[] getScores()
    {
        return scores;
    }
    
    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }
    
    public void setId(Long id)
    {
        this.id = id;
    }
    
    public void setIds(Long[] ids)
    {
        this.ids = ids;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    public void setNames(String[] names)
    {
        this.names = names;
    }
    
    public void setResult(Boolean result)
    {
        this.result = result;
    }
    
    public void setResults(Boolean[] results)
    {
        this.results = results;
    }
    
    public void setScore(Double score)
    {
        this.score = score;
    }
    
    public void setScores(Double[] scores)
    {
        this.scores = scores;
    }
    
    public Map<String, String> getTags()
    {
        return tags;
    }
    
    public void setTags(Map<String, String> tags)
    {
        this.tags = tags;
    }
    
    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }
}
