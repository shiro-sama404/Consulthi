package com.ThimoteoConsultorias.Consulthi.model;

import com.ThimoteoConsultorias.Consulthi.enums.ContentTag;
import com.ThimoteoConsultorias.Consulthi.model.embeddables.ContentBlock;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.DiscriminatorValue;

import java.util.List;
import java.util.Set;

@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
@Entity
@DiscriminatorValue("MATERIAL")
public class Material extends Content
{
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "material_tags", joinColumns = @JoinColumn(name = "material_id"))
    @Enumerated(EnumType.STRING)
    private Set<ContentTag> tags;

    @ElementCollection
    @CollectionTable(name = "material_blocks", joinColumns = @JoinColumn(name = "material_id"))
    @OrderColumn(name = "block_index")
    private List<ContentBlock> contentBlocks;
}