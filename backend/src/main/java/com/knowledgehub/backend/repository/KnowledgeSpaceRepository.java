package com.knowledgehub.backend.repository;

import com.knowledgehub.backend.entity.KnowledgeSpace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnowledgeSpaceRepository extends JpaRepository<KnowledgeSpace, Long> {

    // Core Multi-Tenancy Search: Fetch spaces only belonging to a specific user
    List<KnowledgeSpace> findByUserId(Long userId);
}
