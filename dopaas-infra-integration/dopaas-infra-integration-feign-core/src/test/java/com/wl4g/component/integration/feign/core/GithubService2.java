package com.wl4g.component.integration.feign.core;

import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.wl4g.component.integration.feign.core.annotation.FeignConsumer;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * {@link GithubService2}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-23
 * @sine v1.0
 * @see
 */
@FeignConsumer(name = "github", path = "${github.api.user-path}")
public interface GithubService2 {

	@RequestMapping(method = GET, path = "/{owner}/repos")
	List<GitHubRepoModel> getRepos(@PathVariable("owner") String owner);

}
