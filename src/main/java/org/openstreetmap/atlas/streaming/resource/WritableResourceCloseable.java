package org.openstreetmap.atlas.streaming.resource;

/**
 * Use for WritableResources that have dependent AutoCloseable resources
 * 
 * @author Taylor Smock
 */
public interface WritableResourceCloseable extends WritableResource, ResourceCloseable
{

}
