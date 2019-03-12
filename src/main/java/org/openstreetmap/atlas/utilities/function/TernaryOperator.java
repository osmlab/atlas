package org.openstreetmap.atlas.utilities.function;

/**
 * Represents an operation upon three operands of the same type, producing a result of the same type
 * as the operands. This is a specialization of {@link TernaryFunction} for the case where the
 * operands and the result are all of the same type.
 *
 * @author lcram
 */
@FunctionalInterface
public interface TernaryOperator<T> extends TernaryFunction<T, T, T, T>
{

}
