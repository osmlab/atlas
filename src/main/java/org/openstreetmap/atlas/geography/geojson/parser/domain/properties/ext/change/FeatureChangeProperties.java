package org.openstreetmap.atlas.geography.geojson.parser.domain.properties.ext.change;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.openstreetmap.atlas.geography.geojson.parser.domain.annotation.Foreign;

/**
 * @author Yazad Khambata
 */
@Foreign public class FeatureChangeProperties implements Serializable
{
    private String featureChangeType;
    private Map<String, String> metadata;
    private Description description;
    private String entityType;
    private String completeEntityClass;
    private Long identifier;
    private Map<String, String> tags;
    private Long[] relations;
    private Long startNode;
    private Long endNode;
    private String WKT;
    private String bboxWKT;
    
    public FeatureChangeProperties()
    {
    }
    
    @Override
    public boolean equals(final Object that)
    {
        return EqualsBuilder.reflectionEquals(this, that);
    }
    
    public String getBboxWKT()
    {
        return this.bboxWKT;
    }
    
    public String getCompleteEntityClass()
    {
        return this.completeEntityClass;
    }
    
    public Description getDescription()
    {
        return this.description;
    }
    
    public Long getEndNode()
    {
        return this.endNode;
    }
    
    public String getEntityType()
    {
        return this.entityType;
    }
    
    public String getFeatureChangeType()
    {
        return this.featureChangeType;
    }
    
    public Long getIdentifier()
    {
        return this.identifier;
    }
    
    public Map<String, String> getMetadata()
    {
        return this.metadata;
    }
    
    public Long[] getRelations()
    {
        return this.relations;
    }
    
    public Long getStartNode()
    {
        return this.startNode;
    }
    
    public Map<String, String> getTags()
    {
        return this.tags;
    }
    
    public String getWKT()
    {
        return this.WKT;
    }
    
    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }
    
    public void setBboxWKT(final String bboxWKT)
    {
        this.bboxWKT = bboxWKT;
    }
    
    public void setCompleteEntityClass(final String completeEntityClass)
    {
        this.completeEntityClass = completeEntityClass;
    }
    
    public void setDescription(final Description description)
    {
        this.description = description;
    }
    
    public void setEndNode(final Long endNode)
    {
        this.endNode = endNode;
    }
    
    public void setEntityType(final String entityType)
    {
        this.entityType = entityType;
    }
    
    public void setFeatureChangeType(final String featureChangeType)
    {
        this.featureChangeType = featureChangeType;
    }
    
    public void setIdentifier(final Long identifier)
    {
        this.identifier = identifier;
    }
    
    public void setMetadata(final Map<String, String> metadata)
    {
        this.metadata = metadata;
    }
    
    public void setRelations(final Long[] relations)
    {
        this.relations = relations;
    }
    
    public void setStartNode(final Long startNode)
    {
        this.startNode = startNode;
    }
    
    public void setTags(final Map<String, String> tags)
    {
        this.tags = tags;
    }
    
    public void setWKT(final String WKT)
    {
        this.WKT = WKT;
    }
    
    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }
}
