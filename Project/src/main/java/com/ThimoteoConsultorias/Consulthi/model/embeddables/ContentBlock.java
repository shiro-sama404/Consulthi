package com.ThimoteoConsultorias.Consulthi.model.embeddables;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.ThimoteoConsultorias.Consulthi.enums.ContentBlockType;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ContentBlock
{
    private Integer blockOrder;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContentBlockType type;

    @Column(columnDefinition = "TEXT")
    private String value;
}