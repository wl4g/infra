/*
 * Copyright 2017 ~ 2025 the original authors James Wong.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ALL_OR KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wl4g.infra.common.expression.aviator;

import com.wl4g.infra.common.serialize.JacksonUtils;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.util.Arrays;

import static com.wl4g.infra.common.serialize.JacksonUtils.parseToNode;
import static com.wl4g.infra.common.serialize.JacksonUtils.toJSONString;

/**
 * The {@link ExpressionOperatorTests}
 *
 * @author James Wong
 * @since v3.0
 **/
public class ExpressionOperatorTests {

    @Test
    public void testSimpleRelationOperator() {
        ExpressionOperator.RelationOperator condition = new ExpressionOperator.RelationOperator();
        condition.setName("testCondition");
        condition.setType(ExpressionOperator.OperatorType.RELATION.name());
        condition.setFn(new AviatorFunction("a >= 1 && b <= 2"));
        Assertions.assertTrue(condition.apply(parseToNode("{\"a\":1,\"b\":2}")));
    }

    @Test
    public void testComplexLogicalOperator() {
        ExpressionOperator.RelationOperator condition1 = new ExpressionOperator.RelationOperator();
        condition1.setName("testCondition1");
        condition1.setType(ExpressionOperator.OperatorType.RELATION.name());
        condition1.setFn(new AviatorFunction("a >= 1 && b <= 2"));

        ExpressionOperator.RelationOperator condition2 = new ExpressionOperator.RelationOperator();
        condition2.setName("testCondition2");
        condition2.setType(ExpressionOperator.OperatorType.RELATION.name());
        condition2.setFn(new AviatorFunction("u.age >= 25 && u.wealth.money >= 500000"));

        ExpressionOperator.LogicalOperator condition3 = new ExpressionOperator.LogicalOperator();
        condition3.setName("testCondition3");
        condition3.setType(ExpressionOperator.OperatorType.RELATION.name());
        condition3.setLogical(ExpressionOperator.LogicalType.AND);
        condition3.setSubConditions(Arrays.asList(condition1, condition2));

        ExpressionOperator.RelationOperator condition4 = new ExpressionOperator.RelationOperator();
        condition4.setName("testCondition4");
        condition4.setType(ExpressionOperator.OperatorType.RELATION.name());
        condition4.setFn(new AviatorFunction("u.country == 'US' || u.country == 'CN'"));

        ExpressionOperator.LogicalOperator condition5 = new ExpressionOperator.LogicalOperator();
        condition5.setName("testCondition5");
        condition5.setType(ExpressionOperator.OperatorType.RELATION.name());
        condition5.setLogical(ExpressionOperator.LogicalType.OR);
        condition5.setSubConditions(Arrays.asList(condition3, condition4));

        // ------------------------------------------------------------------------------------
        //            testCondition5(OR)
        //            /                \
        //     testCondition3(AND)      testCondition4
        //    /                 \                    \
        // testCondition1        testCondition2       u.country == 'US' || u.country == 'CN'
        //        |                   |
        //   a >= 1 && b <= 2    u.age >= 25 && u.wealth.money >= 500000
        // ------------------------------------------------------------------------------------
        System.out.println("condition5Json: " + toJSONString(condition5));
        // OUTPUT OPERATOR:
        // {"type":"LOGICAL","name":"testCondition5","logical":"OR","subConditions":[{"type":"LOGICAL","name":"testCondition3","logical":"AND",
        // "subConditions":[{"type":"RELATION","name":"testCondition1","fn":{"expression":"a >= 1 && b <= 2"}},{"type":"RELATION","name":"testCondition2",
        // "fn":{"expression":"u.age >= 25 && u.wealth.money >= 500000"}}]},{"type":"RELATION","name":"testCondition4","fn":{"expression":"u.country == 'US' || u.country == 'CN'"}}]}

        condition5.setLogical(ExpressionOperator.LogicalType.AND);
        Assertions.assertTrue(condition5.apply(parseToNode("{\"a\":1,\"b\":2,\"u\":{\"wealth\":{\"money\":500000},\"age\":25,\"country\":\"US\"}}")));

        condition5.setLogical(ExpressionOperator.LogicalType.OR);
        Assertions.assertTrue(condition5.apply(parseToNode("{\"a\":1,\"b\":2,\"u\":{\"wealth\":{\"money\":499999},\"age\":25,\"country\":\"CN\"}}")));

        condition5.setLogical(ExpressionOperator.LogicalType.AND);
        Assertions.assertFalse(condition5.apply(parseToNode("{\"a\":1,\"b\":2,\"u\":{\"wealth\":{\"money\":499999},\"age\":25,\"country\":\"JP\"}}")));
    }

    @Test
    public void testSerialization() {
        ExpressionOperator.RelationOperator condition1 = new ExpressionOperator.RelationOperator();
        condition1.setName("testCondition1");
        condition1.setType(ExpressionOperator.OperatorType.RELATION.name());
        condition1.setFn(new AviatorFunction("a >= 1 && b <= 2"));

        ExpressionOperator.RelationOperator condition2 = new ExpressionOperator.RelationOperator();
        condition2.setName("testCondition2");
        condition2.setType(ExpressionOperator.OperatorType.RELATION.name());
        condition2.setFn(new AviatorFunction("a >= 5"));

        ExpressionOperator.LogicalOperator condition3 = new ExpressionOperator.LogicalOperator();
        condition3.setName("testCondition3");
        condition3.setType(ExpressionOperator.OperatorType.RELATION.name());
        condition3.setSubConditions(Arrays.asList(condition1, condition2));

        condition3.setLogical(ExpressionOperator.LogicalType.AND);

        String condition3Json = JacksonUtils.toJSONString(condition3);
        //System.out.println("condition3Json: " + condition3Json);

        ExpressionOperator expectOfCondition3 = JacksonUtils.parseJSON(condition3Json, ExpressionOperator.class);
        Assertions.assertInstanceOf(ExpressionOperator.LogicalOperator.class, expectOfCondition3);
        System.out.println("expectOfCondition3: " + expectOfCondition3);
    }

}
