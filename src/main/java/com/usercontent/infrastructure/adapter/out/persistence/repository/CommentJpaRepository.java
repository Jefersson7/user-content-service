package com.usercontent.infrastructure.adapter.out.persistence.repository;

import com.usercontent.infrastructure.adapter.out.persistence.entity.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface CommentJpaRepository extends JpaRepository<CommentEntity, Long> {

    List<CommentEntity> findByPost_IdInOrderByIdAsc(Collection<Long> postIds);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from CommentEntity c where c.post.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
