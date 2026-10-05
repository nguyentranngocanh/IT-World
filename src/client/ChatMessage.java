package client;

public class ChatMessage {
    private String sender, text, time, fileName, filePath;
    private boolean isMe, isSystem, isFile;

    public ChatMessage(String sender, String text, String time, boolean isMe, boolean isSystem, boolean isFile, String fileName, String filePath) {
        this.sender = sender;
        this.text = text;
        this.time = time;
        this.isMe = isMe;
        this.isSystem = isSystem;
        this.isFile = isFile;
        this.fileName = fileName;
        this.filePath = filePath;
    }

    public String getSender() { return sender; }
    public String getText() { return text; }
    public String getTime() { return time; }
    public String getFileName() { return fileName; }
    public String getFilePath() { return filePath; }
    public boolean isMe() { return isMe; }
    public boolean isSystem() { return isSystem; }
    public boolean isFile() { return isFile; }
}