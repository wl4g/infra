package com.wl4g.infra.integration.feign.core.discovery;

import static com.wl4g.infra.common.lang.Assert2.notNullOf;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.springframework.util.StringUtils;

import com.wl4g.infra.integration.feign.core.config.FeignConsumerProperties;

import feign.Request;
import feign.RequestTemplate;

/**
 * Cached discoverable URL feign target.
 */
public class CachedDiscoverableUrlTarget<T> implements feign.Target<T> {

    private final FeignConsumerProperties config;
    private final Class<T> type;
    private final String name;
    private final String url;
    private final String path;

    public CachedDiscoverableUrlTarget(@NotNull FeignConsumerProperties config, @NotNull Class<T> type, @Nullable String name,
            @Nullable String url, @Nullable String path) {
        this.config = notNullOf(config, "config");
        this.type = notNullOf(type, "type");
        this.name = name;
        this.url = url;
        this.path = path;
    }

    @Override
    public Class<T> type() {
        return this.type;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public String url() {
        return buildRequestUrl();
    }

    /* no authentication or other special activity. just insert the url. */
    @Override
    public Request apply(RequestTemplate input) {
        if (input.url().indexOf("http") != 0) {
            input.target(url());
        }
        return input.request();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CachedDiscoverableUrlTarget) {
            CachedDiscoverableUrlTarget<T> other = (CachedDiscoverableUrlTarget<T>) obj;
            return type.equals(other.type) && name.equals(other.name) && url.equals(other.url);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + type.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + url.hashCode();
        return result;
    }

    @Override
    public String toString() {
        if (name.equals(url)) {
            return "HardCodedTarget(type=" + type.getSimpleName() + ", url=" + url + ")";
        }
        return "HardCodedTarget(type=" + type.getSimpleName() + ", name=" + name + ", url=" + url + ")";
    }

    private String buildRequestUrl() {
        // Gets first by absolute URL
        if (!isBlank(this.url)) {
            String url = trimToEmpty(isBlank(this.url) ? config.getDefaultUrl() : this.url);
            return url.concat(cleanPath());
        }
        // Gets secondary by serviceId(name)
        else if (!isBlank(name)) {
            return doGetDiscoveryServiceUrl().concat(cleanPath());
        }
        // Gets fall-back by default URL.
        return config.getDefaultUrl().concat(cleanPath());
    }

    private String cleanPath() {
        String path = trimToEmpty(this.path);
        if (StringUtils.hasLength(path)) {
            if (!path.startsWith("/")) {
                path = "/".concat(path);
            }
            if (path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }
        }
        return EMPTY;
    }

    private String doGetDiscoveryServiceUrl() {
        return name;
    }

}
