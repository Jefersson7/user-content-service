package com.usercontent.infrastructure.adapter.out.persistence.repository;

import com.usercontent.infrastructure.adapter.out.persistence.entity.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostJpaRepository extends JpaRepository<PostEntity, Long> {

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from PostEntity p where p.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
