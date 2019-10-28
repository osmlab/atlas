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
    private String type;
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
    public boolean equals(Object that)
    {
        return EqualsBuilder.reflectionEquals(this, that);
    }
    
    public String getBboxWKT()
    {
        return bboxWKT;
    }
    
    public String getCompleteEntityClass()
    {
        return completeEntityClass;
    }
    
    public Description getDescription()
    {
        return description;
    }
    
    public Long getEndNode()
    {
        return endNode;
    }
    
    public String getEntityType()
    {
        return entityType;
    }
    
    public Long getIdentifier()
    {
        return identifier;
    }
    
    public Long[] getRelations()
    {
        return relations;
    }
    
    public Long getStartNode()
    {
        return startNode;
    }
    
    public Map<String, String> getTags()
    {
        return tags;
    }
    
    public String getType()
    {
        return type;
    }
    
    public String getWKT()
    {
        return WKT;
    }
    
    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }
    
    public void setBboxWKT(String bboxWKT)
    {
        this.bboxWKT = bboxWKT;
    }
    
    public void setCompleteEntityClass(String completeEntityClass)
    {
        this.completeEntityClass = completeEntityClass;
    }
    
    public void setDescription(Description description)
    {
        this.description = description;
    }
    
    public void setEndNode(Long endNode)
    {
        this.endNode = endNode;
    }
    
    public void setEntityType(String entityType)
    {
        this.entityType = entityType;
    }
    
    public void setIdentifier(Long identifier)
    {
        this.identifier = identifier;
    }
    
    public void setRelations(Long[] relations)
    {
        this.relations = relations;
    }
    
    public void setStartNode(Long startNode)
    {
        this.startNode = startNode;
    }
    
    public void setTags(Map<String, String> tags)
    {
        this.tags = tags;
    }
    
    public void setType(String type)
    {
        this.type = type;
    }
    
    public void setWKT(String WKT)
    {
        this.WKT = WKT;
    }
    
    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }
}
