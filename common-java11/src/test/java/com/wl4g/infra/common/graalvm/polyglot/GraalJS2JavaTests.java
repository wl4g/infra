package com.wl4g.infra.common.graalvm.polyglot;

import static com.wl4g.infra.common.serialize.JacksonUtils.toJSONString;
import static java.util.Collections.singletonMap;

import java.io.File;
import java.util.Map;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyObject;
import org.junit.Test;

import com.wl4g.infra.common.remoting.RestClient;

import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * {@link GraalJava2JSTests}
 * 
 * @author James Wong
 * @version 2022-09-24
 * @since v3.0.0
 * @see https://github.com/AMIS-Services/jfall2019-graalvm/blob/master/polyglot/java2js/nl/amis/java2js/CallbackFromJS2J.java
 */
public class GraalJS2JavaTests {

    @Test
    public void testJsCallJava() {
        Context c = Context.create("js");
        c.getBindings("js").putMember("friend", FriendlyNeighbour.builder().nickName("jack001").build());
        c.eval("js", "print(`Live from JavaScript: ${friend.goodMorning('Jim')}`)");
    }

    @Test
    public void testJsCallJava2() throws Exception {
        Context c = Context.newBuilder("js").allowAllAccess(true).build();
        Value bindings = c.getBindings("js");

        Map<String, Object> attributes = singletonMap("objId", "1010012");
        MyEventSource eventSource = MyEventSource.builder()
                .sourceTime(16182771236L)
                .attributes(ProxyObject.fromMap(attributes))
                .build();
        System.out.println(toJSONString(eventSource));

        MyFunctionContext functionContext = MyFunctionContext.builder()
                .id("100101")
                .type("login")
                .eventSource(eventSource)
                .build();

        bindings.putMember("httpClient", new MyHttpClient());

        File call2JavaJS = new File(getClass().getClassLoader().getResource("graalvm/js/test-js2java.js").getFile());
        c.eval(Source.newBuilder("js", call2JavaJS).build());

        Value processFunction = c.getBindings("js").getMember("process");
        String result = processFunction.execute(functionContext).asString();
        System.out.println(result);
    }

    @Data
    @SuperBuilder
    public static class FriendlyNeighbour {
        private String nickName;

        @HostAccess.Export
        public String goodMorning(String myName) {
            return "Good morning my dear " + myName + " - " + nickName + " from the wonderful world of Java";
        }
    }

    @Data
    @SuperBuilder
    public static class MyFunctionContext {
        private String id;
        private String type;
        private MyEventSource eventSource;

        @HostAccess.Export
        public String getId() {
            return id;
        }

        @HostAccess.Export
        public String getType() {
            return type;
        }

        @HostAccess.Export
        public MyEventSource getEventSource() {
            return eventSource;
        }
    }

    @Data
    @SuperBuilder
    public static class MyEventSource {
        private Long sourceTime;
        private ProxyObject attributes;

        @HostAccess.Export
        public Long getSourceTime() {
            return sourceTime;
        }

        @HostAccess.Export
        public ProxyObject getAttributes() {
            return attributes;
        }
    }

    public static class MyHttpClient {

        @HostAccess.Export
        public String get(String url) {
            return new RestClient().getForObject(url, String.class);
        }
    }

}