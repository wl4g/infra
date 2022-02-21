package com.wl4g.component.integration.feign.core;

import feign.Contract;
import feign.Param;
import feign.RequestLine;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wl4g.component.integration.feign.core.annotation.FeignConsumer;
import com.wl4g.component.integration.feign.core.annotation.mvc.SpringMvcContract;

/**
 * {@link GithubService1}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-23
 * @sine v1.0
 * @see
 */
@SuppressWarnings("unused")
@FeignConsumer(name = "github", url = "${github.api.url}", configuration = { Contract.Default.class })
public interface GithubService1 {

	@RequestLine("GET /repos/{owner}/{repo}/contributors")
	List<GitHubContributor> getContributors(@Param("owner") String owner, @Param("repo") String repo);

	/**
	 * <pre>
	 * {
	 *   "login": "wl4g",
	 *   "id": 29530154,
	 *   "node_id": "MDQ6VXNlcjI5NTMwMTU0",
	 *   "avatar_url": "https://avatars0.githubusercontent.com/u/29530154?v=4",
	 *   "gravatar_id": "",
	 *   "url": "https://api.github.com/users/wl4g",
	 *   "html_url": "https://github.com/wl4g",
	 *   "followers_url": "https://api.github.com/users/wl4g/followers",
	 *   "following_url": "https://api.github.com/users/wl4g/following{/other_user}",
	 *   "gists_url": "https://api.github.com/users/wl4g/gists{/gist_id}",
	 *   "starred_url": "https://api.github.com/users/wl4g/starred{/owner}{/repo}",
	 *   "subscriptions_url": "https://api.github.com/users/wl4g/subscriptions",
	 *   "organizations_url": "https://api.github.com/users/wl4g/orgs",
	 *   "repos_url": "https://api.github.com/users/wl4g/repos",
	 *   "events_url": "https://api.github.com/users/wl4g/events{/privacy}",
	 *   "received_events_url": "https://api.github.com/users/wl4g/received_events"
	 * }
	 * </pre>
	 * 
	 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
	 * @version v1.0 2020-12-23
	 * @sine v1.0
	 * @see
	 */
	@Getter
	@Setter
	public static class GitHubContributor {
		private String login;
		private int id;
		private String node_id;
		private String avatar_url;
		private String gravatar_id;
		private String url;
		private String html_url;
		private String followers_url;
		private String following_url;
		private String gists_url;
		private String starred_url;
		private String subscriptions_url;
		private String organizations_url;
		private String repos_url;
		private String events_url;
		private String received_events_url;
		private String type;
		private boolean site_admin;
		private int contributions;
	}

}
