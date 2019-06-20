package org.openstreetmap.atlas.utilities.checkstyle;

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
        INTERFACE,
        ENUM,
        CLASS,
        FIELD,
        INITIALIZER_BLOCK,
        METHOD,
        CONSTRUCTOR
    }

    /**
     * @author matthieun
     */
    private static class ObjectType implements Comparable<ObjectType>
    {
        private final Visibility visibility;
        private final Type type;
        private final boolean isStatic;
        private final String name;

        private ObjectType(final Visibility visibility, final Type type, final boolean isStatic,
                final String name)
        {
            this.visibility = visibility;
            this.type = type;
            this.isStatic = isStatic;
            this.name = name;
        }

        @Override
        public int compareTo(final ObjectType that)
        {
            final Type thisType = this.getType();
            final Type thatType = that.getType();
            if (thisType != thatType)
            {
                return thisType.compareTo(thatType);
            }

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

        public boolean isStatic()
        {
            return this.isStatic;
        }
    }

    @Override
    public int[] getAcceptableTokens()
    {
        return getDefaultTokens();
    }

    @Override
    public int[] getDefaultTokens()
    {
        return new int[] { TokenTypes.METHOD_DEF };
    }

    @Override
    public int[] getRequiredTokens()
    {
        return getDefaultTokens();
    }

    @Override
    public void visitToken(final DetailAST method)
    {
        final DetailAST modifier = method.

        final int methodDefs = body.findAll();

        if (methodDefs > this.max)
        {
            log(method.getLineNo(), message);
        }
    }
}
