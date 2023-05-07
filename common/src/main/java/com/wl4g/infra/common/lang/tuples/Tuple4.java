/*
 * Copyright 2017 ~ 2025 the original author or authors. James Wong <jameswong1376@gmail.com>
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
 * {@link Tuple4}
 * 
 * @author James Wong
 * @version 2023-01-21
 * @since v3.1.0
 */
public class Tuple4 extends Tuple3 {
    private static final long serialVersionUID = -6651344183217701758L;

    private Object item4;

    public Tuple4() {
    }

    public Tuple4(Object item1) {
        this(item1, null);
    }

    public Tuple4(Object item1, Object item2) {
        super(item1, item2);
    }

    public Tuple4(Object item1, Object item2, Object item3) {
        super(item1, item2, item3);
    }

    public Tuple4(Object item1, Object item2, Object item3, Object item4) {
        super(item1, item2, item3);
        setItem4(item4);
    }

    @SuppressWarnings("unchecked")
    public <V> V getItem4() {
        return (V) item4;
    }

    public <V> void setItem4(V item4) {
        this.item4 = item4;
    }

    public <V> Tuple4 withItem4(V item4) {
        setItem4(item4);
        return this;
    }

    @Override
    public Object nth(int index) {
        assertIndexInBounds(index);
        if (index == 3) {
            return item4;
        } else {
            return super.nth(index);
        }
    }

    @Override
    public List<Object> asList() {
        return Arrays.asList(getItem1(), getItem2(), getItem3(), getItem4());
    }

    @Override
    public int size() {
        return 4;
    }

    @Override
    public String toString() {
        return "Tuple4 [item1=" + getItem1() + ", item2=" + getItem2() + ", item3=" + getItem3() + ", item4=" + getItem4() + "]";
    }

}