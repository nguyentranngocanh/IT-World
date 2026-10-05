package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UserProfile implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private String fullName;
    private String itLevel; // Intern, Fresher, Junior Developer, Middle Developer, Senior Developer, Tech Lead, Solution Architect
    private String bio;
    private String techStack; // Phân tách bằng dấu phẩy: "Java, Spring Boot, MySQL, Docker"
    private String githubUrl;
    private int followersCount;
    private int followingCount;
    private int postsCount;
    private boolean isFollowing; // Trạng thái người xem hiện tại có đang follow người này không

    public UserProfile(String username) {
        this.username = username;
        this.fullName = username;
        this.itLevel = "Junior Developer";
        this.bio = "Lập trình viên đam mê công nghệ tại IT World.";
        this.techStack = "Java, MySQL, Git";
        this.githubUrl = "https://github.com/" + username;
        this.followersCount = 0;
        this.followingCount = 0;
        this.postsCount = 0;
        this.isFollowing = false;
    }

    public List<String> getTechStackList() {
        if (techStack == null || techStack.trim().isEmpty()) {
            return new ArrayList<>();
        }
        String[] parts = techStack.split("[,;]+");
        List<String> list = new ArrayList<>();
        for (String p : parts) {
            String trimmed = p.trim();
            if (!trimmed.isEmpty()) list.add(trimmed);
        }
        return list;
    }

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return (fullName != null && !fullName.isEmpty()) ? fullName : username; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getItLevel() { return itLevel != null ? itLevel : "Junior Developer"; }
    public void setItLevel(String itLevel) { this.itLevel = itLevel; }

    public String getBio() { return bio != null ? bio : ""; }
    public void setBio(String bio) { this.bio = bio; }

    public String getTechStack() { return techStack != null ? techStack : ""; }
    public void setTechStack(String techStack) { this.techStack = techStack; }

    public String getGithubUrl() { return githubUrl != null ? githubUrl : ""; }
    public void setGithubUrl(String githubUrl) { this.githubUrl = githubUrl; }

    public int getFollowersCount() { return followersCount; }
    public void setFollowersCount(int followersCount) { this.followersCount = followersCount; }

    public int getFollowingCount() { return followingCount; }
    public void setFollowingCount(int followingCount) { this.followingCount = followingCount; }

    public int getPostsCount() { return postsCount; }
    public void setPostsCount(int postsCount) { this.postsCount = postsCount; }

    public boolean isFollowing() { return isFollowing; }
    public void setFollowing(boolean following) { isFollowing = following; }
}
