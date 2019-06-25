package org.openstreetmap.atlas.utilities.checkstyle;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.utilities.collections.StringList;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * Check for specified ordering of elements in a source file.
 * 
 * @author matthieun
 */
public class ArrangementCheck extends AbstractCheck
{
    /**
     * @author matthieun
     */
    private enum Visibility
    {
        PUBLIC,
        PROTECTED,
        PACKAGE_PRIVATE,
        PRIVATE;

        public static Visibility forName(final String name)
        {
            for (final Visibility visibility : Visibility.values())
            {
                if (visibility.name().equalsIgnoreCase(name))
                {
                    return visibility;
                }
            }
            if (name.isEmpty())
            {
                return Visibility.PACKAGE_PRIVATE;
            }
            throw new CoreException("Invalid name \"{}\"", name);
        }
    }

    /**
     * @author matthieun
     */
    private enum Type
    {
        INTERFACE(TokenTypes.INTERFACE_DEF),
        ENUM(TokenTypes.ENUM_DEF),
        CLASS(TokenTypes.CLASS_DEF),
        FIELD(TokenTypes.VARIABLE_DEF),
        STATIC_INITIALIZER_BLOCK(TokenTypes.STATIC_INIT),
        INITIALIZER_BLOCK(TokenTypes.INSTANCE_INIT),
        METHOD(TokenTypes.METHOD_DEF),
        CONSTRUCTOR(TokenTypes.CTOR_DEF);

        private final int tokenType;

        public static Type forName(final String name)
        {
            for (final Type type : Type.values())
            {
                if (type.name().equalsIgnoreCase(name))
                {
                    return type;
                }
            }
            throw new CoreException("Invalid name {}", name);
        }

        public static Type forType(final int tokenType)
        {
            for (final Type type : Type.values())
            {
                if (type.tokenType == tokenType)
                {
                    return type;
                }
            }
            throw new CoreException("Invalid token type {}", tokenType);
        }

        Type(final int tokenType)
        {
            this.tokenType = tokenType;
        }
    }

    /**
     * @author matthieun
     */
    private class ObjectRawType
    {
        private final Visibility visibility;
        private final Type type;
        private final boolean isStatic;

        ObjectRawType(final Visibility visibility, final Type type, final boolean isStatic)
        {
            this.visibility = visibility;
            this.type = type;
            this.isStatic = isStatic;
        }

        @Override
        public boolean equals(final Object other)
        {
            if (this == other)
            {
                return true;
            }
            if (other == null || getClass() != other.getClass())
            {
                return false;
            }
            final ObjectRawType that = (ObjectRawType) other;
            return isStatic() == that.isStatic() && getVisibility() == that.getVisibility()
                    && getType() == that.getType();
        }

        public Type getType()
        {
            return this.type;
        }

        public Visibility getVisibility()
        {
            return this.visibility;
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(getVisibility(), getType(), isStatic());
        }

        public boolean isStatic()
        {
            return this.isStatic;
        }
    }

    /**
     * @author matthieun
     */
    private class ObjectType implements Comparable<ObjectType>
    {
        private final ObjectRawType objectRawType;
        private final String name;

        ObjectType(final DetailAST object)
        {
            final Type type = Type.forType(object.getType());
            final Visibility visibility;
            final boolean isStatic;
            final Optional<DetailAST> ident = findFirstToken(object, TokenTypes.IDENT);
            if (ident.isPresent())
            {
                this.name = ident.get().getText();
            }
            else if (Type.INITIALIZER_BLOCK == type || Type.STATIC_INITIALIZER_BLOCK == type)
            {
                this.name = type.name().toLowerCase();
            }
            else
            {
                this.name = "";
            }
            final Optional<DetailAST> modifiers = findFirstToken(object, TokenTypes.MODIFIERS);
            if (modifiers.isPresent())
            {
                if (findFirstToken(modifiers.get(), TokenTypes.LITERAL_PRIVATE).isPresent())
                {
                    visibility = Visibility.PRIVATE;
                }
                else if (findFirstToken(modifiers.get(), TokenTypes.LITERAL_PROTECTED).isPresent())
                {
                    visibility = Visibility.PROTECTED;
                }
                else if (findFirstToken(modifiers.get(), TokenTypes.LITERAL_PUBLIC).isPresent())
                {
                    visibility = Visibility.PUBLIC;
                }
                else
                {
                    visibility = Visibility.PACKAGE_PRIVATE;
                }
                isStatic = findFirstToken(modifiers.get(), TokenTypes.LITERAL_STATIC).isPresent();
            }
            else
            {
                visibility = Visibility.PACKAGE_PRIVATE;
                isStatic = false;
            }
            this.objectRawType = new ObjectRawType(visibility, type, isStatic);
        }

        @Override
        public int compareTo(final ObjectType that)
        {
            return ArrangementCheck.this.getObjectTypeComparator().compare(this, that);
        }

        @Override
        public boolean equals(final Object other)
        {
            if (this == other)
            {
                return true;
            }
            if (other == null || getClass() != other.getClass())
            {
                return false;
            }
            final ObjectType that = (ObjectType) other;
            return getObjectRawType().equals(that.getObjectRawType())
                    && getName().equals(that.getName());
        }

        public String getName()
        {
            return this.name;
        }

        public ObjectRawType getObjectRawType()
        {
            return this.objectRawType;
        }

        public Type getType()
        {
            return this.objectRawType.getType();
        }

        public Visibility getVisibility()
        {
            return this.objectRawType.getVisibility();
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(getVisibility(), getType(), isStatic(), getName());
        }

        public boolean isStatic()
        {
            return this.objectRawType.isStatic();
        }

        @Override
        public String toString()
        {
            return "ObjectType{" + "visibility=" + this.getVisibility() + ", type=" + this.getType()
                    + ", isStatic=" + this.isStatic() + ", name='" + this.name + '\'' + '}';
        }
    }

    /**
     * @author matthieun
     */
    private class ObjectTypeComparator implements Comparator<ObjectType>
    {
        private static final int ARRANGEMENT_LINE_SIZE = 3;
        private static final String ARRANGEMENT_FILE = "arrangement.txt";
        private final Map<ObjectRawType, Integer> rawTypeToOrderIndex;

        ObjectTypeComparator()
        {
            this(new InputStreamResource(
                    () -> ArrangementCheck.class.getResourceAsStream(ARRANGEMENT_FILE)));
        }

        ObjectTypeComparator(final Resource ordering)
        {
            int index = 0;
            try
            {
                this.rawTypeToOrderIndex = new HashMap<>();
                for (final String line : ordering.lines())
                {
                    final StringList split = StringList.split(line, ",");
                    if (line.isEmpty() || line.startsWith("#"))
                    {
                        index++;
                        continue;
                    }
                    if (split.size() != ARRANGEMENT_LINE_SIZE)
                    {
                        throw new CoreException("Malformed line: \"{}\"", line);
                    }
                    final Type type = Type.forName(split.get(0));
                    final Visibility visibility = Visibility.forName(split.get(1));
                    final boolean isStatic = "static".equalsIgnoreCase(split.get(2));
                    this.rawTypeToOrderIndex.put(new ObjectRawType(visibility, type, isStatic),
                            index);
                    index++;
                }
            }
            catch (final Exception e)
            {
                throw new CoreException(
                        "Unable to parse file defining arrangement (was at line {}): {}", index + 1,
                        ArrangementCheck.class.getResource(ARRANGEMENT_FILE).getPath(), e);
            }
        }

        @Override
        public int compare(final ObjectType left, final ObjectType right)
        {
            final int difference = this.rawTypeToOrderIndex.get(left.getObjectRawType())
                    - this.rawTypeToOrderIndex.get(right.getObjectRawType());
            if (difference == 0 && Type.FIELD != left.getObjectRawType().getType())
            {
                final String leftName = left.getName();
                final String rightName = right.getName();
                return leftName.compareTo(rightName);
            }
            else
            {
                return difference;
            }
        }

        public boolean isComparable(final ObjectType object)
        {
            return this.rawTypeToOrderIndex.containsKey(object.getObjectRawType())
                    && !"serialVersionUID".equals(object.getName());
        }
    }

    private ObjectTypeComparator objectTypeComparator;
    private String arrangementDefinition = "";

    public static Optional<DetailAST> findFirstToken(final DetailAST source, final int tokenType)
    {
        return Optional.ofNullable(source.findFirstToken(tokenType));
    }

    @Override
    public int[] getAcceptableTokens()
    {
        return getDefaultTokens();
    }

    @Override
    public int[] getDefaultTokens()
    {
        return new int[] { TokenTypes.CLASS_DEF, TokenTypes.ENUM_DEF, TokenTypes.METHOD_DEF,
                TokenTypes.VARIABLE_DEF, TokenTypes.CTOR_DEF, TokenTypes.INTERFACE_DEF,
                TokenTypes.STATIC_INIT, TokenTypes.INSTANCE_INIT };
    }

    @Override
    public int[] getRequiredTokens()
    {
        return getDefaultTokens();
    }

    public void setArrangementDefinition(final String arrangementDefinition)
    {
        this.arrangementDefinition = arrangementDefinition;
    }

    @Override
    public void visitToken(final DetailAST object)
    {
        final ObjectType left = new ObjectType(object);
        if (!acceptedTokens().contains(object.getType())
                || !this.getObjectTypeComparator().isComparable(left))
        {
            return;
        }
        Optional<DetailAST> nextSibling = Optional.ofNullable(object.getNextSibling());
        while (nextSibling.isPresent() && (!acceptedTokens().contains(nextSibling.get().getType())
                || !this.getObjectTypeComparator().isComparable(new ObjectType(nextSibling.get()))))
        {
            nextSibling = Optional.ofNullable(nextSibling.get().getNextSibling());
        }
        if (nextSibling.isPresent())
        {
            final DetailAST nextSiblingGet = nextSibling.get();
            final ObjectType right = new ObjectType(nextSiblingGet);

            if (left.compareTo(right) > 0)
            {
                String moreInfo = "";
                if (!left.getName().isEmpty())
                {
                    moreInfo = moreInfo + ", " + left.getName();
                }
                if (!right.getName().isEmpty())
                {
                    moreInfo = moreInfo + ", " + right.getName();
                }
                log(nextSiblingGet.getLineNo(), "Invalid order" + moreInfo);
            }
        }
    }

    private Set<Integer> acceptedTokens()
    {
        final HashSet<Integer> result = new HashSet<>();
        for (final int value : getAcceptableTokens())
        {
            result.add(value);
        }
        return result;
    }

    private ObjectTypeComparator getObjectTypeComparator()
    {
        if (this.objectTypeComparator == null)
        {
            if (this.arrangementDefinition.isEmpty())
            {
                this.objectTypeComparator = new ObjectTypeComparator();
            }
            else if (this.arrangementDefinition.startsWith("/"))
            {
                this.objectTypeComparator = new ObjectTypeComparator(
                        new File(this.arrangementDefinition));
            }
            else
            {
                throw new CoreException("Invalid configuration for ArrangementCheck: {}",
                        this.arrangementDefinition);
            }
        }
        return this.objectTypeComparator;
    }
}
