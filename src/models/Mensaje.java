package models;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Mensaje {
    private String username;
    private LocalDateTime createdAt = LocalDateTime.now();

    public Mensaje(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
