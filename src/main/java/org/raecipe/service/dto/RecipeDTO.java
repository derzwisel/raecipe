package org.raecipe.service.dto;

import java.time.Duration;
import javax.validation.constraints.*;
import java.io.Serializable;

/**
 * A DTO for the {@link org.raecipe.domain.Recipe} entity.
 */
public class RecipeDTO implements Serializable {
    
    private Long id;

    @NotNull
    private String name;

    private Duration duration;

    private String instructions;

    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RecipeDTO)) {
            return false;
        }

        return id != null && id.equals(((RecipeDTO) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "RecipeDTO{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", duration='" + getDuration() + "'" +
            ", instructions='" + getInstructions() + "'" +
            "}";
    }
}
