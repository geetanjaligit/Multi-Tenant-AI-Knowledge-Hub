package com.knowledgehub.backend.service;

import com.knowledgehub.backend.entity.KnowledgeSpace;
import com.knowledgehub.backend.entity.User;
import com.knowledgehub.backend.repository.KnowledgeSpaceRepository;
import com.knowledgehub.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KnowledgeSpaceService {

    private final KnowledgeSpaceRepository spaceRepository;
    private final UserRepository userRepository;

    public KnowledgeSpaceService(KnowledgeSpaceRepository spaceRepository, UserRepository userRepository) {
        this.spaceRepository = spaceRepository;
        this.userRepository = userRepository;
    }

    // Enforce multi-tenancy on creation: Space must be tied to a valid user
    public KnowledgeSpace createSpace(Long userId, String name, String description) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        KnowledgeSpace space = new KnowledgeSpace();
        space.setName(name);
        space.setDescription(description);
        space.setUser(user);

        return spaceRepository.save(space);
    }

    // Enforce multi-tenancy on retrieval: Only fetch spaces belonging to the exact
    // user
    public List<KnowledgeSpace> getUserSpaces(Long userId) {
        return spaceRepository.findByUserId(userId);
    }
}
