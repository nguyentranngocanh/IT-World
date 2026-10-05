package model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Post implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String author;
    private String authorLevel; // Trình độ của tác giả lúc đăng bài (vd: Senior Java Dev)
    private String title;
    private String content;
    private String codeSnippet; // Đoạn mã lập trình (nếu có)
    private String codeLanguage; // Java, Python, JavaScript, C++, SQL...
    private String tags; // #java #backend #spring
    private String createdAt;
    private int likesCount;
    private boolean isLikedByMe;
    private List<Comment> comments;

    public Post(String id, String author, String title, String content, String codeSnippet, String codeLanguage, String tags) {
        this.id = id;
        this.author = author;
        this.authorLevel = "Developer";
        this.title = title;
        this.content = content;
        this.codeSnippet = codeSnippet;
        this.codeLanguage = (codeLanguage != null && !codeLanguage.isEmpty()) ? codeLanguage : "Java";
        this.tags = (tags != null && !tags.isEmpty()) ? tags : "#itworld";
        this.createdAt = new SimpleDateFormat("HH:mm dd/MM/yyyy").format(new Date());
        this.likesCount = 0;
        this.isLikedByMe = false;
        this.comments = new ArrayList<>();
    }

    public List<String> getTagList() {
        if (tags == null || tags.trim().isEmpty()) return new ArrayList<>();
        String[] parts = tags.split("[,;\\s]+");
        List<String> list = new ArrayList<>();
        for (String p : parts) {
            String trimmed = p.trim();
            if (!trimmed.isEmpty()) {
                if (!trimmed.startsWith("#")) trimmed = "#" + trimmed;
                list.add(trimmed);
            }
        }
        return list;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getAuthorLevel() { return authorLevel != null ? authorLevel : "Developer"; }
    public void setAuthorLevel(String authorLevel) { this.authorLevel = authorLevel; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getCodeSnippet() { return codeSnippet; }
    public void setCodeSnippet(String codeSnippet) { this.codeSnippet = codeSnippet; }

    public String getCodeLanguage() { return codeLanguage != null ? codeLanguage : "Java"; }
    public void setCodeLanguage(String codeLanguage) { this.codeLanguage = codeLanguage; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public int getLikesCount() { return likesCount; }
    public void setLikesCount(int likesCount) { this.likesCount = likesCount; }

    public boolean isLikedByMe() { return isLikedByMe; }
    public void setLikedByMe(boolean likedByMe) { isLikedByMe = likedByMe; }

    public List<Comment> getComments() {
        if (comments == null) comments = new ArrayList<>();
        return comments;
    }
    public void setComments(List<Comment> comments) { this.comments = comments; }
}
