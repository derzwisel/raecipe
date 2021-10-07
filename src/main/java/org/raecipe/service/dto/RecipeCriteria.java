package org.raecipe.service.dto;

import java.io.Serializable;
import java.util.Objects;
import io.github.jhipster.service.Criteria;
import io.github.jhipster.service.filter.BooleanFilter;
import io.github.jhipster.service.filter.DoubleFilter;
import io.github.jhipster.service.filter.Filter;
import io.github.jhipster.service.filter.FloatFilter;
import io.github.jhipster.service.filter.IntegerFilter;
import io.github.jhipster.service.filter.LongFilter;
import io.github.jhipster.service.filter.StringFilter;
import io.github.jhipster.service.filter.DurationFilter;

/**
 * Criteria class for the {@link org.raecipe.domain.Recipe} entity. This class is used
 * in {@link org.raecipe.web.rest.RecipeResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /recipes?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
public class RecipeCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter name;

    private BooleanFilter starred;

    private StringFilter tags;

    private StringFilter ingredients;

    private StringFilter steps;

    private StringFilter comment;

    private DurationFilter duration;

    public RecipeCriteria() {
    }

    public RecipeCriteria(RecipeCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.name = other.name == null ? null : other.name.copy();
        this.starred = other.starred == null ? null : other.starred.copy();
        this.tags = other.tags == null ? null : other.tags.copy();
        this.ingredients = other.ingredients == null ? null : other.ingredients.copy();
        this.steps = other.steps == null ? null : other.steps.copy();
        this.comment = other.comment == null ? null : other.comment.copy();
        this.duration = other.duration == null ? null : other.duration.copy();
    }

    @Override
    public RecipeCriteria copy() {
        return new RecipeCriteria(this);
    }

    public LongFilter getId() {
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public StringFilter getName() {
        return name;
    }

    public void setName(StringFilter name) {
        this.name = name;
    }

    public BooleanFilter getStarred() {
        return starred;
    }

    public void setStarred(BooleanFilter starred) {
        this.starred = starred;
    }

    public StringFilter getTags() {
        return tags;
    }

    public void setTags(StringFilter tags) {
        this.tags = tags;
    }

    public StringFilter getIngredients() {
        return ingredients;
    }

    public void setIngredients(StringFilter ingredients) {
        this.ingredients = ingredients;
    }

    public StringFilter getSteps() {
        return steps;
    }

    public void setSteps(StringFilter steps) {
        this.steps = steps;
    }

    public StringFilter getComment() {
        return comment;
    }

    public void setComment(StringFilter comment) {
        this.comment = comment;
    }

    public DurationFilter getDuration() {
        return duration;
    }

    public void setDuration(DurationFilter duration) {
        this.duration = duration;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final RecipeCriteria that = (RecipeCriteria) o;
        return
            Objects.equals(id, that.id) &&
            Objects.equals(name, that.name) &&
            Objects.equals(starred, that.starred) &&
            Objects.equals(tags, that.tags) &&
            Objects.equals(ingredients, that.ingredients) &&
            Objects.equals(steps, that.steps) &&
            Objects.equals(comment, that.comment) &&
            Objects.equals(duration, that.duration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
        id,
        name,
        starred,
        tags,
        ingredients,
        steps,
        comment,
        duration
        );
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "RecipeCriteria{" +
                (id != null ? "id=" + id + ", " : "") +
                (name != null ? "name=" + name + ", " : "") +
                (starred != null ? "starred=" + starred + ", " : "") +
                (tags != null ? "tags=" + tags + ", " : "") +
                (ingredients != null ? "ingredients=" + ingredients + ", " : "") +
                (steps != null ? "steps=" + steps + ", " : "") +
                (comment != null ? "comment=" + comment + ", " : "") +
                (duration != null ? "duration=" + duration + ", " : "") +
            "}";
    }

}
