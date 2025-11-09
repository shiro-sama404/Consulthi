package com.ThimoteoConsultorias.Consulthi.enums;

public enum ExpertiseArea
{
    HIGH_PERFORMANCE_SPORTS,
    PHYSICAL_EDUCATION,
    PHYSIOTHERAPY,
    SPORTS,
    
    BEHAVIORAL_NUTRITION,
    CLINICAL_NUTRITION,
    HEALTH_NUTRITION,
    SPORTS_NUTRITION,
    
    CLINICAL_PSYCHOLOGY,
    EXERCISE_PSYCHOLOGY,
    EDUCACIONAL_PSYCHOLOGY,
    SPORTS_PSYCHOLOGY;

    public boolean isCoachRole()
    {
        return this == HIGH_PERFORMANCE_SPORTS || this == PHYSICAL_EDUCATION || this == PHYSIOTHERAPY || this == SPORTS;
    }

    public boolean isNutritionistRole()
    {
        return this == BEHAVIORAL_NUTRITION || this == CLINICAL_NUTRITION || this == HEALTH_NUTRITION || this == SPORTS_NUTRITION;
    }

    public boolean isPsychologistRole()
    {
        return this == CLINICAL_PSYCHOLOGY || this == EXERCISE_PSYCHOLOGY || this == EDUCACIONAL_PSYCHOLOGY || this == SPORTS_PSYCHOLOGY;
    }
}