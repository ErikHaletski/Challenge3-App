package de.challenge3.questapp.ui.quest;

public class Quest {

    public enum QuestType {
        DAILY,
        NORMAL
    }

    private String id;
    private String title;
    private String description;
    private boolean isCompleted;
    private int xpReward;
    private String statType;
    private int statReward;
    private QuestType type;
    private long expiresAt; // (Unix-Timestamp), 0 = kein Ablauf

    public Quest(String id, String title, String description,
                 int xpReward, String statType, int statReward,
                 QuestType type, long expiresAt) {

        this.id = id;
        this.title = title;
        this.description = description;
        this.isCompleted = false;
        this.xpReward = xpReward;
        this.statType = statType;
        this.statReward = statReward;
        this.type = type;
        this.expiresAt = expiresAt;
    }

    // Getter & Setter
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public boolean isCompleted() { return isCompleted; }
    public int getXpReward() { return xpReward; }
    public String getStatType() { return statType; }
    public int getStatReward() { return statReward; }
    public QuestType getType() { return type; }
    public long getExpiresAt() { return expiresAt; }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }

    public void setType(QuestType type) {
        this.type = type;
    }

    // Quest abgelaufen?
    public boolean isExpired() {
        return type == QuestType.DAILY && System.currentTimeMillis() > expiresAt;
    }
}

