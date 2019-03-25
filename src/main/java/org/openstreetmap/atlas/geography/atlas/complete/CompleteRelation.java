package org.openstreetmap.atlas.geography.atlas.complete;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean.RelationBeanItem;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.geography.atlas.items.RelationMemberList;
import org.openstreetmap.atlas.utilities.collections.Iterables;

/**
 * Independent {@link Relation} that contains its own data. At scale, use at your own risk.
 *
 * @author matthieun
 */
public class CompleteRelation extends Relation implements CompleteEntity
{
    private static final long serialVersionUID = -8295865049110084558L;

    private Rectangle bounds;
    private long identifier;
    private Map<String, String> tags;
    private RelationBean members;
    private List<Long> allRelationsWithSameOsmIdentifier;
    private RelationBean allKnownOsmMembers;
    private Long osmRelationIdentifier;
    private Set<Long> relationIdentifiers;

    public static CompleteRelation from(final Relation relation)
    {
        return new CompleteRelation(relation.getIdentifier(), relation.getTags(), relation.bounds(),
                relation.members().asBean(),
                relation.allRelationsWithSameOsmIdentifier().stream().map(Relation::getIdentifier)
                        .collect(Collectors.toList()),
                relation.allKnownOsmMembers().asBean(), relation.osmRelationIdentifier(),
                relation.relations().stream().map(Relation::getIdentifier)
                        .collect(Collectors.toSet()));
    }

    public static CompleteRelation shallowFrom(final Relation relation)
    {
        return new CompleteRelation(relation.getIdentifier())
                .withBoundsExtendedBy(relation.bounds());
    }

    CompleteRelation(final long identifier)
    {
        this(identifier, null, null, null, null, null, null, null);
    }

    public CompleteRelation(final Long identifier, final Map<String, String> tags, // NOSONAR
            final Rectangle bounds, final RelationBean members,
            final List<Long> allRelationsWithSameOsmIdentifier,
            final RelationBean allKnownOsmMembers, final Long osmRelationIdentifier,
            final Set<Long> relationIdentifiers)
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
    }

    protected CompleteRelation(final Atlas atlas)
    {
        super(atlas);
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
    public Rectangle bounds()
    {
        return this.bounds;
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
                    && Objects.equals(this.osmRelationIdentifier(), that.osmRelationIdentifier());
        }
        return false;
    }

    @Override
    public long getIdentifier()
    {
        return this.identifier;
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
    public boolean isCompletelyShallow()
    {
        return this.bounds == null && this.members == null
                && this.allRelationsWithSameOsmIdentifier == null && this.allKnownOsmMembers == null
                && this.osmRelationIdentifier == null && this.tags == null
                && this.relationIdentifiers == null;
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
    public String toString()
    {
        return this.getClass().getSimpleName() + " [identifier=" + this.identifier + ", tags="
                + this.tags + ", members=" + this.members + ", relationIdentifiers="
                + this.relationIdentifiers + "]";
    }

    @Override
    public CompleteRelation withAddedTag(final String key, final String value)
    {
        return withTags(CompleteEntity.addNewTag(getTags(), key, value));
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

    public CompleteRelation withExtraMember(final AtlasEntity newMember,
            final AtlasEntity memberFromWhichToCopyRole)
    {
        final Relation sourceRelation = Iterables.stream(memberFromWhichToCopyRole.relations())
                .firstMatching(relation -> relation.getIdentifier() == this.getIdentifier())
                .orElseThrow(() -> new CoreException(
                        "Cannot copy role from {} {} as it does not have relation {} as parent",
                        memberFromWhichToCopyRole.getType(),
                        memberFromWhichToCopyRole.getIdentifier(), this.getIdentifier()));
        final String role = sourceRelation.members().asBean()
                .getItemFor(memberFromWhichToCopyRole.getIdentifier(),
                        memberFromWhichToCopyRole.getType())
                .orElseThrow(() -> new CoreException(
                        "Cannot copy role from {} {} as it is not a member of {} {}",
                        memberFromWhichToCopyRole.getType(),
                        memberFromWhichToCopyRole.getIdentifier(), this.getClass().getSimpleName(),
                        this))
                .getRole();
        return withExtraMember(newMember, role);
    }

    public CompleteRelation withExtraMember(final AtlasEntity newMember, final String role)
    {
        this.members.addItem(
                new RelationBeanItem(newMember.getIdentifier(), role, newMember.getType()));
        this.updateBounds(newMember.bounds());
        return this;
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
        return withMembersAndSource(members.asBean(), source, members.bounds());
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

    @Override
    public CompleteRelation withRemovedTag(final String key)
    {
        return withTags(CompleteEntity.removeTag(getTags(), key));
    }

    @Override
    public CompleteRelation withReplacedTag(final String oldKey, final String newKey,
            final String newValue)
    {
        return withRemovedTag(oldKey).withAddedTag(newKey, newValue);
    }

    @Override
    public CompleteRelation withTags(final Map<String, String> tags)
    {
        this.tags = tags;
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
