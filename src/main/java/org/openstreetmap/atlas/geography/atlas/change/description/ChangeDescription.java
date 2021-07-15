package org.openstreetmap.atlas.geography.atlas.change.description;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
    /**
     * A transformer factory for writing data -- creation is <i>surprisingly</i> expensive (~90% of
     * the cost for saveAsOsc)
     */
    private static final TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newInstance();
    /** This improves performance for writing OSC files */
    private static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory
            .newInstance();
    private static final Logger logger = LoggerFactory.getLogger(ChangeDescription.class);
    private static final ChangeDescriptorComparator COMPARATOR = new ChangeDescriptorComparator();

    /** `visible` is used in OSC files to denote whether or not an object should be shown */
    private static final String VISIBLE = "visible";
    /** `version` is used in OSC files to help determine if there is a change conflict */
    private static final String VERSION = "version";
    /**
     * `if-unused` is used in OSC files to indicate that something should be deleted, if it is
     * unused. Any value enables it.
     */
    private static final String IF_UNUSED = "if-unused";
    /** A relation is a collection of other objects */
    private static final String RELATION = "relation";
    /** A relation member is part of a relation */
    private static final String RELATION_MEMBER = "member";

    /** The specified line separator for json (windows \r\n works, since \r is whitespace) */
    private static final String JSON_LINE_SEPARATOR = System.lineSeparator();

    private final long identifier;
    private final ItemType itemType;
    private final ChangeDescriptorType changeDescriptorType;
    private final List<ChangeDescriptor> descriptors;
    private final Collection<LocationItem> nodes;
    private final Map<String, String> originalTags;
    private final AtlasEntity afterView;
    private final AtlasEntity beforeView;
    private String osc;

    /**
     * Convert an entity to a JsonObject which can be used to create an OSC file. Note: You still
     * have to add the @code{visible="true|false"} information, as we don't know if this is
     * creating, updating, or removing.
     *
     * @param entity
     *            the entity to convert
     * @param nodes
     *            nodes for the entity (may include unrelated nodes)
     * @param tags
     *            The tags for the entity. May be {@code null}. If {@code null}, tags from the
     *            entity are used.
     * @return A JsonObject with the information needed to create an OSC file
     */
    private static JsonObject atlasEntityToOscInformation(final AtlasEntity entity,
            final Collection<? extends LocationItem> nodes,
            @Nullable final Map<String, String> tags)
    {
        final var information = new JsonObject();
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
            lastEditVersion.ifPresent(s -> information.addProperty(VERSION, Long.parseLong(s) + 1));
        }
        // Add the tags (OSC files are idempotent, in that they require <i>all</i> information for
        // an object)
        final Map<String, String> tagsToAdd;
        if (tags != null)
        {
            tagsToAdd = tags;
        }
        else if (entity.getTags() != null && !entity.getOsmTags().isEmpty())
        {
            tagsToAdd = entity.getOsmTags();
        }
        else
        {
            tagsToAdd = null;
        }
        if (tagsToAdd != null)
        {
            final var tagsObject = new JsonObject();
            tagsToAdd.forEach(tagsObject::addProperty);
            information.add("tags", tagsObject);
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
        information.addProperty("type", RELATION);
        // Relations have an array of members
        // <relation><member type="node|way|relation" ref="id" role="role"/></relation>
        final var members = new JsonArray();
        // Try to account for relations spread across atlases
        for (final RelationMember memberInformation : entity.allKnownOsmMembers())
        {
            final var memberObject = new JsonObject();
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
                type = RELATION;
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
            final LineItem entity, final Collection<? extends LocationItem> nodes)
    {
        information.addProperty("type", "way");
        // Lines have an array of node references
        // <way><nd ref="-1"/><nd ref="-2"</></way>
        final var nodeIds = new JsonArray();
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

    private static void createOscXmlElement(final Document document, final Element parentElement,
            final JsonObject object)
    {
        if (!object.has("type"))
        {
            return;
        }
        final var type = object.get("type").getAsString();
        final var objectElement = document.createElement(type);
        objectElement.setAttribute(VISIBLE, Optional.ofNullable(object.get(VISIBLE))
                .orElse(new JsonPrimitive(true)).getAsString());
        objectElement.setAttribute("id",
                Optional.ofNullable(object.get("id")).orElse(new JsonPrimitive(0)).getAsString());
        objectElement.setAttribute(VERSION, Optional.ofNullable(object.get(VERSION))
                .orElse(new JsonPrimitive(1)).getAsString());
        if (object.has(IF_UNUSED))
        {
            objectElement.setAttribute(IF_UNUSED, object.get(IF_UNUSED).getAsString());
        }
        if (object.has("tags"))
        {
            for (final Map.Entry<String, JsonElement> tag : object.get("tags").getAsJsonObject()
                    .entrySet())
            {
                final var tagElement = document.createElement("tag");
                tagElement.setAttribute("k", tag.getKey());
                tagElement.setAttribute("v", tag.getValue().getAsString());
                objectElement.appendChild(tagElement);
            }
        }
        if ("node".equals(type))
        {
            if (object.has("lat") && object.has("lon"))
            {
                final var lat = object.get("lat").getAsDouble();
                final var lon = object.get("lon").getAsDouble();
                objectElement.setAttribute("lat", Double.toString(lat));
                objectElement.setAttribute("lon", Double.toString(lon));
            }
            parentElement.appendChild(objectElement);
        }
        else if ("way".equals(type))
        {
            if (object.has("nd"))
            {
                object.get("nd").getAsJsonArray().forEach(element ->
                {
                    final var ndElement = document.createElement("nd");
                    ndElement.setAttribute("ref", element.getAsString());
                    objectElement.appendChild(ndElement);
                });
            }
            parentElement.appendChild(objectElement);
        }
        else if (RELATION.equals(type))
        {
            if (object.has(RELATION_MEMBER))
            {
                object.get(RELATION_MEMBER).getAsJsonArray().forEach(element ->
                {
                    final var member = element.getAsJsonObject();
                    final var memberElement = document.createElement(RELATION_MEMBER);
                    memberElement.setAttribute("type", member.get("type").getAsString());
                    memberElement.setAttribute("ref", member.get("ref").getAsString());
                    memberElement.setAttribute("role", member.get("role").getAsString());
                    objectElement.appendChild(memberElement);
                });
            }
            parentElement.appendChild(objectElement);
        }
    }

    /**
     * Check if the entity has OSM tags. This avoids an NPE that can be encountered when calling
     * {@link AtlasEntity#getOsmTags()} when {@link AtlasEntity#getTags()} is null.
     *
     * @param entity
     *            The entity to check
     * @return {@code true} if the entity has OSM tags
     */
    private static boolean hasOsmTags(final AtlasEntity entity)
    {
        return entity.getTags() != null && entity.getOsmTags() != null
                && !entity.getOsmTags().isEmpty();
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
     * @throws ParserConfigurationException
     *             if a parser cannot be configured
     * @throws TransformerException
     *             if we could not transform the generated XML to a string.
     */
    // Suppress java:S2755 -- external entity vulnerabilities. We are building the XML, not parsing
    // it.
    @SuppressWarnings("java:S2755")
    private static void saveAsOsc(final JsonObject description, final Collection<JsonObject> create,
            final Collection<JsonObject> modify, final Collection<JsonObject> delete)
            throws ParserConfigurationException, TransformerException
    {
        DOCUMENT_BUILDER_FACTORY.setExpandEntityReferences(false);

        final var builder = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
        final var document = builder.newDocument();
        final var rootElement = document.createElement("osmChange");
        rootElement.setAttribute(VERSION, "0.6");
        // Increment the version when this code changes
        rootElement.setAttribute("generator", "atlas ChangeDescription v0.0.1");
        document.appendChild(rootElement);
        if (!create.isEmpty())
        {
            final var createElement = document.createElement("create");
            create.forEach(
                    createObject -> createOscXmlElement(document, createElement, createObject));
            rootElement.appendChild(createElement);
        }
        if (!modify.isEmpty())
        {
            final var modifyElement = document.createElement("modify");
            modify.forEach(jsonObject -> createOscXmlElement(document, modifyElement, jsonObject));
            rootElement.appendChild(modifyElement);
        }
        if (!delete.isEmpty())
        {
            final var deleteElement = document.createElement("delete");
            delete.forEach(
                    deleteObject -> createOscXmlElement(document, deleteElement, deleteObject));
            rootElement.appendChild(deleteElement);
        }
        if (rootElement.hasChildNodes())
        {
            final var transformer = TRANSFORMER_FACTORY.newTransformer();
            final var stringWriter = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(stringWriter));
            final var docString = stringWriter.getBuffer().toString();

            // MapRoulette uses OSC in base64 encoded format, and this is a good way to ensure that
            // everything is in place
            saveOsc(description,
                    Base64.getEncoder().encodeToString(docString.getBytes(StandardCharsets.UTF_8)));
        }
    }

    /**
     * Save OSC to a JsonObject
     *
     * @param description
     *            The object to save to
     * @param osc
     *            The osc to save
     */
    private static void saveOsc(final JsonObject description, final String osc)
    {
        description.add("osc", new JsonPrimitive(osc));
    }

    public ChangeDescription(final long identifier, final ItemType itemType,
            final AtlasEntity beforeView, final AtlasEntity afterView,
            final ChangeType sourceFeatureChangeType)
    {
        this(identifier, itemType, beforeView, afterView, sourceFeatureChangeType, null,
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
     * @param originalTags
     *            The original object's tags
     * @param nodes
     *            The nodes to be used for way geometry changes, in order. If a collection has
     *            multiple nodes, then no geometry changes for an OSC will be written.
     */
    public ChangeDescription(final long identifier, final ItemType itemType,
            final AtlasEntity beforeView, final AtlasEntity afterView,
            final ChangeType sourceFeatureChangeType, final Map<String, String> originalTags,
            final Collection<LocationItem> nodes)
    {
        this.identifier = identifier;
        this.itemType = itemType;
        this.descriptors = new ArrayList<>();

        this.nodes = nodes != null ? nodes : Collections.emptyList();
        this.originalTags = originalTags != null ? originalTags : Collections.emptyMap();
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

    /**
     * Get the OSC, if one exists
     *
     * @return An optional with the osc, if present
     */
    public Optional<String> getOsc()
    {
        return Optional.ofNullable(this.osc);
    }

    /**
     * Set the OSC information (this is for deserialization, please do not call when not
     * deserializing)
     *
     * @param osc
     *            The osc to set (base64 encoded)
     */
    public void setOsc(final String osc)
    {
        this.osc = osc;
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

        if (this.osc == null)
        {
            this.createOsc(description);
        }
        else
        {
            saveOsc(description, this.osc);
        }
        return description;
    }

    @Override
    public String toString()
    {
        this.descriptors.sort(COMPARATOR);
        final var builder = new StringBuilder(22 + 2 * JSON_LINE_SEPARATOR.length());
        builder.append("ChangeDescription [");
        builder.append(JSON_LINE_SEPARATOR);
        builder.append(this.changeDescriptorType);
        builder.append(" ");
        builder.append(this.itemType);
        builder.append(" ");
        builder.append(this.getIdentifier());
        builder.append(JSON_LINE_SEPARATOR);

        if (this.descriptors.isEmpty())
        {
            builder.append("]");
            return builder.toString();
        }

        for (int i = 0; i < this.descriptors.size() - 1; i++)
        {
            builder.append(this.descriptors.get(i));
            builder.append(JSON_LINE_SEPARATOR);
        }
        builder.append(this.descriptors.get(this.descriptors.size() - 1));
        builder.append(JSON_LINE_SEPARATOR);
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
        this.oscCreateUpdate(create, modify);
        if (this.updateRequiredLocations(create, modify, requiredLocations))
        {
            this.oscDelete(delete, requiredLocations);
            try
            {
                saveAsOsc(description, create, modify, delete);
            }
            catch (final TransformerException | ParserConfigurationException e)
            {
                logger.error("Could not save OpenStreetMap Change information", e);
            }
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
    private void oscCreateUpdate(final List<? super JsonObject> create,
            final Collection<? super JsonObject> modify)
    {
        if ((this.changeDescriptorType == ChangeDescriptorType.ADD
                || this.changeDescriptorType == ChangeDescriptorType.UPDATE)
                && this.afterView != null)
        {
            final Map<String, String> tags;
            if (!hasOsmTags(this.afterView) && !hasOsmTags(this.beforeView)
                    && !this.originalTags.isEmpty())
            {
                tags = this.originalTags;
            }
            else if (hasOsmTags(this.afterView))
            {
                tags = this.afterView.getOsmTags();
            }
            else
            {
                tags = null;
            }
            final JsonObject createObject = atlasEntityToOscInformation(this.afterView, this.nodes,
                    tags);
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
            final JsonObject deleteObject = atlasEntityToOscInformation(this.beforeView, this.nodes,
                    null);
            if (deleteObject != null)
            {
                deleteObject.addProperty(VISIBLE, false);
                // This helps ensure that we don't accidentally delete something if the OSC is
                // directly
                // applied to the OSM API.
                deleteObject.addProperty(IF_UNUSED, true);
                // Remove lat/lon, since we are deleting the object.
                deleteObject.remove("lat");
                deleteObject.remove("lon");
                delete.add(deleteObject);
            }
        }
        this.oscDeleteNodes(delete, requiredLocations);
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
                final var localNodes = entityObject.get("nd").getAsJsonArray();
                for (final JsonElement nodeElement : localNodes)
                {
                    final var nodeId = nodeElement.getAsLong();
                    final LocationItem node = this.nodes.stream()
                            .filter(node1 -> node1.getOsmIdentifier() == nodeId).findAny()
                            .filter(node1 -> node1.getTags() != null)
                            .filter(node1 -> node1.getOsmTags().isEmpty())
                            .filter(node1 -> !requiredLocations.contains(node1.getLocation()))
                            .orElse(null);
                    // Don't delete nodes with tags
                    if (node != null)
                    {
                        final JsonObject nodeDelete = atlasEntityToOscInformation(node, null,
                                Collections.emptyMap());
                        nodeDelete.addProperty(VISIBLE, false);
                        // This helps ensure that we don't accidentally delete something if the
                        // OSC is directly applied to the OSM API.
                        nodeDelete.addProperty(IF_UNUSED, true);
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
            final Collection<JsonObject> modify,
            final Collection<? super Location> requiredLocations)
    {
        for (final JsonObject entityObject : Stream.concat(create.stream(), modify.stream())
                .collect(Collectors.toList()))
        {
            if (entityObject.get("nd") == null)
            {
                continue;
            }
            final var localNodes = entityObject.get("nd").getAsJsonArray();
            for (final JsonElement nodeElement : localNodes)
            {
                final var nodeId = nodeElement.getAsLong();
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
                    final JsonObject newNode = atlasEntityToOscInformation(nodesFound.get(0), null,
                            null);
                    newNode.addProperty(VISIBLE, true);
                    create.add(0, newNode);
                }
                requiredLocations.add(nodesFound.get(0).getLocation());
            }
        }
        return true;
    }
}
