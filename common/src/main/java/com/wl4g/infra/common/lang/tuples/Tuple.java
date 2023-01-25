package com.wl4g.infra.common.lang.tuples;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * {@link Tuple}
 * 
 * @author James Wong
 * @version 2023-01-25
 * @since v3.1.0
 * @see {@linkplain io.smallrye.reactive:mutiny:io.smallrye.mutiny.tuples.Tuple}
 */
public interface Tuple extends Iterable<Object>, Serializable {

    /**
     * Get the item stored at the given index.
     *
     * @param index
     *            The index of the item to retrieve.
     * @return The item, can be {@code null}
     * @throws IndexOutOfBoundsException
     *             if the index is greater than the size.
     */
    Object nth(int index);

    /**
     * Gets a {@link java.util.List} of {@link Object Objects} containing the
     * items composing this {@link Tuple}
     *
     * @return A list containing the item of the tuple.
     */
    List<Object> asList();

    /**
     * Gets an immutable {@link Iterator} traversing the content of this
     * {@link Tuple}.
     *
     * @return the iterator
     */
    @Override
    default Iterator<Object> iterator() {
        return Collections.unmodifiableList(asList()).iterator();
    }

    /**
     * @return the number of items stored in the {@link Tuple}
     */
    int size();
}
