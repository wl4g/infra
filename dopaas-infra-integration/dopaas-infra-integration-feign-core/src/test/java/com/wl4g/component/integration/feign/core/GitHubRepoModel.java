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
package com.wl4g.component.integration.feign.core;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

/**
 * {@link GitHubRepoModel}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2021-12-13
 * @sine v1.0
 * @see
 */
@Getter
@Setter
public class GitHubRepoModel {
	private int id;
	@JsonProperty("node_id")
	private String nodeId;
	private String name;
	@JsonProperty("full_name")
	private String fullName;
	private boolean privateStr;
	private Owner owner;
	@JsonProperty("html_url")
	private String htmlUrl;
	private String description;
	private boolean fork;
	private String url;
	@JsonProperty("forks_url")
	private String forksUrl;
	@JsonProperty("keys_url")
	private String keysUrl;
	@JsonProperty("collaborators_url")
	private String collaboratorsUrl;
	@JsonProperty("teams_url")
	private String teamsUrl;
	@JsonProperty("hooks_url")
	private String hooksUrl;
	@JsonProperty("issue_events_url")
	private String issueEventsUrl;
	@JsonProperty("events_url")
	private String eventsUrl;
	@JsonProperty("assignees_url")
	private String assigneesUrl;
	@JsonProperty("branches_url")
	private String branchesUrl;
	@JsonProperty("tags_url")
	private String tagsUrl;
	@JsonProperty("blobs_url")
	private String blobsUrl;
	@JsonProperty("git_tags_url")
	private String gitTagsUrl;
	@JsonProperty("git_refs_url")
	private String gitRefsUrl;
	@JsonProperty("trees_url")
	private String treesUrl;
	@JsonProperty("statuses_url")
	private String statusesUrl;
	@JsonProperty("languages_url")
	private String languagesUrl;
	@JsonProperty("stargazers_url")
	private String stargazersUrl;
	@JsonProperty("contributors_url")
	private String contributorsUrl;
	@JsonProperty("subscribers_url")
	private String subscribersUrl;
	@JsonProperty("subscription_url")
	private String subscriptionUrl;
	@JsonProperty("commits_url")
	private String commitsUrl;
	@JsonProperty("git_commits_url")
	private String gitCommitsUrl;
	@JsonProperty("comments_url")
	private String commentsUrl;
	@JsonProperty("issue_comment_url")
	private String issueCommentUrl;
	@JsonProperty("contents_url")
	private String contentsUrl;
	@JsonProperty("compare_url")
	private String compareUrl;
	@JsonProperty("merges_url")
	private String mergesUrl;
	@JsonProperty("archive_url")
	private String archiveUrl;
	@JsonProperty("downloads_url")
	private String downloadsUrl;
	@JsonProperty("issues_url")
	private String issuesUrl;
	@JsonProperty("pulls_url")
	private String pullsUrl;
	@JsonProperty("milestones_url")
	private String milestonesUrl;
	@JsonProperty("notifications_url")
	private String notificationsUrl;
	@JsonProperty("labels_url")
	private String labelsUrl;
	@JsonProperty("releases_url")
	private String releasesUrl;
	@JsonProperty("deployments_url")
	private String deploymentsUrl;
	@JsonProperty("created_at")
	private Date createdAt;
	@JsonProperty("updated_at")
	private Date updatedAt;
	@JsonProperty("pushed_at")
	private Date pushedAt;
	@JsonProperty("git_url")
	private String gitUrl;
	@JsonProperty("ssh_url")
	private String sshUrl;
	@JsonProperty("clone_url")
	private String cloneUrl;
	@JsonProperty("svn_url")
	private String svnUrl;
	private String homepage;
	private int size;
	@JsonProperty("stargazers_count")
	private int stargazersCount;
	@JsonProperty("watchers_count")
	private int watchersCount;
	private String language;
	@JsonProperty("has_issues")
	private boolean hasIssues;
	@JsonProperty("has_projects")
	private boolean hasProjects;
	@JsonProperty("has_downloads")
	private boolean hasDownloads;
	@JsonProperty("has_wiki")
	private boolean hasWiki;
	@JsonProperty("has_pages")
	private boolean hasPages;
	@JsonProperty("forks_count")
	private int forksCount;
	@JsonProperty("mirror_url")
	private String mirrorUrl;
	private boolean archived;
	private boolean disabled;
	@JsonProperty("open_issues_count")
	private int openIssuesCount;
	private License license;
	private int forks;
	@JsonProperty("open_issues")
	private int openIssues;
	private int watchers;
	@JsonProperty("default_branch")
	private String defaultBranch;

	@Getter
	@Setter
	public static class Owner {
		private String login;
		private int id;
		@JsonProperty("node_id")
		private String nodeId;
		@JsonProperty("avatar_url")
		private String avatarUrl;
		@JsonProperty("gravatar_id")
		private String gravatarId;
		private String url;
		@JsonProperty("html_url")
		private String htmlUrl;
		@JsonProperty("followers_url")
		private String followersUrl;
		@JsonProperty("following_url")
		private String followingUrl;
		@JsonProperty("gists_url")
		private String gistsUrl;
		@JsonProperty("starred_url")
		private String starredUrl;
		@JsonProperty("subscriptions_url")
		private String subscriptionsUrl;
		@JsonProperty("organizations_url")
		private String organizationsUrl;
		@JsonProperty("repos_url")
		private String reposUrl;
		@JsonProperty("events_url")
		private String eventsUrl;
		@JsonProperty("received_events_url")
		private String receivedEventsUrl;
		private String type;
		@JsonProperty("site_admin")
		private boolean siteAdmin;
	}

	@Getter
	@Setter
	public static class License {
		private String key;
		private String name;
		@JsonProperty("spdx_id")
		private String spdxId;
		private String url;
		@JsonProperty("node_id")
		private String nodeId;
	}

}