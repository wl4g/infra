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
package com.wl4g.infra.common.lang;

/**
 * {@link Tuple6}
 * 
 * @author James Wong
 * @version 2023-01-21
 * @since v3.1.0
 */
public class Tuple6 extends Tuple5 {
    private static final long serialVersionUID = -6651344183217701756L;

    private Object item6;

    public Tuple6() {
    }

    public Tuple6(Object item1, Object item2) {
        super(item1, item2);
    }

    public Tuple6(Object item1, Object item2, Object item3) {
        super(item1, item2, item3);
    }

    public Tuple6(Object item1, Object item2, Object item3, Object item4) {
        super(item1, item2, item3, item4);
    }

    public Tuple6(Object item1, Object item2, Object item3, Object item4, Object item5) {
        super(item1, item2, item3, item4, item5);
    }

    public Tuple6(Object item1, Object item2, Object item3, Object item4, Object item5, Object item6) {
        super(item1, item2, item3, item4, item5);
        setItem6(item6);
    }

    @SuppressWarnings("unchecked")
    public <V> V getItem6() {
        return (V) item6;
    }

    public <V> Tuple6 setItem6(V item6) {
        this.item6 = item6;
        return this;
    }

    @Override
    public String toString() {
        return "Tuple6 [item1=" + getItem1() + ", item2=" + getItem2() + ", item3=" + getItem3() + ", item4=" + getItem4()
                + ", item5=" + getItem5() + ", item6=" + getItem6() + "]";
    }

}