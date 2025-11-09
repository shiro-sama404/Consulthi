package com.ThimoteoConsultorias.Consulthi.repository;

import com.ThimoteoConsultorias.Consulthi.model.Content;
import com.ThimoteoConsultorias.Consulthi.model.Diet;
import com.ThimoteoConsultorias.Consulthi.model.Material;
import com.ThimoteoConsultorias.Consulthi.model.Routine;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContentRepository extends JpaRepository<Content, Long>
{
    List<Content> findByCreatorId(Long creatorId);
    Optional<Content> findByIdAndCreatorId(Long contentId, Long creatorId);

    // ======== Polimórficas ========
    List<Diet> findDietsByCreatorId(Long creatorId);
    Optional<Diet> findDietByIdAndCreatorId(Long contentId, Long creatorId);

    List<Material> findMaterialsByCreatorId(Long creatorId);
    Optional<Material> findMaterialByIdAndCreatorId(Long contentId, Long creatorId);

    List<Routine> findRoutinesByCreatorId(Long creatorId);
    Optional<Routine> findRoutineByIdAndCreatorId(Long contentId, Long creatorId);
}