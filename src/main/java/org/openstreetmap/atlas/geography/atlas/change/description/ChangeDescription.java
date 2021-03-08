package org.openstreetmap.atlas.geography.atlas.change.description;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.change.ChangeType;
import org.openstreetmap.atlas.geography.atlas.change.FeatureChange;
import org.openstreetmap.atlas.geography.atlas.change.description.descriptors.ChangeDescriptor;
import org.openstreetmap.atlas.geography.atlas.change.description.descriptors.ChangeDescriptorComparator;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.LineItem;
import org.openstreetmap.atlas.geography.atlas.items.LocationItem;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * A basic description of the internal contents of a {@link FeatureChange}. A
 * {@link ChangeDescription} consists of a {@link List} of {@link ChangeDescriptor}s as well as some
 * other details (like an identifier, an {@link ItemType}, and a {@link ChangeDescriptorType}).
 *
 * @author lcram
 */
public class ChangeDescription
{
    private static final ChangeDescriptorComparator COMPARATOR = new ChangeDescriptorComparator();

    /** `visible` is used in OSC files to denote whether or not an object should be shown */
    private static final String VISIBLE = "visible";

    private final long identifier;
    private final ItemType itemType;
    private final ChangeDescriptorType changeDescriptorType;
    private final List<ChangeDescriptor> descriptors;
    private final Collection<LocationItem> nodes;
    private final AtlasEntity afterView;
    private final AtlasEntity beforeView;

    /**
     * Convert an entity to a JsonObject which can be used to create an OSC file. Note: You still
     * have to add the @code{visible="true|false"} information, as we don't know if this is
     * creating, updating, or removing.
     *
     * @param entity
     *            the entity to convert
     * @param nodes
     *            nodes for the entity (may include unrelated nodes)
     * @return A JsonObject with the information needed to create an OSC file
     */
    private static JsonObject atlasEntityToOscInformation(final AtlasEntity entity,
            final Collection<LocationItem> nodes)
    {
        final JsonObject information = new JsonObject();
        // Common requirements
        if (entity.getOsmIdentifier() > 0)
        {
            information.addProperty("id", entity.getOsmIdentifier());
        }
        else
        {
            information.addProperty("id", newId(entity.getIdentifier()));
        }
        // This is used to ensure that this will apply cleanly
        if (entity.getTags() != null)
        {
            final Optional<String> lastEditVersion = entity.getTag("last_edit_version");
            if (lastEditVersion.isPresent())
            {
                information.addProperty("version", Long.parseLong(lastEditVersion.get()) + 1);
            }
        }
        // Add the tags (OSC files are idempotent, in that they require <i>all</i> information for
        // an object)
        if (entity.getTags() != null && !entity.getOsmTags().isEmpty())
        {
            final JsonObject tags = new JsonObject();
            entity.getOsmTags().forEach(tags::addProperty);
            information.add("tags", tags);
        }

        if (entity instanceof LocationItem)
        {
            atlasEntityToOscInformationNode(information, (LocationItem) entity);
        }
        else if (entity instanceof LineItem && ((LineItem) entity).asPolyLine() != null)
        {
            return atlasEntityToOscInformationWay(information, (LineItem) entity, nodes);
        }
        else if (entity instanceof Relation)
        {
            atlasEntityToOscInformationRelation(information, (Relation) entity);
        }
        return information;
    }

    /**
     * Convert a LocationItem to OSC information
     *
     * @param information
     *            The information to add to
     * @param entity
     *            The node
     */
    private static void atlasEntityToOscInformationNode(final JsonObject information,
            final LocationItem entity)
    {
        // Nodes have two additional attributes for latitude and longitude
        information.addProperty("type", "node");
        information.addProperty("lat", entity.getLocation().getLatitude().asDegrees());
        information.addProperty("lon", entity.getLocation().getLongitude().asDegrees());
    }

    /**
     * Convert a Relation object to OSC information
     *
     * @param information
     *            The information to add to
     * @param entity
     *            The relation
     */
    private static void atlasEntityToOscInformationRelation(final JsonObject information,
            final Relation entity)
    {
        information.addProperty("type", "relation");
        // Relations have an array of members
        // <relation><member type="node|way|relation" ref="id" role="role"/></relation>
        final JsonArray members = new JsonArray();
        // Try to account for relations spread across atlases
        for (final RelationMember memberInformation : entity.allKnownOsmMembers())
        {
            final JsonObject memberObject = new JsonObject();
            final String type;
            if (memberInformation.getEntity() instanceof LocationItem)
            {
                type = "node";
            }
            else if (memberInformation.getEntity() instanceof LineItem)
            {
                type = "way";
            }
            else
            {
                type = "relation";
            }
            memberObject.addProperty("type", type);
            if (memberInformation.getEntity().getOsmIdentifier() > 0)
            {
                memberObject.addProperty("ref", memberInformation.getEntity().getOsmIdentifier());
            }
            else
            {
                memberObject.addProperty("ref",
                        newId(memberInformation.getEntity().getIdentifier()));
            }
            memberObject.addProperty("role", memberInformation.getRole());
            members.add(memberObject);
        }
    }

    /**
     * Convert a LineItem to OSC information
     *
     * @param information
     *            The information to add to
     * @param entity
     *            The way
     * @return {@code null} if we cannot create the way with the available information
     */
    private static JsonObject atlasEntityToOscInformationWay(final JsonObject information,
            final LineItem entity, final Collection<LocationItem> nodes)
    {
        information.addProperty("type", "way");
        // Lines have an array of node references
        // <way><nd ref="-1"/><nd ref="-2"</></way>
        final JsonArray nodeIds = new JsonArray();
        for (final Location location : entity.asPolyLine())
        {
            // Don't short-circuit on the first found, since there is no guarantee that it is
            // the only location at that point.
            final List<LocationItem> foundNodes = nodes.stream()
                    .filter(locationItem -> location.equals(locationItem.getLocation()))
                    .collect(Collectors.toList());
            // Atlases don't store what nodes belong to what ways, so we cannot create an OSC
            // change for this way.
            if (foundNodes.stream().mapToLong(LocationItem::getIdentifier).distinct().count() != 1)
            {
                return null;
            }
            if (foundNodes.get(0).getOsmIdentifier() > 0)
            {
                nodeIds.add(new JsonPrimitive(foundNodes.get(0).getOsmIdentifier()));
            }
            else
            {
                nodeIds.add(new JsonPrimitive(newId(foundNodes.get(0).getIdentifier())));
            }
        }
        information.add("nd", nodeIds);
        return information;
    }

    /**
     * Convert an identifier to a negative number, as this is commonly seen as a placeholder for new
     * objects
     *
     * @param identifier
     *            the current id
     * @return The new id (<i>always</i> negative)
     */
    private static long newId(final long identifier)
    {
        if (identifier < 0)
        {
            return identifier;
        }
        return -identifier;
    }

    /**
     * Convert create, modify, and delete objects to an OSC type json object
     *
     * @param description
     *            The JsonObject to add the OSC info to
     * @param create
     *            The objects being created
     * @param modify
     *            The objects being modified
     * @param delete
     *            The objects being deleted
     */
    private static void saveAsOsc(final JsonObject description, final Collection<JsonObject> create,
            final Collection<JsonObject> modify, final Collection<JsonObject> delete)
    {
        final JsonObject oscObject = new JsonObject();
        if (!create.isEmpty())
        {
            final JsonArray createObject = new JsonArray();
            create.forEach(createObject::add);
            oscObject.add("create", createObject);
        }
        if (!modify.isEmpty())
        {
            final JsonArray modifyArray = new JsonArray();
            modify.forEach(modifyArray::add);
            oscObject.add("modify", modifyArray);
        }
        if (!delete.isEmpty())
        {
            final JsonArray deleteArray = new JsonArray();
            delete.forEach(deleteArray::add);
            oscObject.add("delete", deleteArray);
        }
        if (!oscObject.entrySet().isEmpty())
        {
            description.add("osc", oscObject);
        }
    }

    public ChangeDescription(final long identifier, final ItemType itemType,
            final AtlasEntity beforeView, final AtlasEntity afterView,
            final ChangeType sourceFeatureChangeType)
    {
        this(identifier, itemType, beforeView, afterView, sourceFeatureChangeType,
                Collections.emptyList());
    }

    /**
     * Create a new ChangeDescription
     *
     * @param identifier
     *            The identifier for the change object
     * @param itemType
     *            The item type
     * @param beforeView
     *            The unmodified object
     * @param afterView
     *            The modified object
     * @param sourceFeatureChangeType
     *            Change type
     * @param nodes
     *            The nodes to be used for way geometry changes, in order. If a collection has
     *            multiple nodes, then no geometry changes for an OSC will be written.
     */
    public ChangeDescription(final long identifier, final ItemType itemType,
            final AtlasEntity beforeView, final AtlasEntity afterView,
            final ChangeType sourceFeatureChangeType, final Collection<LocationItem> nodes)
    {
        this.identifier = identifier;
        this.itemType = itemType;
        this.descriptors = new ArrayList<>();

        this.nodes = nodes != null ? nodes : Collections.emptyList();
        // Avoid saving before/after views if we don't need them to generate the json (if no nodes,
        // then not needed)
        if (!this.nodes.isEmpty())
        {
            this.afterView = afterView;
            this.beforeView = beforeView;
        }
        else
        {
            this.afterView = null;
            this.beforeView = null;
        }

        if (sourceFeatureChangeType == ChangeType.ADD)
        {
            if (beforeView != null)
            {
                this.changeDescriptorType = ChangeDescriptorType.UPDATE;
            }
            else
            {
                this.changeDescriptorType = ChangeDescriptorType.ADD;
            }
        }
        else
        {
            this.changeDescriptorType = ChangeDescriptorType.REMOVE;
        }
        this.descriptors.addAll(
                new ChangeDescriptorGenerator(beforeView, afterView, this.changeDescriptorType)
                        .generate());
    }

    public ChangeDescriptorType getChangeDescriptorType()
    {
        return this.changeDescriptorType;
    }

    /**
     * Get a sorted copy of the underlying {@link ChangeDescriptor} list.
     *
     * @return the sorted list
     */
    public List<ChangeDescriptor> getChangeDescriptors()
    {
        return new ArrayList<>(this.descriptors);
    }

    /**
     * Get the identifier of the feature described by this {@link ChangeDescription}.
     *
     * @return the identifier
     */
    public long getIdentifier()
    {
        return this.identifier;
    }

    /**
     * Get the {@link ItemType} of the feature described by this {@link ChangeDescription}.
     *
     * @return the type
     */
    public ItemType getItemType()
    {
        return this.itemType;
    }

    public JsonElement toJsonElement()
    {
        final JsonObject description = new JsonObject();
        description.addProperty("type", this.changeDescriptorType.toString());
        final JsonArray descriptorArray = new JsonArray();
        for (final ChangeDescriptor descriptor : this.descriptors)
        {
            descriptorArray.add(descriptor.toJsonElement());
        }
        description.add("descriptors", descriptorArray);

        createOsc(description);
        return description;
    }

    @Override
    public String toString()
    {
        this.descriptors.sort(COMPARATOR);
        final StringBuilder builder = new StringBuilder();
        builder.append("ChangeDescription [");
        builder.append("\n");
        builder.append(this.changeDescriptorType);
        builder.append(" ");
        builder.append(this.itemType);
        builder.append(" ");
        builder.append(this.getIdentifier());
        builder.append("\n");

        if (this.descriptors.isEmpty())
        {
            builder.append("]");
            return builder.toString();
        }

        for (int i = 0; i < this.descriptors.size() - 1; i++)
        {
            builder.append(this.descriptors.get(i).toString());
            builder.append("\n");
        }
        builder.append(this.descriptors.get(this.descriptors.size() - 1).toString());
        builder.append("\n");
        builder.append("]");

        return builder.toString();
    }

    /**
     * Create an object for use in generating an OSC
     *
     * @param description
     *            The object to add the OSC info to
     */
    private void createOsc(final JsonObject description)
    {
        final List<JsonObject> create = new ArrayList<>();
        final List<JsonObject> modify = new ArrayList<>();
        final List<JsonObject> delete = new ArrayList<>();
        final Collection<Location> requiredLocations = new HashSet<>();
        oscCreateUpdate(create, modify);
        if (updateRequiredLocations(create, modify, requiredLocations))
        {
            oscDelete(delete, requiredLocations);
            saveAsOsc(description, create, modify, delete);
        }
    }

    /**
     * Fill information in for create/update objects
     *
     * @param create
     *            The collection of objects that will be created (this is added to)
     * @param modify
     *            The collection of objects that will be modified (this is added to)
     */
    private void oscCreateUpdate(final List<JsonObject> create, final Collection<JsonObject> modify)
    {
        if ((this.changeDescriptorType == ChangeDescriptorType.ADD
                || this.changeDescriptorType == ChangeDescriptorType.UPDATE)
                && this.afterView != null)
        {
            final JsonObject createObject = atlasEntityToOscInformation(this.afterView, this.nodes);
            if (createObject != null)
            {
                createObject.addProperty(VISIBLE, true);
                if (this.changeDescriptorType == ChangeDescriptorType.ADD)
                {
                    create.add(createObject);
                }
                else
                {
                    modify.add(createObject);
                }
            }
        }
    }

    /**
     * Fill items in for delete objects
     *
     * @param delete
     *            The collection of objects to delete
     * @param requiredLocations
     *            The locations where nodes must not be deleted
     */
    private void oscDelete(final List<JsonObject> delete,
            final Collection<Location> requiredLocations)
    {
        if (this.changeDescriptorType == ChangeDescriptorType.REMOVE && this.beforeView != null)
        {
            final JsonObject deleteObject = atlasEntityToOscInformation(this.beforeView,
                    this.nodes);
            if (deleteObject != null)
            {
                deleteObject.addProperty(VISIBLE, false);
                // This helps ensure that we don't accidentally delete something if the OSC is
                // directly
                // applied to the OSM API.
                deleteObject.addProperty("if-unused", true);
                // Remove lat/lon, since we are deleting the object.
                deleteObject.remove("lat");
                deleteObject.remove("lon");
                delete.add(deleteObject);
            }
        }
        oscDeleteNodes(delete, requiredLocations);
    }

    /**
     * Delete nodes
     *
     * @param delete
     *            The JsonObjects that may have deletable nodes
     * @param requiredLocations
     *            The locations that must not be deleted
     */
    private void oscDeleteNodes(final List<JsonObject> delete,
            final Collection<Location> requiredLocations)
    {
        // new ArrayList avoids a concurrent modification exception
        for (final JsonObject entityObject : new ArrayList<>(delete))
        {
            if (entityObject.get("nd") != null)
            {
                final JsonArray localNodes = entityObject.get("nd").getAsJsonArray();
                for (final JsonElement nodeElement : localNodes)
                {
                    final long nodeId = nodeElement.getAsLong();
                    final LocationItem node = this.nodes.stream()
                            .filter(node1 -> node1.getOsmIdentifier() == nodeId).findAny()
                            .filter(node1 -> node1.getTags() != null)
                            .filter(node1 -> node1.getOsmTags().isEmpty())
                            .filter(node1 -> !requiredLocations.contains(node1.getLocation()))
                            .orElse(null);
                    // Don't delete nodes with tags
                    if (node != null)
                    {
                        final JsonObject nodeDelete = atlasEntityToOscInformation(node, null);
                        nodeDelete.addProperty(VISIBLE, false);
                        // This helps ensure that we don't accidentally delete something if the
                        // OSC is directly applied to the OSM API.
                        nodeDelete.addProperty("if-unused", true);
                        // Remove lat/lon, since we are deleting the object.
                        nodeDelete.remove("lat");
                        nodeDelete.remove("lon");
                        delete.add(nodeDelete);
                    }
                }
            }
        }
    }

    /**
     * Fill information in for create/update objects
     *
     * @param create
     *            The collection of objects that will be created
     * @param modify
     *            The collection of objects that will be modified
     * @param requiredLocations
     *            The collection of locations that must be present (this is added to)
     * @return {@code true} if we can continue with the osc creation process
     */
    private boolean updateRequiredLocations(final List<JsonObject> create,
            final Collection<JsonObject> modify, final Collection<Location> requiredLocations)
    {
        for (final JsonObject entityObject : Stream.concat(create.stream(), modify.stream())
                .collect(Collectors.toList()))
        {
            if (entityObject.get("nd") == null)
            {
                continue;
            }
            final JsonArray localNodes = entityObject.get("nd").getAsJsonArray();
            for (final JsonElement nodeElement : localNodes)
            {
                final long nodeId = nodeElement.getAsLong();
                final List<LocationItem> nodesFound = this.nodes.stream().filter(
                        node -> node.getIdentifier() == nodeId || node.getOsmIdentifier() == nodeId)
                        .collect(Collectors.toList());
                if (nodesFound.size() != 1)
                {
                    return false;
                }
                if (nodeId < 0)
                {
                    // New nodes should come prior to anything else
                    final JsonObject newNode = atlasEntityToOscInformation(nodesFound.get(0), null);
                    newNode.addProperty(VISIBLE, true);
                    create.add(0, newNode);
                }
                requiredLocations.add(nodesFound.get(0).getLocation());
            }
        }
        return true;
    }
}
