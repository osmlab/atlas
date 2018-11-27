package org.openstreetmap.atlas.geography.atlas.bloated;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedAtlas.BloatedEntity;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean.RelationBeanItem;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.geography.atlas.items.RelationMemberList;

/**
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
     * changed bounds, if preset. Each time with(Located) is called on this entity, it is recomputed
     * from the original bounds and the new Located bounds.
     */
    private Rectangle aggregateBounds;

    private long identifier;
    private Map<String, String> tags;
    private RelationBean members;
    private List<Long> allRelationsWithSameOsmIdentifier;
    private RelationBean allKnownOsmMembers;
    private Long osmRelationIdentifier;
    private Set<Long> relationIdentifiers;

    public static BloatedRelation fromRelation(final Relation relation)
    {
        return new BloatedRelation(relation.getIdentifier(), relation.getTags(), relation.bounds(),
                relation.members().asBean(),
                relation.allRelationsWithSameOsmIdentifier().stream().map(Relation::getIdentifier)
                        .collect(Collectors.toList()),
                relation.allKnownOsmMembers().asBean(), relation.osmRelationIdentifier(),
                relation.relations().stream().map(Relation::getIdentifier)
                        .collect(Collectors.toSet()));
    }

    public static BloatedRelation shallowFromRelation(final Relation relation)
    {
        return new BloatedRelation(relation.getIdentifier(), relation.bounds());
    }

    BloatedRelation(final long identifier)
    {
        this(identifier, null, null, null, null, null, null, null);
    }

    BloatedRelation(final long identifier, final Rectangle bounds)
    {
        this(identifier, null, bounds, null, null, null, null, null);
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
        this.aggregateBounds = bounds != null ? bounds : null;

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
        return BloatedAtlas.equals(this, other);
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
    public RelationMemberList members()
    {
        return membersFor(this.members);
    }

    @Override
    public long osmRelationIdentifier()
    {
        return this.osmRelationIdentifier;
    }

    @Override
    public Set<Relation> relations()
    {
        return this.relationIdentifiers == null ? null : this.relationIdentifiers.stream()
                .map(BloatedRelation::new).collect(Collectors.toSet());
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

    public BloatedRelation withIdentifier(final long identifier)
    {
        this.identifier = identifier;
        return this;
    }

    public BloatedRelation withMembers(final RelationBean members, final Rectangle bounds)
    {
        this.members = members;
        // TODO note to reviewer, is this the right approach?
        this.aggregateBounds = bounds;
        return this;
    }

    /**
     * Here the members have to be full so the new bounds can be computed from them.
     *
     * @param members
     *            The full members of the Relation
     * @return This.
     */
    public BloatedRelation withMembers(final RelationMemberList members)
    {
        this.members = members.asBean();
        if (this.originalBounds == null)
        {
            this.originalBounds = members.bounds();
        }
        this.aggregateBounds = Rectangle.forLocated(this.originalBounds, members.bounds());
        return this;
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
                    BloatedAtlas.bloatedEntityFor(item.getIdentifier(), item.getType()),
                    getIdentifier()));
        }
        return new RelationMemberList(memberList);
    }
}
