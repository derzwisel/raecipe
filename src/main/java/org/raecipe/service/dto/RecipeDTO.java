package org.raecipe.service.dto;

import javax.validation.constraints.*;
import java.io.Serializable;

/**
 * A DTO for the {@link org.raecipe.domain.Recipe} entity.
 */
public class RecipeDTO implements Serializable {
    
    private Long id;

    @NotNull
    private String name;

    private Boolean starred;

    private String tags;

    private String ingredients;

    private String steps;

    private String comment;

    
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

    public Boolean isStarred() {
        return starred;
    }

    public void setStarred(Boolean starred) {
        this.starred = starred;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public String getSteps() {
        return steps;
    }

    public void setSteps(String steps) {
        this.steps = steps;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
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
            ", starred='" + isStarred() + "'" +
            ", tags='" + getTags() + "'" +
            ", ingredients='" + getIngredients() + "'" +
            ", steps='" + getSteps() + "'" +
            ", comment='" + getComment() + "'" +
            "}";
    }
}
