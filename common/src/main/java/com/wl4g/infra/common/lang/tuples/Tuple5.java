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
 * {@link Tuple5}
 * 
 * @author James Wong
 * @version 2023-01-21
 * @since v3.1.0
 */
public class Tuple5 extends Tuple4 {
    private static final long serialVersionUID = -6651344183217701759L;

    private Object item5;

    public Tuple5() {
    }

    public Tuple5(Object item1, Object item2) {
        super(item1, item2);
    }

    public Tuple5(Object item1, Object item2, Object item3) {
        super(item1, item2, item3);
    }

    public Tuple5(Object item1, Object item2, Object item3, Object item4) {
        super(item1, item2, item3, item4);
    }

    public Tuple5(Object item1, Object item2, Object item3, Object item4, Object item5) {
        super(item1, item2, item3, item4);
        setItem5(item5);
    }

    @SuppressWarnings("unchecked")
    public <V> V getItem5() {
        return (V) item5;
    }

    public <V> void setItem5(V item5) {
        this.item5 = item5;
    }

    public <V> Tuple5 withItem5(V item5) {
        setItem5(item5);
        return this;
    }

    @Override
    public Object nth(int index) {
        assertIndexInBounds(index);
        if (index == 4) {
            return item5;
        } else {
            return super.nth(index);
        }
    }

    @Override
    public List<Object> asList() {
        return Arrays.asList(getItem1(), getItem2(), getItem3(), getItem4(), getItem5());
    }

    @Override
    public int size() {
        return 5;
    }

    @Override
    public String toString() {
        return "Tuple5 [item1=" + getItem1() + ", item2=" + getItem2() + ", item3=" + getItem3() + ", item4=" + getItem4()
                + ", item5=" + getItem5() + "]";
    }

}