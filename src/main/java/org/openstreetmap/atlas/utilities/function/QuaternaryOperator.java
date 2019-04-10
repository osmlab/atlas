package org.openstreetmap.atlas.utilities.function;

/**
 * Represents an operation upon four operands of the same type, producing a result of the same type
 * as the operands. This is a specialization of {@link QuaternaryFunction} for the case where the
 * operands and the result are all of the same type.
 *
 * @param <T>
 *            the type of the operands and result of the operator
 * @author lcram
 */
@FunctionalInterface
public interface QuaternaryOperator<T> extends QuaternaryFunction<T, T, T, T>
{
}
