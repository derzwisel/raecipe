package org.raecipe.domain;

import java.io.Serializable;
import java.time.Duration;
import javax.persistence.*;
import javax.validation.constraints.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Recipe.
 */
@Entity
@Table(name = "recipe")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@org.springframework.data.elasticsearch.annotations.Document(indexName = "recipe")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Recipe implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "starred")
    private Boolean starred;

    @Column(name = "tags")
    private String tags;

    @Column(name = "ingredients")
    private String ingredients;

    @Column(name = "steps")
    private String steps;

    @Column(name = "comment")
    private String comment;

    @Column(name = "duration")
    private Duration duration;

    @Column(name = "pictures")
    private String pictures;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Recipe id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public Recipe name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getStarred() {
        return this.starred;
    }

    public Recipe starred(Boolean starred) {
        this.setStarred(starred);
        return this;
    }

    public void setStarred(Boolean starred) {
        this.starred = starred;
    }

    public String getTags() {
        return this.tags;
    }

    public Recipe tags(String tags) {
        this.setTags(tags);
        return this;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getIngredients() {
        return this.ingredients;
    }

    public Recipe ingredients(String ingredients) {
        this.setIngredients(ingredients);
        return this;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public String getSteps() {
        return this.steps;
    }

    public Recipe steps(String steps) {
        this.setSteps(steps);
        return this;
    }

    public void setSteps(String steps) {
        this.steps = steps;
    }

    public String getComment() {
        return this.comment;
    }

    public Recipe comment(String comment) {
        this.setComment(comment);
        return this;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Duration getDuration() {
        return this.duration;
    }

    public Recipe duration(Duration duration) {
        this.setDuration(duration);
        return this;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public String getPictures() {
        return this.pictures;
    }

    public Recipe pictures(String pictures) {
        this.setPictures(pictures);
        return this;
    }

    public void setPictures(String pictures) {
        this.pictures = pictures;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Recipe)) {
            return false;
        }
        return id != null && id.equals(((Recipe) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Recipe{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", starred='" + getStarred() + "'" +
            ", tags='" + getTags() + "'" +
            ", ingredients='" + getIngredients() + "'" +
            ", steps='" + getSteps() + "'" +
            ", comment='" + getComment() + "'" +
            ", duration='" + getDuration() + "'" +
            ", pictures='" + getPictures() + "'" +
            "}";
    }
}
