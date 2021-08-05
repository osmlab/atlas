package org.openstreetmap.atlas.geography.atlas.complete;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean.RelationBeanItem;
import org.openstreetmap.atlas.geography.atlas.change.eventhandling.event.TagChangeEvent;
import org.openstreetmap.atlas.geography.atlas.change.eventhandling.listener.TagChangeListener;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.geography.atlas.items.RelationMemberList;
import org.openstreetmap.atlas.geography.atlas.items.complex.RelationOrAreaToMultiPolygonConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsMultiPolygonToMultiPolygonConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsPolyLineConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsPolygonConverter;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Independent {@link Relation} that contains its own data. At scale, use at your own risk.
 *
 * @author matthieun
 * @author Yazad Khambata
 */
public class CompleteRelation extends Relation implements CompleteEntity<CompleteRelation>
{
    private static final long serialVersionUID = -8295865049110084558L;
    private static final Logger logger = LoggerFactory.getLogger(CompleteRelation.class);

    private Rectangle bounds;
    private long identifier;
    private Map<String, String> tags;
    private RelationBean members;
    private List<Long> allRelationsWithSameOsmIdentifier;
    private RelationBean allKnownOsmMembers;
    private Long osmRelationIdentifier;
    private Set<Long> relationIdentifiers;
    private MultiPolygon storedGeometry;
    private boolean overrideGeometry = false;
    private final Set<LineString> addedGeometry = new HashSet<>();
    private final Set<LineString> removedGeometry = new HashSet<>();

    private final TagChangeDelegate tagChangeDelegate = TagChangeDelegate.newTagChangeDelegate();

    /**
     * Create a {@link CompleteRelation} from a given {@link Relation} reference. The
     * {@link CompleteRelation}'s fields will match the fields of the reference. The returned
     * {@link CompleteRelation} will be full, i.e. all of its associated fields will be non-null.
     *
     * @param relation
     *            the {@link Relation} to copy
     * @return the full {@link CompleteRelation}
     */
    public static CompleteRelation from(final Relation relation)
    {
        if (relation instanceof CompleteRelation && !((CompleteRelation) relation).isFull())
        {
            throw new CoreException(
                    "Relation parameter was a CompleteRelation but it was not full: {}", relation);
        }
        return new CompleteRelation(relation.getIdentifier(), relation.getTags(), relation.bounds(),
                relation.members().asBean(),
                relation.allRelationsWithSameOsmIdentifier().stream().map(Relation::getIdentifier)
                        .collect(Collectors.toList()),
                relation.allKnownOsmMembers().asBean(), relation.osmRelationIdentifier(),
                relation.relations().stream().map(Relation::getIdentifier)
                        .collect(Collectors.toSet()),
                relation.asMultiPolygon().orElse(null));
    }

    /**
     * Create a shallow {@link CompleteRelation} from a given {@link Relation} reference. The
     * {@link CompleteRelation}'s identifier will match the identifier of the reference
     * {@link Relation}. The returned {@link CompleteRelation} will be shallow, i.e. all of its
     * associated fields will be null except for the identifier.
     *
     * @param relation
     *            the {@link Relation} to copy
     * @return the shallow {@link CompleteRelation}
     */
    public static CompleteRelation shallowFrom(final Relation relation)
    {
        if (relation.bounds() == null)
        {
            throw new CoreException("Relation parameter bounds were null");
        }
        return new CompleteRelation(relation.getIdentifier())
                .withBoundsExtendedBy(relation.bounds());
    }

    public CompleteRelation(final long identifier)
    {
        this(identifier, null, null, null, null, null, null, null);
    }

    public CompleteRelation(final Long identifier, final Map<String, String> tags, // NOSONAR
            final Rectangle bounds, final RelationBean members,
            final List<Long> allRelationsWithSameOsmIdentifier,
            final RelationBean allKnownOsmMembers, final Long osmRelationIdentifier,
            final Set<Long> relationIdentifiers)
    {
        this(identifier, tags, bounds, members, allRelationsWithSameOsmIdentifier,
                allKnownOsmMembers, osmRelationIdentifier, relationIdentifiers, null);
    }

    public CompleteRelation(final Long identifier, final Map<String, String> tags, // NOSONAR
            final Rectangle bounds, final RelationBean members,
            final List<Long> allRelationsWithSameOsmIdentifier,
            final RelationBean allKnownOsmMembers, final Long osmRelationIdentifier,
            final Set<Long> relationIdentifiers, final MultiPolygon jtsGeometry)
    {
        super(new EmptyAtlas());

        if (identifier == null)
        {
            throw new CoreException("Identifier can never be null.");
        }

        this.bounds = bounds != null ? bounds : null;

        this.identifier = identifier;
        this.tags = tags;
        this.members = members;
        this.allRelationsWithSameOsmIdentifier = allRelationsWithSameOsmIdentifier;
        this.allKnownOsmMembers = allKnownOsmMembers;
        this.osmRelationIdentifier = osmRelationIdentifier;
        this.relationIdentifiers = relationIdentifiers;
        this.storedGeometry = jtsGeometry;
    }

    protected CompleteRelation(final Atlas atlas)
    {
        super(atlas);
    }

    @Override
    public void addTagChangeListener(final TagChangeListener tagChangeListener)
    {
        this.tagChangeDelegate.addTagChangeListener(tagChangeListener);
    }

    @Override
    public RelationMemberList allKnownOsmMembers()
    {
        return membersFor(this.allKnownOsmMembers);
    }

    @Override
    public List<Relation> allRelationsWithSameOsmIdentifier()
    {
        /*
         * Note that the Relations returned by this method will technically break the Located
         * contract, since they have null bounds.
         */
        return this.allRelationsWithSameOsmIdentifier == null ? null
                : this.allRelationsWithSameOsmIdentifier.stream().map(CompleteRelation::new)
                        .collect(Collectors.toList());
    }

    @Override
    public Optional<MultiPolygon> asMultiPolygon()
    {
        return Optional.ofNullable(this.storedGeometry);
    }

    @Override
    public Rectangle bounds()
    {
        return this.bounds;
    }

    public CompleteRelation changeMemberRole(final AtlasEntity member, final String role)
    {
        final Optional<RelationBeanItem> oldItem = this.members.getItemFor(member.getIdentifier(),
                member.getType());
        if (oldItem.isPresent())
        {
            if (isGeometric() && this.asMultiPolygon().isPresent())
            {
                throw new CoreException(
                        "Cannot modify roles directly for relations with existing geometry! Please remove and add members directly instead");
            }
            else
            {
                final RelationBeanItem old = oldItem.get();
                this.members.removeItem(old);
                final RelationBeanItem newItem = new RelationBeanItem(member.getIdentifier(), role,
                        member.getType());
                this.members.addItem(newItem);
                this.members.addItemExplicitlyExcluded(member.getIdentifier(), old.getRole(),
                        member.getType());
            }
        }
        return this;
    }

    @Override
    public CompleteItemType completeItemType()
    {
        return CompleteItemType.RELATION;
    }

    public CompleteRelation copy()
    {
        return new CompleteRelation(this.identifier, this.tags, this.bounds, this.members,
                this.allRelationsWithSameOsmIdentifier, this.allKnownOsmMembers,
                this.osmRelationIdentifier, this.relationIdentifiers, this.storedGeometry);
    }

    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof CompleteRelation)
        {
            final CompleteRelation that = (CompleteRelation) other;
            return CompleteEntity.basicEqual(this, that)
                    && CompleteEntity.equalThroughGet(this.members(), that.members(),
                            RelationMemberList::asBean)
                    && Objects.equals(this.allRelationsWithSameOsmIdentifier(),
                            that.allRelationsWithSameOsmIdentifier())
                    && CompleteEntity.equalThroughGet(this.allKnownOsmMembers(),
                            that.allKnownOsmMembers(), RelationMemberList::asBean)
                    && Objects.equals(this.osmRelationIdentifier(), that.osmRelationIdentifier())
                    && Objects.equals(this.asMultiPolygon(), that.asMultiPolygon())
                    && Objects.equals(this.getAddedGeometry(), that.getAddedGeometry())
                    && Objects.equals(this.getRemovedGeometry(), that.getRemovedGeometry());
        }
        return false;
    }

    @Override
    public void fireTagChangeEvent(final TagChangeEvent tagChangeEvent)
    {
        this.tagChangeDelegate.fireTagChangeEvent(tagChangeEvent);
    }

    public Set<LineString> getAddedGeometry()
    {
        if (this.overrideGeometry)
        {
            return new HashSet<>();
        }
        return this.addedGeometry;
    }

    @Override
    public Iterable<Location> getGeometry()
    {
        // TO DO enable for geometric?
        throw new UnsupportedOperationException("Relations do not have an explicit geometry."
                + " Please instead use bounds to check the apparent geometry.");
    }

    @Override
    public long getIdentifier()
    {
        return this.identifier;
    }

    public Set<LineString> getRemovedGeometry()
    {
        if (this.overrideGeometry)
        {
            return new HashSet<>();
        }
        return this.removedGeometry;
    }

    @Override
    public Map<String, String> getTags()
    {
        return this.tags;
    }

    @Override
    public int hashCode()
    {
        return super.hashCode();
    }

    @Override
    public boolean isFull()
    {
        return this.bounds != null && this.tags != null && this.members != null
                && this.allRelationsWithSameOsmIdentifier != null && this.allKnownOsmMembers != null
                && this.osmRelationIdentifier != null && this.relationIdentifiers != null
                && (!this.isGeometric() || this.storedGeometry != null);
    }

    @Override
    public boolean isGeometric()
    {
        return this.getTags() == null ? false : super.isGeometric();
    }

    public boolean isOverrideGeometry()
    {
        return this.overrideGeometry;
    }

    @Override
    public boolean isShallow()
    {
        return this.bounds == null && this.members == null
                && this.allRelationsWithSameOsmIdentifier == null && this.allKnownOsmMembers == null
                && this.osmRelationIdentifier == null && this.tags == null
                && this.relationIdentifiers == null && this.storedGeometry == null;
    }

    @Override
    public RelationMemberList members()
    {
        return membersFor(this.members);
    }

    @Override
    public Long osmRelationIdentifier()
    {
        return this.osmRelationIdentifier;
    }

    @Override
    public String prettify(final PrettifyStringFormat format, final boolean truncate)
    {
        String separator = "";
        if (format == PrettifyStringFormat.MINIMAL_SINGLE_LINE)
        {
            separator = "";
        }
        else if (format == PrettifyStringFormat.MINIMAL_MULTI_LINE)
        {
            separator = "\n";
        }
        final StringBuilder builder = new StringBuilder();

        builder.append(this.getClass().getSimpleName() + " ");
        builder.append("[");
        builder.append(separator);
        builder.append("identifier: " + this.identifier + ", ");
        builder.append(separator);
        if (this.tags != null)
        {
            builder.append("tags: " + new TreeMap<>(this.tags) + ", ");
            builder.append(separator);
        }
        if (this.members != null && !this.members.isEmpty())
        {
            builder.append("members: " + this.members + ", ");
            builder.append(separator);
        }
        if (this.relationIdentifiers != null)
        {
            builder.append("parentRelations: " + new TreeSet<>(this.relationIdentifiers) + ", ");
            builder.append(separator);
        }
        if (this.bounds != null)
        {
            builder.append("bounds: " + this.bounds.toWkt() + ", ");
            builder.append(separator);
        }
        if (this.isGeometric())
        {
            builder.append("multiPolygonGeometry: "
                    + this.asMultiPolygon().map(Geometry::toText).orElse("null"));
        }
        builder.append(separator);
        builder.append("]");

        return builder.toString();
    }

    @Override
    public Set<Long> relationIdentifiers()
    {
        return this.relationIdentifiers;
    }

    @Override
    public Set<Relation> relations()
    {
        /*
         * Note that the Relations returned by this method will technically break the Located
         * contract, since they have null bounds.
         */
        return this.relationIdentifiers == null ? null
                : this.relationIdentifiers.stream().map(CompleteRelation::new)
                        .collect(Collectors.toSet());
    }

    @Override
    public void removeTagChangeListeners()
    {
        this.tagChangeDelegate.removeTagChangeListeners();
    }

    @Override
    public void setTags(final Map<String, String> tags)
    {
        this.tags = tags != null ? new HashMap<>(tags) : null;
    }

    @Override
    public JsonObject toJson()
    {
        final JsonObject relationObject = super.toJson();

        final JsonArray membersArray = new JsonArray();
        for (final RelationBeanItem item : this.members)
        {
            final JsonObject memberObject = new JsonObject();
            memberObject.addProperty("type", item.getType().toString());
            memberObject.addProperty("identifier", item.getIdentifier());
            memberObject.addProperty("role", item.getRole());
            membersArray.add(memberObject);
        }
        relationObject.add("members", membersArray);
        relationObject.addProperty("multiPolygonGeometry",
                this.asMultiPolygon().map(Geometry::toText).orElse("null"));

        return relationObject;
    }

    @Override
    public String toString()
    {
        return this.getClass().getSimpleName() + " [identifier=" + this.identifier + ", tags="
                + this.tags + ", members=" + this.members + ", relationIdentifiers="
                + this.relationIdentifiers + "]";
    }

    @Override
    public String toWkt()
    {
        if (this.isGeometric() && this.storedGeometry != null)
        {
            return this.storedGeometry.toText();
        }
        if (this.bounds == null)
        {
            return null;
        }
        return this.bounds.toWkt();
    }

    public void updateGeometry()
    {
        try
        {
            this.storedGeometry = new JtsMultiPolygonToMultiPolygonConverter()
                    .backwardConvert(new RelationOrAreaToMultiPolygonConverter().convert(this));
        }
        catch (final Exception exc)
        {
            logger.error("Couldn't reconstruct geometry for relation {}", this, exc);
            this.storedGeometry = null;
        }
    }

    public CompleteRelation withAddedMember(final AtlasEntity newMember,
            final AtlasEntity memberFromWhichToCopyRole)
    {
        // TO DO support geometry
        final Relation parentRelation = Iterables.stream(memberFromWhichToCopyRole.relations())
                .firstMatching(relation -> relation.getIdentifier() == this.getIdentifier())
                .orElseThrow(() -> new CoreException(
                        "Cannot copy role from {} {} as it does not have relation {} as parent",
                        memberFromWhichToCopyRole.getType(),
                        memberFromWhichToCopyRole.getIdentifier(), this.getIdentifier()));
        final String role = parentRelation.members().asBean()
                .getItemFor(memberFromWhichToCopyRole.getIdentifier(),
                        memberFromWhichToCopyRole.getType())
                .orElseThrow(() -> new CoreException(
                        "Cannot copy role from {} {} as it is not a member of {} {}",
                        memberFromWhichToCopyRole.getType(),
                        memberFromWhichToCopyRole.getIdentifier(), this.getClass().getSimpleName(),
                        this))
                .getRole();
        return withAddedMember(newMember, role);
    }

    public CompleteRelation withAddedMember(final AtlasEntity newMember, final String role)
    {
        if (this.members == null)
        {
            final Collection<RelationMember> newMembers = new ArrayList<>();
            newMembers.add(new RelationMember(role, newMember, this.getIdentifier()));
            final RelationMemberList memberList = new RelationMemberList(newMembers);
            return this.withMembers(memberList);
        }
        this.members.addItem(
                new RelationBeanItem(newMember.getIdentifier(), role, newMember.getType()));
        if (this.isGeometric() && this.asMultiPolygon().isPresent()
                && (role.equalsIgnoreCase(Ring.INNER.toString())
                        || role.equalsIgnoreCase(Ring.OUTER.toString())))
        {
            switch (newMember.getType())
            {
                case LINE:
                    this.addedGeometry.add(
                            new JtsPolyLineConverter().convert(((Line) newMember).asPolyLine()));
                    break;
                case AREA:
                    this.addedGeometry.add(
                            new JtsPolyLineConverter().convert(((Area) newMember).asPolygon()));
                    break;
                case EDGE:
                    this.addedGeometry.add(
                            new JtsPolyLineConverter().convert(((Edge) newMember).asPolyLine()));
                    break;
                case NODE:
                case POINT:
                case RELATION:
                default:
                    break;
            }
        }
        this.withBoundsExtendedBy(newMember.bounds());
        return this;
    }

    @Override
    public CompleteRelation withAddedRelationIdentifier(final Long relationIdentifier)
    {
        this.relationIdentifiers.add(relationIdentifier);
        return this;
    }

    public CompleteRelation withAllKnownOsmMembers(final RelationBean allKnownOsmMembers)
    {
        this.allKnownOsmMembers = allKnownOsmMembers;
        return this;
    }

    public CompleteRelation withAllRelationsWithSameOsmIdentifier(
            final List<Long> allRelationsWithSameOsmIdentifier)
    {
        this.allRelationsWithSameOsmIdentifier = allRelationsWithSameOsmIdentifier;
        return this;
    }

    public CompleteRelation withBounds(final Rectangle bounds)
    {
        this.bounds = bounds;
        return this;
    }

    public CompleteRelation withBoundsExtendedBy(final Rectangle bounds)
    {
        if (this.bounds == null)
        {
            this.bounds = bounds;
            return this;
        }
        this.bounds = Rectangle.forLocated(this.bounds, bounds);
        return this;
    }

    @Override
    public CompleteEntity withGeometry(final Iterable<Location> locations)
    {
        throw new UnsupportedOperationException("Relations cannot have an explicit geometry."
                + " Please instead use withBounds or withBoundsExtendedBy to adjust the bounds.");
    }

    @Override
    public CompleteRelation withIdentifier(final long identifier)
    {
        this.identifier = identifier;
        return this;
    }

    /**
     * Assign this {@link CompleteRelation} with members.
     * <p>
     * In case this {@link CompleteRelation} is created from an existing relation, and the new
     * member list has had some existing members removed, use
     * {@link #withMembersAndSource(RelationBean, Relation, Rectangle)}
     *
     * @param members
     *            The members of the relation
     * @param bounds
     *            The bounds of all the members of the relation.
     * @return This
     */
    public CompleteRelation withMembers(final RelationBean members, final Rectangle bounds)
    {
        this.members = members;
        updateBounds(bounds);
        return this;
    }

    /**
     * Assign this {@link CompleteRelation} with members.
     * <p>
     * In case this {@link CompleteRelation} is created from an existing relation, and the new
     * member list has had some existing members removed, use
     * {@link #withMembersAndSource(RelationMemberList, Relation)}
     *
     * @param members
     *            The full members of the Relation
     * @return This
     */
    public CompleteRelation withMembers(final RelationMemberList members)
    {
        return withMembers(members.asBean(), members.bounds());
    }

    /**
     * @param members
     *            The members of the relation
     * @param source
     *            The relation that was used as a base to create that {@link CompleteRelation}, if
     *            any. Due to the weak nature of relation membership across Atlas(es), this helps
     *            decide what relation members are forcibly removed if any.
     * @param bounds
     *            The bounds of all the members of the relation.
     * @return This.
     */
    public CompleteRelation withMembersAndSource(final RelationBean members, final Relation source,
            final Rectangle bounds)
    {
        if (source == null)
        {
            throw new CoreException("Source relation must not be null.");
        }
        this.members = members;

        // This has been created from an existing relation, make sure to record the members that
        // have been intentionally omitted, so as not to add them back in the future when either
        // merging FeatureChanges or stitching MultiAtlases.
        for (final RelationMember member : source.members())
        {
            if (!members.getItemFor(member.getEntity().getIdentifier(), member.getRole(),
                    member.getEntity().getType()).isPresent())
            {
                this.members.addItemExplicitlyExcluded(member.getEntity().getIdentifier(),
                        member.getRole(), member.getEntity().getType());
            }
        }
        updateBounds(bounds);
        return this;
    }

    /**
     * Here the members have to be full so the new bounds can be computed from them.
     *
     * @param members
     *            The full members of the Relation
     * @param source
     *            The relation that was used as a base to create that {@link CompleteRelation}, if
     *            any. Due to the weak nature of relation membership across Atlas(es), this helps
     *            decide what relation members are forcibly removed if any.
     * @return This.
     */
    public CompleteRelation withMembersAndSource(final RelationMemberList members,
            final Relation source)
    {
        if (source instanceof CompleteRelation)
        {
            throw new CoreException(
                    "This version of withMembersAndSource must use a source Relation that is tied to an atlas, instead found Relation of type {}",
                    source.getClass().getName());
        }
        return withMembersAndSource(members.asBean(), source, members.bounds());
    }

    public CompleteRelation withMultiPolygonGeometry(final MultiPolygon geometry)
    {
        this.storedGeometry = geometry;
        this.overrideGeometry = true;
        if (geometry != null && geometry.getEnvelope() instanceof Polygon)
        {
            this.bounds = Rectangle.forLocated(
                    new JtsPolygonConverter().backwardConvert((Polygon) geometry.getEnvelope()));
        }
        return this;
    }

    public CompleteRelation withOsmRelationIdentifier(final Long osmRelationIdentifier)
    {
        this.osmRelationIdentifier = osmRelationIdentifier;
        return this;
    }

    @Override
    public CompleteRelation withRelationIdentifiers(final Set<Long> relationIdentifiers)
    {
        this.relationIdentifiers = relationIdentifiers;
        return this;
    }

    @Override
    public CompleteRelation withRelations(final Set<Relation> relations)
    {
        this.relationIdentifiers = relations.stream().map(Relation::getIdentifier)
                .collect(Collectors.toSet());
        return this;
    }

    public CompleteRelation withRemovedMember(final AtlasEntity memberToRemove)
    {
        final List<String> roles = this.members
                .removeAllMatchingItems(memberToRemove.getIdentifier(), memberToRemove.getType());
        roles.forEach(role -> this.members.addItemExplicitlyExcluded(memberToRemove.getIdentifier(),
                role, memberToRemove.getType()));
        if (this.isGeometric() && this.asMultiPolygon().isPresent()
                && (roles.contains(Ring.INNER.toString()) || roles.contains(Ring.OUTER.toString())
                        || roles.contains(Ring.INNER.toString().toLowerCase())
                        || roles.contains(Ring.OUTER.toString().toLowerCase())))
        {
            switch (memberToRemove.getType())
            {
                case LINE:
                    this.removedGeometry.add(new JtsPolyLineConverter()
                            .convert(((Line) memberToRemove).asPolyLine()));
                    break;
                case AREA:
                    this.removedGeometry.add(new JtsPolyLineConverter()
                            .convert(((Area) memberToRemove).asPolygon()));
                    break;
                case EDGE:
                    this.removedGeometry.add(new JtsPolyLineConverter()
                            .convert(((Edge) memberToRemove).asPolyLine()));
                    break;
                case NODE:
                case POINT:
                case RELATION:
                default:
                    break;
            }
        }
        return this;
    }

    public CompleteRelation withRemovedMember(final AtlasEntity memberToRemove, final String role)
    {
        final boolean success = this.members.removeItem(memberToRemove.getIdentifier(), role,
                memberToRemove.getType());
        if (success)
        {
            this.members.addItemExplicitlyExcluded(memberToRemove.getIdentifier(), role,
                    memberToRemove.getType());
            if (this.isGeometric() && this.asMultiPolygon().isPresent()
                    && (role.equalsIgnoreCase(Ring.INNER.toString())
                            || role.equalsIgnoreCase(Ring.OUTER.toString())))
            {
                switch (memberToRemove.getType())
                {
                    case LINE:
                        this.removedGeometry.add(new JtsPolyLineConverter()
                                .convert(((Line) memberToRemove).asPolyLine()));
                        break;
                    case AREA:
                        this.removedGeometry.add(new JtsPolyLineConverter()
                                .convert(((Area) memberToRemove).asPolygon()));
                        break;
                    case EDGE:
                        this.removedGeometry.add(new JtsPolyLineConverter()
                                .convert(((Edge) memberToRemove).asPolyLine()));
                        break;
                    case NODE:
                    case POINT:
                    case RELATION:
                    default:
                        break;
                }
            }
        }
        return this;
    }

    @Override
    public CompleteRelation withRemovedRelationIdentifier(final Long relationIdentifier)
    {
        this.relationIdentifiers = this.relationIdentifiers.stream()
                .filter(keepId -> keepId != relationIdentifier.longValue())
                .collect(Collectors.toSet());
        return this;
    }

    private RelationMemberList membersFor(final RelationBean bean)
    {
        if (bean == null)
        {
            return null;
        }
        final List<RelationMember> memberList = new ArrayList<>();
        for (final RelationBeanItem item : bean)
        {
            memberList.add(new RelationMember(item.getRole(),
                    getAtlas().entity(item.getIdentifier(), item.getType()), getIdentifier()));
        }
        final RelationMemberList result = new RelationMemberList(memberList);
        bean.getExplicitlyExcluded().forEach(result::addItemExplicitlyExcluded);
        return result;
    }

    private void updateBounds(final Rectangle bounds)
    {
        this.bounds = bounds;
    }
}
