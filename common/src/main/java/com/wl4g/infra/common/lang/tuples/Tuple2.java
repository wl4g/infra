/*
 * Copyright 2017 ~ 2025 the original author or authors. James Wong <jameswong1376@gmail.com>>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wl4g.infra.common.lang.tuples;

import java.util.Arrays;
import java.util.List;

/**
 * {@link Tuple2}
 * 
 * @author James Wong
 * @version 2023-01-21
 * @since v3.1.0
 */
public class Tuple2 implements Tuple {
    private static final long serialVersionUID = -6651344183217701756L;

    private Object item1;
    private Object item2;

    public Tuple2() {
    }

    public Tuple2(Object item1) {
        this(item1, null);
    }

    public Tuple2(Object item1, Object item2) {
        setItem1(item1);
        setItem2(item2);
    }

    @SuppressWarnings("unchecked")
    public <K> K getItem1() {
        return (K) item1;
    }

    public <K> Tuple2 setItem1(K item1) {
        this.item1 = item1;
        return this;
    }

    @SuppressWarnings("unchecked")
    public <V> V getItem2() {
        return (V) item2;
    }

    public <V> void setItem2(V item2) {
        this.item2 = item2;
    }

    public <V> Tuple2 withItem2(V item2) {
        setItem2(item2);
        return this;
    }

    @Override
    public Object nth(int index) {
        assertIndexInBounds(index);
        if (index == 0) {
            return item1;
        }
        return item2;
    }

    @Override
    public List<Object> asList() {
        return Arrays.asList(getItem1(), getItem2());
    }

    @Override
    public int size() {
        return 2;
    }

    protected void assertIndexInBounds(int index) {
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException("Cannot retrieve item at position " + index + ", size is " + size());
        }
    }

    @Override
    public String toString() {
        return "Tuple2 [item1=" + getItem1() + ", item2=" + getItem2() + "]";
    }

}