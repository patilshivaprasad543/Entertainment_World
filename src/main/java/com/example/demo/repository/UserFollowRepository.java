package com.example.demo.repository;

import com.example.demo.model.UserAccount;
import com.example.demo.model.UserFollow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserFollowRepository extends JpaRepository<UserFollow, Long> {

    Optional<UserFollow> findByFollowerAndFollowing(UserAccount follower, UserAccount following);

    List<UserFollow> findByFollower(UserAccount follower);

    List<UserFollow> findByFollowing(UserAccount following);

    long countByFollowing(UserAccount following);

    long countByFollower(UserAccount follower);
}
