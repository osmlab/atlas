package org.openstreetmap.atlas.utilities.collections;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.change.description.ChangeDescriptorType;

/**
 * A collection of utilities that implements a simple diff compute/apply system for {@link List}s of
 * any type.
 * 
 * @author lcram
 */
public final class ListDiff
{
    /**
     * @author lcram
     * @param <T>
     *            the type of the element with which we are acting
     */
    public static class Action<T>
    {
        static final int END_INDEX = -1;

        private final ChangeDescriptorType descriptorType;
        private final int index;
        private final T beforeElement;
        private final T afterElement;

        Action(final ChangeDescriptorType type, final int index, final T beforeElement,
                final T afterElement)
        {
            this.descriptorType = type;
            this.index = index;
            this.beforeElement = beforeElement;
            this.afterElement = afterElement;
        }

        @Override
        public boolean equals(final Object object)
        {
            if (this == object)
            {
                return true;
            }
            if (object == null || getClass() != object.getClass())
            {
                return false;
            }
            final Action<?> action = (Action<?>) object;
            return this.index == action.index && this.descriptorType == action.descriptorType
                    && Objects.equals(this.beforeElement, action.beforeElement)
                    && Objects.equals(this.afterElement, action.afterElement);
        }

        public T getAfterElement()
        {
            return this.afterElement;
        }

        public T getBeforeElement()
        {
            return this.beforeElement;
        }

        public ChangeDescriptorType getDescriptorType()
        {
            return this.descriptorType;
        }

        public int getIndex()
        {
            return this.index;
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(this.descriptorType, this.index, this.beforeElement,
                    this.afterElement);
        }

        @Override
        public String toString()
        {
            final String changeString;
            if (this.beforeElement != null && this.afterElement != null)
            {
                changeString = this.beforeElement + " => " + this.afterElement;
            }
            else if (this.beforeElement != null)
            {
                changeString = this.beforeElement + " => *";
            }
            else
            {
                changeString = "* => " + this.afterElement;
            }
            if (this.index == END_INDEX)
            {
                return "ACTION(" + this.descriptorType + ", " + changeString + ")";
            }
            return "ACTION(" + this.descriptorType + ", " + this.index + ", " + changeString + ")";
        }
    }

    /**
     * @author lcram
     * @param <T>
     *            the type of the element with which we are acting
     */
    public static class Diff<T>
    {
        private final List<Action<T>> actions;

        /*
         * We save a list hash computed from the beforeList. A Diff object is essentially tied to
         * the List from which it was computed. If we try to apply a Diff object to a different
         * list, we should be able to detect this and fail.
         */
        private final int listHash;

        Diff(final List<T> beforeList)
        {
            this.actions = new ArrayList<>();
            this.listHash = beforeList.hashCode();
        }

        @Override
        public boolean equals(final Object object)
        {
            if (this == object)
            {
                return true;
            }
            if (object == null || getClass() != object.getClass())
            {
                return false;
            }
            final Diff<?> diff = (Diff<?>) object;
            return this.listHash == diff.listHash && Objects.equals(this.actions, diff.actions);
        }

        public Action getAction(final int index)
        {
            return this.actions.get(index);
        }

        public List<Action<T>> getActions()
        {
            return new ArrayList<>(this.actions);
        }

        public int getListHash()
        {
            return this.listHash;
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(this.actions, this.listHash);
        }

        public boolean isEmpty()
        {
            return this.actions.isEmpty();
        }

        @Override
        public String toString()
        {
            final StringBuilder builder = new StringBuilder();
            builder.append("DIFF[");
            for (int i = 0; i < this.actions.size() - 1; i++)
            {
                builder.append(this.actions.get(i).toString());
                builder.append(", ");
            }
            builder.append(this.actions.get(this.actions.size() - 1).toString());
            builder.append("]");
            return builder.toString();
        }

        void addAction(final Action action)
        {
            this.actions.add(action);
        }
    }

    /**
     * Transform a given {@link List} using some given {@link Diff}.
     * 
     * @param diff
     *            the {@link Diff} to transform the {@link List}
     * @param targetList
     *            the {@link List} on which to apply the {@link Diff}
     * @param <T>
     *            the type of elements in the given {@link List}
     * @return a new {@link List} based on the provided {@link Diff}
     */
    public static <T> List<T> apply(final Diff<T> diff, final List<T> targetList)
    {
        final int targetHash = targetList.hashCode();
        if (diff.getListHash() != targetHash)
        {
            throw new CoreException(
                    "Failed to apply diff. Diffs can only be applied to the list from which they are generated.");
        }

        final List<T> newList = new ArrayList<>(targetList);

        for (final Action<T> action : diff.getActions())
        {
            if (action.getDescriptorType() == ChangeDescriptorType.UPDATE)
            {
                newList.remove(action.getIndex());
                newList.add(action.getIndex(), action.getAfterElement());
            }
            if (action.getDescriptorType() == ChangeDescriptorType.ADD)
            {
                newList.add(action.getAfterElement());
            }
            if (action.getDescriptorType() == ChangeDescriptorType.REMOVE)
            {
                newList.remove(newList.size() - 1);
            }
        }

        return newList;
    }

    /**
     * Given two {@link List}s, compute a {@link Diff} object that can be used to transform the
     * before {@link List} to the after {@link List}.
     *
     * @param beforeList
     *            the before {@link List}
     * @param afterList
     *            the after {@link List}
     * @param <T>
     *            the type of elements in the given {@link List}s
     * @return a {@link Diff} object representing the change from before to after
     */
    public static <T> Diff<T> diff(final List<T> beforeList, final List<T> afterList) // NOSONAR
    {
        if (beforeList == null)
        {
            throw new CoreException("beforeList cannot be null");
        }
        if (afterList == null)
        {
            throw new CoreException("afterList cannot be null");
        }

        final Diff<T> diff = new Diff<>(beforeList);

        /*
         * When both lists are equal (or empty), we can just do nothing.
         */
        if (Objects.equals(beforeList, afterList))
        {
            return diff;
        }

        /*
         * If the beforeList is empty, we have all ADDs.
         */
        if (beforeList.isEmpty())
        {
            for (final T element : afterList)
            {
                diff.addAction(
                        new Action<>(ChangeDescriptorType.ADD, Action.END_INDEX, null, element));
            }
        }

        /*
         * If the afterList is empty, we have all REMOVEs.
         */
        if (afterList.isEmpty())
        {
            for (final T element : beforeList)
            {
                diff.addAction(
                        new Action<>(ChangeDescriptorType.REMOVE, Action.END_INDEX, element, null));
            }
        }

        int beforeIndex = 0;
        int afterIndex = 0;

        /*
         * Now that we have handled the case where one or both of the lists are empty, there are 3
         * possible configurations for the before/after lists.
         */
        // 1) beforeList is longer than afterList
        // bef: A B C D E
        // aft: A Z C
        //
        // 2) afterList is longer than beforeList
        // bef: A B C
        // aft: A B C D E
        //
        // 3) beforeList and afterList are the same length
        // bef: A B C Y
        // aft: A Z C D
        /*
         * In case 1), we should have an UPDATE on index 1 from B -> Z, and then we should have two
         * REMOVEs for D and E. In case 2), we should have two ADDs for D and E. In case 3), we
         * should have an UPDATE on index 1 from B -> Z and an UPDATE on index 3 from Y -> D.
         */
        while (true)
        {
            final T beforeElement;
            final T afterElement;

            if (beforeIndex >= beforeList.size() && afterIndex >= afterList.size())
            {
                break;
            }

            if (beforeIndex < beforeList.size())
            {
                beforeElement = beforeList.get(beforeIndex);
            }
            else
            {
                beforeElement = null;
            }
            if (afterIndex < afterList.size())
            {
                afterElement = afterList.get(afterIndex);
            }
            else
            {
                afterElement = null;
            }

            /*
             * This block handles case 3) above, where we are in the middle of the lists and we see
             * a difference. So we need to generate UPDATEs to change the middle elements.
             */
            if (beforeElement != null && afterElement != null)
            {
                if (!beforeElement.equals(afterElement))
                {
                    diff.addAction(new Action<>(ChangeDescriptorType.UPDATE, beforeIndex,
                            beforeElement, afterElement));
                }
            }
            /*
             * This block handles case 1) above, where we have reached the end of the afterList. So
             * we need to generate REMOVEs.
             */
            else if (beforeElement != null)
            {
                diff.addAction(new Action<>(ChangeDescriptorType.REMOVE, Action.END_INDEX,
                        beforeElement, null));
            }
            /*
             * This block handles case 2) above, where we have reached the end of the beforeList. So
             * we need to generate ADDs.
             */
            else if (afterElement != null)
            {
                diff.addAction(new Action<>(ChangeDescriptorType.ADD, Action.END_INDEX, null,
                        afterElement));
            }

            beforeIndex++;
            afterIndex++;
        }

        return diff;
    }

    private ListDiff()
    {

    }
}
