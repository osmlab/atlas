package org.openstreetmap.atlas.utilities.checkstyle;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

import org.openstreetmap.atlas.exception.CoreException;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
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
        PRIVATE
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
    private static class ObjectType implements Comparable<ObjectType>
    {
        private static final ObjectTypeComparator OBJECT_TYPE_COMPARATOR = new ObjectTypeComparator();

        private final Visibility visibility;
        private final Type type;
        private final boolean isStatic;
        private final String name;

        ObjectType(final DetailAST object)
        {
            this.type = Type.forType(object.getType());
            final Optional<DetailAST> ident = findFirstToken(object, TokenTypes.IDENT);
            this.name = ident.isPresent() ? ident.get().getText() : "";
            final Optional<DetailAST> modifiers = findFirstToken(object, TokenTypes.MODIFIERS);
            if (modifiers.isPresent())
            {
                if (findFirstToken(modifiers.get(), TokenTypes.LITERAL_PRIVATE).isPresent())
                {
                    this.visibility = Visibility.PRIVATE;
                }
                else if (findFirstToken(modifiers.get(), TokenTypes.LITERAL_PROTECTED).isPresent())
                {
                    this.visibility = Visibility.PROTECTED;
                }
                else if (findFirstToken(modifiers.get(), TokenTypes.LITERAL_PUBLIC).isPresent())
                {
                    this.visibility = Visibility.PUBLIC;
                }
                else
                {
                    this.visibility = Visibility.PACKAGE_PRIVATE;
                }
                this.isStatic = findFirstToken(modifiers.get(), TokenTypes.LITERAL_STATIC)
                        .isPresent();
            }
            else
            {
                this.visibility = Visibility.PACKAGE_PRIVATE;
                this.isStatic = false;
            }
        }

        @Override
        public int compareTo(final ObjectType that)
        {
            return OBJECT_TYPE_COMPARATOR.compare(this, that);
        }

        @Override
        public boolean equals(final Object other)
        {
            if (this == other)
                return true;
            if (other == null || getClass() != other.getClass())
                return false;
            final ObjectType that = (ObjectType) other;
            return isStatic() == that.isStatic() && getVisibility() == that.getVisibility()
                    && getType() == that.getType() && getName().equals(that.getName());
        }

        public String getName()
        {
            return this.name;
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
            return Objects.hash(getVisibility(), getType(), isStatic(), getName());
        }

        public boolean isStatic()
        {
            return this.isStatic;
        }

        @Override
        public String toString()
        {
            return "ObjectType{" + "visibility=" + this.visibility + ", type=" + this.type + ", isStatic="
                    + this.isStatic + ", name='" + this.name + '\'' + '}';
        }
    }

    /**
     * @author matthieun
     */
    private static class ObjectTypeComparator implements Comparator<ObjectType>
    {
        ObjectTypeComparator()
        {
        }

        @Override
        public int compare(final ObjectType left, final ObjectType right)
        {
            return -1;
        }
    }

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
                TokenTypes.VARIABLE_DEF, TokenTypes.CTOR_DEF, TokenTypes.INTERFACE_DEF };
    }

    @Override
    public int[] getRequiredTokens()
    {
        return getDefaultTokens();
    }

    @Override
    public void visitToken(final DetailAST object)
    {
        final DetailAST nextSibling = object.getNextSibling();
        final ObjectType left = new ObjectType(object);
        final ObjectType right = new ObjectType(nextSibling);

        if (left.compareTo(right) < 0)
        {
            log(nextSibling.getLineNo(), "Invalid method order.");
        }
    }
}
