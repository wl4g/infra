/*
 * Copyright 2017 ~ 2025 the original author or authors. <James Wong <jameswong1376@gmail.com>>
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
package com.wl4g.infra.integration.feign.istio;

import java.util.Collections;

import org.junit.Test;

import io.fabric8.istio.api.networking.v1beta1.DestinationRuleBuilder;
import io.fabric8.istio.api.networking.v1beta1.DestinationRuleList;
import io.fabric8.istio.api.networking.v1beta1.GatewayBuilder;
import io.fabric8.istio.api.networking.v1beta1.GatewayList;
import io.fabric8.istio.api.networking.v1beta1.IstioEgressListenerBuilder;
import io.fabric8.istio.api.networking.v1beta1.LoadBalancerSettingsBuilder;
import io.fabric8.istio.api.networking.v1beta1.LoadBalancerSettingsSimple;
import io.fabric8.istio.api.networking.v1beta1.LoadBalancerSettingsSimpleLB;
import io.fabric8.istio.api.networking.v1beta1.PortBuilder;
import io.fabric8.istio.api.networking.v1beta1.ServerBuilder;
import io.fabric8.istio.api.networking.v1beta1.ServerTLSSettingsBuilder;
import io.fabric8.istio.api.networking.v1beta1.ServiceEntryBuilder;
import io.fabric8.istio.api.networking.v1beta1.ServiceEntryList;
import io.fabric8.istio.api.networking.v1beta1.ServiceEntryLocation;
import io.fabric8.istio.api.networking.v1beta1.SidecarBuilder;
import io.fabric8.istio.api.networking.v1beta1.SidecarList;
import io.fabric8.istio.api.networking.v1beta1.VirtualServiceBuilder;
import io.fabric8.istio.api.networking.v1beta1.VirtualServiceList;
import io.fabric8.istio.api.networking.v1beta1.WorkloadEntryBuilder;
import io.fabric8.istio.api.networking.v1beta1.WorkloadEntryList;
import io.fabric8.istio.api.security.v1beta1.AuthorizationPolicyAction;
import io.fabric8.istio.api.security.v1beta1.AuthorizationPolicyBuilder;
import io.fabric8.istio.api.security.v1beta1.AuthorizationPolicyList;
import io.fabric8.istio.api.security.v1beta1.ConditionBuilder;
import io.fabric8.istio.api.security.v1beta1.OperationBuilder;
import io.fabric8.istio.api.security.v1beta1.PeerAuthenticationBuilder;
import io.fabric8.istio.api.security.v1beta1.PeerAuthenticationList;
import io.fabric8.istio.api.security.v1beta1.PeerAuthenticationMutualTLS;
import io.fabric8.istio.api.security.v1beta1.PeerAuthenticationMutualTLSMode;
import io.fabric8.istio.api.security.v1beta1.RuleBuilder;
import io.fabric8.istio.api.security.v1beta1.RuleFromBuilder;
import io.fabric8.istio.api.security.v1beta1.RuleToBuilder;
import io.fabric8.istio.api.security.v1beta1.SourceBuilder;
import io.fabric8.istio.api.type.v1beta1.WorkloadSelectorBuilder;
import io.fabric8.istio.client.DefaultIstioClient;
import io.fabric8.istio.client.IstioClient;
import io.fabric8.kubernetes.client.ConfigBuilder;

/**
 * {@link IstioDiscoveryClientTests}
 * 
 * @author James Wong &lt;983708408@qq.com, wanglsir@gmail.com&gt;
 * @version 2022-03-01 v1.0.0
 * @since v1.0.0
 */
public class IstioDiscoveryClientTests {

    private static final String NAMESPACE = "default";

    // https://github.com/fabric8io/kubernetes-client/blob/master/extensions/istio/examples/src/main/java/io/fabric8/istio/api/examples/v1beta1/GatewayExample.java
    @Test
    public void testIstioGateways() {
        IstioClient client = newDefaultClient();
        System.out.println("Creating a gateway");

        // Example from:
        // https://istio.io/latest/docs/reference/config/networking/gateway/
        client.v1beta1().gateways().inNamespace(NAMESPACE).create(new GatewayBuilder().withNewMetadata()
                .withName("my-gateway")
                .endMetadata()
                .withNewSpec()
                .withSelector(Collections.singletonMap("app", "my-gateway-controller"))
                .withServers(new ServerBuilder()
                        .withPort(new PortBuilder().withNumber(80).withProtocol("HTTP").withName("http").build())
                        .withHosts("uk.bookinfo.com", "eu.bookinfo.com")
                        .withTls(new ServerTLSSettingsBuilder().withHttpsRedirect(true).build())
                        .build())
                .endSpec()
                .build());

        System.out.println("Listing gateway instances:");

        GatewayList list = client.v1beta1().gateways().inNamespace(NAMESPACE).list();
        list.getItems().forEach(b -> System.out.println(b.getMetadata().getName()));

        System.out.println("Done");
    }

    // https://github.com/fabric8io/kubernetes-client/blob/master/extensions/istio/examples/src/main/java/io/fabric8/istio/api/examples/v1beta1/VirtualServiceExample.java
    @Test
    public void testIstioVirtualServices() {
        IstioClient client = newDefaultClient();
        System.out.println("Creating a virtual service");

        // Example from:
        // https://istio.io/latest/docs/reference/config/networking/virtual-service/
        String reviewsHost = "reviews.prod.svc.cluster.local";
        client.v1beta1().virtualServices().inNamespace(NAMESPACE).create(new VirtualServiceBuilder().withNewMetadata()
                .withName("reviews-route")
                .endMetadata()
                .withNewSpec()
                .addToHosts(reviewsHost)
                .addNewHttp()
                .withName("reviews-v2-routes")
                .addNewMatch()
                .withNewUri()
                .withNewStringMatchPrefixType("/wpcatalog")
                .endUri()
                .endMatch()
                .addNewMatch()
                .withNewUri()
                .withNewStringMatchPrefixType("/consumercatalog")
                .endUri()
                .endMatch()
                .withNewRewrite()
                .withUri("/newcatalog")
                .endRewrite()
                .addNewRoute()
                .withNewDestination()
                .withHost(reviewsHost)
                .withSubset("v2")
                .endDestination()
                .endRoute()
                .endHttp()
                .addNewHttp()
                .withName("reviews-v2-routes")
                .addNewRoute()
                .withNewDestination()
                .withHost(reviewsHost)
                .withSubset("v1")
                .endDestination()
                .endRoute()
                .endHttp()
                .endSpec()
                .build());

        System.out.println("Listing Virtual Service Instances:");

        VirtualServiceList list = client.v1beta1().virtualServices().inNamespace(NAMESPACE).list();
        list.getItems().forEach(b -> System.out.println(b.getMetadata().getName()));

        System.out.println("Done");
    }

    // https://github.com/fabric8io/kubernetes-client/blob/master/extensions/istio/examples/src/main/java/io/fabric8/istio/api/examples/v1beta1/GatewayExample.java
    @Test
    public void testIstioDestinations() {
        IstioClient client = newDefaultClient();
        System.out.println("Creating a destinations");

        // Example from:
        // https://istio.io/latest/docs/reference/config/networking/destination-rule/
        client.v1beta1().destinationRules().inNamespace(NAMESPACE).create(new DestinationRuleBuilder().withNewMetadata()
                .withName("reviews-route")
                .endMetadata()
                .withNewSpec()
                .withHost("ratings.prod.svc.cluster.local")
                .withNewTrafficPolicy()
                .withLoadBalancer(new LoadBalancerSettingsBuilder()
                        .withLbPolicy(new LoadBalancerSettingsSimple(LoadBalancerSettingsSimpleLB.RANDOM))
                        .build())
                .endTrafficPolicy()
                .endSpec()
                .build());

        System.out.println("Listing destination rules instances:");

        DestinationRuleList list = client.v1beta1().destinationRules().inNamespace(NAMESPACE).list();
        list.getItems().forEach(b -> System.out.println(b.getMetadata().getName()));

        System.out.println("Done");
    }

    // https://github.com/fabric8io/kubernetes-client/blob/master/extensions/istio/examples/src/main/java/io/fabric8/istio/api/examples/v1beta1/ServiceEntryExample.java
    @Test
    public void testIstioServiceEntries() {
        IstioClient client = newDefaultClient();
        System.out.println("Creating a service entries");

        // Example from:
        // https://istio.io/latest/docs/reference/config/networking/service-entry/
        client.v1beta1().serviceEntries().inNamespace(NAMESPACE).create(new ServiceEntryBuilder().withNewMetadata()
                .withName("external-svc-https")
                .endMetadata()
                .withNewSpec()
                .withHosts("api.dropboxapi.com", "www.googleapis.com")
                .withLocation(ServiceEntryLocation.MESH_INTERNAL)
                .withPorts(new PortBuilder().withName("https").withProtocol("TLS").withNumber(443).build())
                .endSpec()
                .build());

        System.out.println("Listing Virtual Service Instances:");

        ServiceEntryList list = client.v1beta1().serviceEntries().inNamespace(NAMESPACE).list();
        list.getItems().forEach(b -> System.out.println(b.getMetadata().getName()));

        System.out.println("Done");
    }

    // https://github.com/fabric8io/kubernetes-client/blob/master/extensions/istio/examples/src/main/java/io/fabric8/istio/api/examples/v1beta1/WorkloadEntryExample.java
    @Test
    public void testIstioWorkloadEntries() {
        IstioClient client = newDefaultClient();
        System.out.println("Creating a workload entries");

        // Example from:
        // https://istio.io/latest/docs/reference/config/networking/workload-entry/
        client.v1beta1().workloadEntries().inNamespace(NAMESPACE).create(new WorkloadEntryBuilder().withNewMetadata()
                .withName("details-svc")
                .endMetadata()
                .withNewSpec()
                .withServiceAccount("details-legacy")
                .withLabels(Collections.singletonMap("app", "details-legacy"))
                .endSpec()
                .build());

        System.out.println("Listing workload entry instances:");

        WorkloadEntryList list = client.v1beta1().workloadEntries().inNamespace(NAMESPACE).list();
        list.getItems().forEach(b -> System.out.println(b.getMetadata().getName()));

        System.out.println("Done");
    }

    // https://github.com/fabric8io/kubernetes-client/blob/master/extensions/istio/examples/src/main/java/io/fabric8/istio/api/examples/v1beta1/SidecarExample.java
    @Test
    public void testIstioSidecar() {
        IstioClient client = newDefaultClient();
        System.out.println("Creating a sidecar");
        // Example from:
        // https://istio.io/latest/docs/reference/config/networking/sidecar/
        client.v1beta1().sidecars().inNamespace(NAMESPACE).create(new SidecarBuilder().withNewMetadata()
                .withName("default")
                .endMetadata()
                .withNewSpec()
                .withEgress(new IstioEgressListenerBuilder().withHosts("./*", "istio-system/*").build())
                .endSpec()
                .build());

        System.out.println("Listing sidecar instances:");

        SidecarList list = client.v1beta1().sidecars().inNamespace(NAMESPACE).list();
        list.getItems().forEach(b -> System.out.println(b.getMetadata().getName()));

        System.out.println("Done");
    }

    // https://github.com/fabric8io/kubernetes-client/blob/master/extensions/istio/examples/src/main/java/io/fabric8/istio/api/examples/v1beta1/PeerAuthenticationExample.java
    @Test
    public void testIstioPeerAuthentication() {
        IstioClient client = newDefaultClient();
        System.out.println("Creating a PeerAuthentication entry");

        client.v1beta1().peerAuthentications().inNamespace(NAMESPACE).create(new PeerAuthenticationBuilder().withNewMetadata()
                .withName("details-svc")
                .endMetadata()
                .withNewSpec()
                .withSelector(new WorkloadSelectorBuilder().addToMatchLabels("app", "reviews").build())
                .withMtls(new PeerAuthenticationMutualTLS(PeerAuthenticationMutualTLSMode.DISABLE))
                .endSpec()
                .build());

        System.out.println("Listing workload entry instances:");

        PeerAuthenticationList list = client.v1beta1().peerAuthentications().inNamespace(NAMESPACE).list();
        list.getItems().forEach(b -> System.out.println(b.getMetadata().getName()));

        System.out.println("Done");
    }

    // https://github.com/fabric8io/kubernetes-client/blob/master/extensions/istio/examples/src/main/java/io/fabric8/istio/api/examples/v1beta1/AuthorizationPolicyExample.java
    @Test
    public void testIstioAuthorizationPolicy() {
        IstioClient client = newDefaultClient();
        System.out.println("Creating a AuthorizationPolicy entry");

        client.v1beta1().authorizationPolicies().inNamespace(NAMESPACE).create(new AuthorizationPolicyBuilder().withNewMetadata()
                .withName("httpbin")
                .endMetadata()
                .withNewSpec()
                .withSelector(new WorkloadSelectorBuilder().withMatchLabels(Collections.singletonMap("app", "httpbin")).build())
                .withAction(AuthorizationPolicyAction.DENY)
                .withRules(new RuleBuilder()
                        .withFrom(
                                new RuleFromBuilder().withSource(
                                        new SourceBuilder().withPrincipals("cluster.local/ns/default/sa/sleep").build()).build(),
                                new RuleFromBuilder().withSource(new SourceBuilder().withNamespaces("dev").build()).build())
                        .withTo(new RuleToBuilder().withOperation(new OperationBuilder().withMethods("GET").build()).build())
                        .withWhen(new ConditionBuilder().withKey("request.auth.claims[iss]")
                                .withValues("https://accounts.google.com")
                                .build())
                        .build())
                .endSpec()
                .build());

        System.out.println("Listing AuthorizationPolicy instances:");

        AuthorizationPolicyList list = client.v1beta1().authorizationPolicies().inNamespace(NAMESPACE).list();
        list.getItems().forEach(b -> System.out.println(b.getMetadata().getName()));

        System.out.println("Done");
    }

    static IstioClient newDefaultClient() {
        io.fabric8.kubernetes.client.Config config = new ConfigBuilder().withMasterUrl("https://10.0.0.150:33385")
                .withOauthToken("")
                .withUsername("")
                .withPassword("")
                .withNamespace(NAMESPACE)
                .build();
        return new DefaultIstioClient(config);
    }

}