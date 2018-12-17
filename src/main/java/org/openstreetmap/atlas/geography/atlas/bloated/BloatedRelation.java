package org.openstreetmap.atlas.geography.atlas.bloated;

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
public class BloatedRelation extends Relation implements BloatedEntity
{
    private static final long serialVersionUID = -8295865049110084558L;

    /*
     * We need to store the original entity bounds at creation-time. This is so multiple consecutive
     * with(Located) calls can update the aggregate bounds without including the bounds from the
     * overwritten change.
     */
    private Rectangle originalBounds;

    /*
     * This is the aggregate feature bounds. It is a super-bound of the original bounds and the
     * changed bounds, if present. Each time with(Located) is called on this entity, it is
     * recomputed from the original bounds and the new Located bounds.
     */
    private Rectangle aggregateBounds;

    private long identifier;
    private Map<String, String> tags;
    private RelationBean members;
    private List<Long> allRelationsWithSameOsmIdentifier;
    private RelationBean allKnownOsmMembers;
    private Long osmRelationIdentifier;
    private Set<Long> relationIdentifiers;

    public static BloatedRelation from(final Relation relation)
    {
        return new BloatedRelation(relation.getIdentifier(), relation.getTags(), relation.bounds(),
                relation.members().asBean(),
                relation.allRelationsWithSameOsmIdentifier().stream().map(Relation::getIdentifier)
                        .collect(Collectors.toList()),
                relation.allKnownOsmMembers().asBean(), relation.osmRelationIdentifier(),
                relation.relations().stream().map(Relation::getIdentifier)
                        .collect(Collectors.toSet()));
    }

    public static BloatedRelation shallowFrom(final Relation relation)
    {
        return new BloatedRelation(relation.getIdentifier()).withInitialBounds(relation.bounds());
    }

    BloatedRelation(final long identifier)
    {
        this(identifier, null, null, null, null, null, null, null);
    }

    public BloatedRelation(final Long identifier, final Map<String, String> tags, // NOSONAR
            final Rectangle bounds, final RelationBean members,
            final List<Long> allRelationsWithSameOsmIdentifier,
            final RelationBean allKnownOsmMembers, final Long osmRelationIdentifier,
            final Set<Long> relationIdentifiers)
    {
        super(new BloatedAtlas());

        if (identifier == null)
        {
            throw new CoreException("Identifier can never be null.");
        }

        this.originalBounds = bounds != null ? bounds : null;
        this.aggregateBounds = this.originalBounds;

        this.identifier = identifier;
        this.tags = tags;
        this.members = members;
        this.allRelationsWithSameOsmIdentifier = allRelationsWithSameOsmIdentifier;
        this.allKnownOsmMembers = allKnownOsmMembers;
        this.osmRelationIdentifier = osmRelationIdentifier;
        this.relationIdentifiers = relationIdentifiers;
    }

    protected BloatedRelation(final Atlas atlas)
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
        return this.allRelationsWithSameOsmIdentifier == null ? null
                : this.allRelationsWithSameOsmIdentifier.stream().map(BloatedRelation::new)
                        .collect(Collectors.toList());
    }

    @Override
    public Rectangle bounds()
    {
        return this.aggregateBounds;
    }

    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof BloatedRelation)
        {
            final BloatedRelation that = (BloatedRelation) other;
            return BloatedEntity.basicEqual(this, that)
                    && BloatedEntity.equalThroughGet(this.members(), that.members(),
                            RelationMemberList::asBean)
                    && Objects.equals(this.allRelationsWithSameOsmIdentifier(),
                            that.allRelationsWithSameOsmIdentifier())
                    && BloatedEntity.equalThroughGet(this.allKnownOsmMembers(),
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
    public boolean isSuperShallow()
    {
        return this.members == null && this.allRelationsWithSameOsmIdentifier == null
                && this.allKnownOsmMembers == null && this.osmRelationIdentifier == null
                && this.tags == null && this.relationIdentifiers == null;
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
        return this.relationIdentifiers == null ? null
                : this.relationIdentifiers.stream().map(BloatedRelation::new)
                        .collect(Collectors.toSet());
    }

    @Override
    public String toString()
    {
        return "BloatedRelation [identifier=" + this.identifier + ", tags=" + this.tags
                + ", members=" + this.members + ", relationIdentifiers=" + this.relationIdentifiers
                + "]";
    }

    public BloatedRelation withAddedTag(final String key, final String value)
    {
        return withTags(BloatedEntity.addNewTag(getTags(), key, value));
    }

    public BloatedRelation withAggregateBoundsExtendedUsing(final Rectangle bounds)
    {
        if (this.aggregateBounds == null)
        {
            this.aggregateBounds = bounds;
        }
        this.aggregateBounds = Rectangle.forLocated(this.aggregateBounds, bounds);
        return this;
    }

    public BloatedRelation withAllKnownOsmMembers(final RelationBean allKnownOsmMembers)
    {
        this.allKnownOsmMembers = allKnownOsmMembers;
        return this;
    }

    public BloatedRelation withAllRelationsWithSameOsmIdentifier(
            final List<Long> allRelationsWithSameOsmIdentifier)
    {
        this.allRelationsWithSameOsmIdentifier = allRelationsWithSameOsmIdentifier;
        return this;
    }

    public BloatedRelation withExtraMember(final AtlasEntity newMember,
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

    public BloatedRelation withExtraMember(final AtlasEntity newMember, final String role)
    {
        this.members.addItem(
                new RelationBeanItem(newMember.getIdentifier(), role, newMember.getType()));
        this.updateBounds(newMember.bounds());
        return this;
    }

    public BloatedRelation withIdentifier(final long identifier)
    {
        this.identifier = identifier;
        return this;
    }

    /**
     * Assign this {@link BloatedRelation} with members.
     * <p>
     * In case this {@link BloatedRelation} is created from an existing relation, and the new member
     * list has had some existing members removed, use
     * {@link #withMembersAndSource(RelationBean, Relation, Rectangle)}
     *
     * @param members
     *            The members of the relation
     * @param bounds
     *            The bounds of all the members of the relation.
     * @return This
     */
    public BloatedRelation withMembers(final RelationBean members, final Rectangle bounds)
    {
        this.members = members;
        updateBounds(bounds);
        return this;
    }

    /**
     * Assign this {@link BloatedRelation} with members.
     * <p>
     * In case this {@link BloatedRelation} is created from an existing relation, and the new member
     * list has had some existing members removed, use
     * {@link #withMembersAndSource(RelationMemberList, Relation)}
     *
     * @param members
     *            The full members of the Relation
     * @return This
     */
    public BloatedRelation withMembers(final RelationMemberList members)
    {
        return withMembers(members.asBean(), members.bounds());
    }

    /**
     * @param members
     *            The members of the relation
     * @param source
     *            The relation that was used as a base to create that BloatedRelation, if any. Due
     *            to the weak nature of relation membership across Atlas(es), this helps decide what
     *            relation members are forcibly removed if any.
     * @param bounds
     *            The bounds of all the members of the relation.
     * @return This.
     */
    public BloatedRelation withMembersAndSource(final RelationBean members, final Relation source,
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
     *            The relation that was used as a base to create that BloatedRelation, if any. Due
     *            to the weak nature of relation membership across Atlas(es), this helps decide what
     *            relation members are forcibly removed if any.
     * @return This.
     */
    public BloatedRelation withMembersAndSource(final RelationMemberList members,
            final Relation source)
    {
        return withMembersAndSource(members.asBean(), source, members.bounds());
    }

    public BloatedRelation withOsmRelationIdentifier(final Long osmRelationIdentifier)
    {
        this.osmRelationIdentifier = osmRelationIdentifier;
        return this;
    }

    public BloatedRelation withRelationIdentifiers(final Set<Long> relationIdentifiers)
    {
        this.relationIdentifiers = relationIdentifiers;
        return this;
    }

    public BloatedRelation withRelations(final Set<Relation> relations)
    {
        this.relationIdentifiers = relations.stream().map(Relation::getIdentifier)
                .collect(Collectors.toSet());
        return this;
    }

    public BloatedRelation withRemovedTag(final String key)
    {
        return withTags(BloatedEntity.removeTag(getTags(), key));
    }

    public BloatedRelation withReplacedTag(final String oldKey, final String newKey,
            final String newValue)
    {
        return withRemovedTag(oldKey).withAddedTag(newKey, newValue);
    }

    public BloatedRelation withTags(final Map<String, String> tags)
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
        if (this.originalBounds == null)
        {
            this.originalBounds = bounds;
        }
        this.aggregateBounds = Rectangle.forLocated(this.originalBounds, bounds);
    }

    private BloatedRelation withInitialBounds(final Rectangle bounds)
    {
        this.originalBounds = bounds;
        this.aggregateBounds = bounds;
        return this;
    }
}
