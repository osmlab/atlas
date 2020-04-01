package org.openstreetmap.atlas.utilities.function;

/**
 * Represents an operation upon six operands of the same type, producing a result of the same type
 * as the operands. This is a specialization of {@link SenaryFunction} for the case where the
 * operands and the result are all of the same type.
 *
 * @param <T>
 *            the type of the operands and result of the operator
 * @author lcram
 */
@FunctionalInterface
public interface SenaryOperator<T> extends SenaryFunction<T, T, T, T, T, T, T>
{

}
