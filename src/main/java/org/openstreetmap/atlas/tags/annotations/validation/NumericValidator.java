package org.openstreetmap.atlas.tags.annotations.validation;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Numeric validators can check ranges of valid data or even exclude certain values regardless of
 * the range
 *
 * @author cstaylor
 */
public abstract class NumericValidator extends ExactMatchValidator
{
    private static final Logger logger = LoggerFactory.getLogger(NumericValidator.class);

    private Long minimum;
    private Long maximum;
    private final Set<Double> exclusions;

    public NumericValidator()
    {
        this.exclusions = new HashSet<>();
    }

    public void excludeValue(final long value)
    {
        this.exclusions.add(new Double(value));
    }

    /**
     * Since all NumericValidators are ExactMatchValidators, we first check if the value matches any
     * exact match, and then proceed with the numeric conversion and range check
     */
    @Override
    public final boolean isValid(final String value)
    {
        try
        {
            return super.isValid(value) || withinRange(parse(value));
        }
        catch (final NumberFormatException oops)
        {
            return false;
        }
    }

    public void setMaximum(final long maximum)
    {
        if (this.minimum == null)
        {
            this.maximum = maximum;
        }
        else
        {
            if (maximum > this.minimum)
            {
                this.maximum = maximum;
            }
            else
            {
                logger.debug(
                        "Cannot set maximum less than or equal to minimum {}. Consider using setRange instead.",
                        this.minimum);
            }
        }
    }

    public void setMinimum(final long minimum)
    {
        if (this.maximum == null)
        {
            this.minimum = minimum;
        }
        else
        {
            if (minimum < this.maximum)
            {
                this.minimum = minimum;
            }
            else
            {
                logger.debug(
                        "Cannot set minimum greater than or equal to maximum {}. Consider using setRange instead.",
                        this.maximum);
            }
        }
    }

    public void setRange(final long minimum, final long maximum)
    {
        if (minimum < maximum)
        {
            this.minimum = minimum;
            this.maximum = maximum;
        }
        else if (minimum == Long.MAX_VALUE || maximum == Long.MIN_VALUE)
        {
            /*
             * Special case where at least one of the range endpoints is a default value. In this
             * case, we don't want to send out a log message - instead just do nothing.
             */
        }
        else
        {
            logger.debug("Invalid range supplied, minimum: {} cannot be greater than maximum: {}.",
                    minimum, maximum);
        }
    }

    protected boolean checkExclusions(final Number checkMe)
    {
        return this.exclusions.contains(checkMe.doubleValue());
    }

    protected abstract Number parse(String value);

    protected boolean withinRange(final Number checkMe)
    {
        if (checkExclusions(checkMe))
        {
            return false;
        }
        final double theValue = checkMe.doubleValue();
        if (this.minimum != null && theValue < this.minimum)
        {
            return false;
        }
        if (this.maximum != null && theValue > this.maximum)
        {
            return false;
        }
        return true;
    }
}
