package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_follows", uniqueConstraints = @UniqueConstraint(columnNames = {"follower_id", "following_id"}))
public class UserFollow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "follower_id", nullable = false)
    private UserAccount follower;

    @ManyToOne(optional = false)
    @JoinColumn(name = "following_id", nullable = false)
    private UserAccount following;

    private LocalDateTime createdAt;

    public UserFollow() {
    }

    public UserFollow(UserAccount follower, UserAccount following) {
        this.follower = follower;
        this.following = following;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public UserAccount getFollower() { return follower; }
    public UserAccount getFollowing() { return following; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
