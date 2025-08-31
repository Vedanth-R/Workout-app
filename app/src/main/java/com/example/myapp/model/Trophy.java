package com.example.myapp.model;

public class Trophy {

    public enum Type { BINARY, INCREMENTAL }

    private final String id;
    private final String title;
    private final String description;
    private final Type type;

    // Progress (for INCREMENTAL)
    private int current;  // 0..total
    private int total;    // 0 if not incremental

    // Status
    private boolean unlocked;

    public Trophy(String id, String title, String description, Type type) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.type = type;
    }

    // --- Getters / Setters ---
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Type getType() { return type; }

    public int getCurrent() { return current; }
    public void setCurrent(int current) { this.current = current; }

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    public boolean isUnlocked() { return unlocked; }
    public void setUnlocked(boolean unlocked) { this.unlocked = unlocked; }

    public boolean isIncremental() { return type == Type.INCREMENTAL && total > 0; }
}
