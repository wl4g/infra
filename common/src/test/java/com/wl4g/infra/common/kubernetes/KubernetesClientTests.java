/*
 * Copyright 2017 ~ 2025 the original author or authors. <wanglsir@gmail.com, 983708408@qq.com>
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
package com.wl4g.infra.common.kubernetes;

import static com.wl4g.infra.common.collection.CollectionUtils2.safeList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.google.gson.reflect.TypeToken;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1ContainerPortBuilder;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.openapi.models.V1PodSpec;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.openapi.models.V1ServiceList;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.Watch;
import okhttp3.Call;
import okhttp3.OkHttpClient;

/**
 * {@link KubernetesClientTests}
 * 
 * @author James Wong &lt;983708408@qq.com, wanglsir@gmail.com&gt;
 * @version 2022-03-01 v1.0.0
 * @since v1.0.0
 * @see https://kubernetes.io/docs/reference/using-api/client-libraries/
 */
public class KubernetesClientTests {

    // https://github.com/kubernetes-client/java/blob/v13.0.2/examples/examples-release-13/src/main/java/io/kubernetes/client/examples/Example.java
    @Test
    public void testQueryPodList() throws Exception {
        ApiClient client = Config.defaultClient();
        Configuration.setDefaultApiClient(client);

        CoreV1Api api = new CoreV1Api();

        V1PodList list = api.listPodForAllNamespaces(null, null, null, null, null, null, null, null, null, null);
        for (V1Pod item : list.getItems()) {
            System.out.println(item.getMetadata().getName());
        }
    }

    /**
     * Example:
     * 
     * <pre>
     *   kubectl delete po base-springbootapp-dfb5c8db6-8dx2j
     *   kubectl delete po base-springbootapp-dfb5c8db6-shsxw
     * </pre>
     * 
     * Output:
     * 
     * <pre>
     *   ADDED : nginx-65d6b7bddb-d6xdp
     *   ADDED : base-springbootapp-dfb5c8db6-8dx2j
     *   ADDED : nginx-65d6b7bddb-dnh4p
     *   ADDED : base-springbootapp-dfb5c8db6-shsxw
     *   MODIFIED : base-springbootapp-dfb5c8db6-8dx2j
     *   MODIFIED : base-springbootapp-dfb5c8db6-8dx2j
     *   ADDED : base-springbootapp-dfb5c8db6-l2798
     *   MODIFIED : base-springbootapp-dfb5c8db6-l2798
     *   MODIFIED : base-springbootapp-dfb5c8db6-l2798
     *   MODIFIED : base-springbootapp-dfb5c8db6-8dx2j
     *   MODIFIED : base-springbootapp-dfb5c8db6-l2798
     *   MODIFIED : base-springbootapp-dfb5c8db6-8dx2j
     *   DELETED : base-springbootapp-dfb5c8db6-8dx2j
     *   MODIFIED : base-springbootapp-dfb5c8db6-shsxw
     *   ADDED : base-springbootapp-dfb5c8db6-p6vdr
     *   MODIFIED : base-springbootapp-dfb5c8db6-p6vdr
     *   MODIFIED : base-springbootapp-dfb5c8db6-p6vdr
     *   MODIFIED : base-springbootapp-dfb5c8db6-p6vdr
     *   MODIFIED : base-springbootapp-dfb5c8db6-shsxw
     *   DELETED : base-springbootapp-dfb5c8db6-shsxw
     *   MODIFIED : base-springbootapp-dfb5c8db6-l2798
     *  ...
     * </pre>
     */
    @Test
    public void testWatchPods() throws Exception {
        ApiClient client = Config.defaultClient();
        // infinite timeout
        OkHttpClient httpClient = client.getHttpClient().newBuilder().readTimeout(0, TimeUnit.SECONDS).build();
        client.setHttpClient(httpClient);
        Configuration.setDefaultApiClient(client);

        CoreV1Api api = new CoreV1Api();

        Call podCall = api.listNamespacedPodCall("default", null, false, null, null, null, 10, null, null, null, true, null);
        Watch<V1Pod> watch = Watch.createWatch(client, podCall, new TypeToken<Watch.Response<V1Pod>>() {
        }.getType());

        try {
            for (Watch.Response<V1Pod> item : watch) {
                System.out.printf("%s : %s%n", item.type, item.object.getMetadata().getName());
            }
        } finally {
            watch.close();
        }
    }

    /**
     * Example:
     * 
     * <pre>
     *   kubectl delete svc base-springbootapp
     * </pre>
     * 
     * Output:
     * 
     * <pre>
     *   ADDED : nginx, IPs: [10.96.224.139]
     *   ADDED : redis-base-springbootapp-headless, IPs: [None]
     *   ADDED : zookeeper-base-springbootapp-headless, IPs: [None]
     *   ADDED : jaeger-base-springbootapp-headless, IPs: [None]
     *   ADDED : drds-base-springbootapp-headless, IPs: [None]
     *   ADDED : base-springbootapp, IPs: [10.96.39.174]
     *   ADDED : kubernetes, IPs: [10.96.0.1]
     *   ADDED : nginx-demo-svc, IPs: [10.96.181.137]
     *   DELETED : base-springbootapp, IPs: [10.96.39.174]
     *  ...
     * </pre>
     */
    @Test
    public void testWatchServicesAndDetail() throws Exception {
        ApiClient client = Config.defaultClient();
        Configuration.setDefaultApiClient(client);

        CoreV1Api api = new CoreV1Api();

        Call svcCall = api.listNamespacedServiceCall("default", null, false, null, null, null, 10, null, null, null, true, null);
        Watch<V1Service> watch = Watch.createWatch(client, svcCall, new TypeToken<Watch.Response<V1Service>>() {
        }.getType());
        try {
            for (Watch.Response<V1Service> item : watch) {
                System.out.printf("%s : %s, IPs: %s%n", item.type, item.object.getMetadata().getName(),
                        item.object.getSpec().getClusterIPs());
            }
        } finally {
            watch.close();
        }
    }

    @Test
    public void testCreatePod() throws Exception {
        ApiClient client = Config.defaultClient();
        Configuration.setDefaultApiClient(client);

        CoreV1Api api = new CoreV1Api();

        V1Pod body = new V1Pod();
        body.setKind("Pod");
        body.setApiVersion("v1");

        V1ObjectMeta metadata = new V1ObjectMeta();
        metadata.setNamespace("default");
        metadata.setGenerateName("nginx-test001-");
        metadata.setLabels(singletonMap("name", "nginx"));
        body.setMetadata(metadata);

        V1PodSpec spec = new V1PodSpec();
        body.setSpec(spec);
        V1Container c1 = new V1Container();
        c1.setName("nginx-default");
        c1.setImage("nginx:1.7.9");
        c1.setImagePullPolicy("IfNotPresent");
        c1.setPorts(singletonList(
                new V1ContainerPortBuilder().withName("default-port").withContainerPort(80).withProtocol("TCP").build()));
        spec.setContainers(singletonList(c1));
        spec.setRestartPolicy("Always");

        V1Pod pod = api.createNamespacedPod("default", body, null, null, null, null);
        System.out.println(pod);
    }

    @Test
    public void testGetNodePortServices() throws Exception {
        ApiClient client = Config.defaultClient();
        Configuration.setDefaultApiClient(client);

        CoreV1Api api = new CoreV1Api();

        V1ServiceList services = api.listServiceForAllNamespaces(null, null, null, null, null, null, null, null, null, null);
        // System.out.println(services);

        safeList(services.getItems()).stream().filter(svc -> svc.getSpec().getType().equals("NodePort")).forEach(svc -> {
            System.out.println(svc.getMetadata().getName() + "/" + svc.getMetadata().getNamespace());
            List<String> portString = svc.getSpec().getPorts().stream().map(port -> {
                return port.getName() + "/" + port.getProtocol() + ":" + port.getPort() + "->" + port.getNodePort();
            }).collect(toList());
            System.out.println(portString);
            System.out.println("---");
        });
    }

    @Test
    public void testWatchNodePortServices() throws Exception {
        ApiClient client = Config.defaultClient();
        Configuration.setDefaultApiClient(client);

        CoreV1Api api = new CoreV1Api();

        Call svcCall = api.listNamespacedServiceCall("default", null, false, null, null, null, 10, null, null, null, true, null);
        // System.out.println(svcCall);

        Watch<V1Service> watch = Watch.createWatch(client, svcCall, new TypeToken<Watch.Response<V1Service>>() {
        }.getType());

        try {
            for (Watch.Response<V1Service> item : watch) {
                System.out.printf("%s : %s%n", item.type,
                        item.object.getMetadata().getName() + " updated ports: " + item.object.getSpec().getPorts());
            }
        } finally {
            watch.close();
        }
    }

    @Test
    public void testWatchNodePortAllServices() throws Exception {
        ApiClient client = Config.defaultClient();
        Configuration.setDefaultApiClient(client);

        CoreV1Api api = new CoreV1Api();

        // Node: It will stop automatically after 60 seconds
        Call svcCall = api.listServiceForAllNamespacesCall(null, null, null, null, null, null, null, null, 60, true, null);
        // System.out.println(svcCall);

        Watch<V1Service> watch = Watch.createWatch(client, svcCall, new TypeToken<Watch.Response<V1Service>>() {
        }.getType());

        try {
            for (Watch.Response<V1Service> item : watch) {
                if (nonNull(item.object)) {
                    System.out.printf("%s : %s%n", item.type, item.object.getMetadata().getName());
                    System.out.println("Updated ports: " + item.object.getSpec().getPorts());
                }
            }
        } finally {
            watch.close();
        }
    }

}
