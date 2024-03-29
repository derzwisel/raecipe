package org.raecipe.service.criteria;

import java.io.Serializable;
import java.util.Objects;
import org.springdoc.api.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link org.raecipe.domain.Book} entity. This class is used
 * in {@link org.raecipe.web.rest.BookResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /books?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class BookCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter name;

    private BooleanFilter published;

    private StringFilter creator;

    private ZonedDateTimeFilter creationDate;

    private ZonedDateTimeFilter updateDate;

    private LongFilter recipeId;

    private Boolean distinct;

    public BookCriteria() {}

    public BookCriteria(BookCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.name = other.name == null ? null : other.name.copy();
        this.published = other.published == null ? null : other.published.copy();
        this.creator = other.creator == null ? null : other.creator.copy();
        this.creationDate = other.creationDate == null ? null : other.creationDate.copy();
        this.updateDate = other.updateDate == null ? null : other.updateDate.copy();
        this.recipeId = other.recipeId == null ? null : other.recipeId.copy();
        this.distinct = other.distinct;
    }

    @Override
    public BookCriteria copy() {
        return new BookCriteria(this);
    }

    public LongFilter getId() {
        return id;
    }

    public LongFilter id() {
        if (id == null) {
            id = new LongFilter();
        }
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public StringFilter getName() {
        return name;
    }

    public StringFilter name() {
        if (name == null) {
            name = new StringFilter();
        }
        return name;
    }

    public void setName(StringFilter name) {
        this.name = name;
    }

    public BooleanFilter getPublished() {
        return published;
    }

    public BooleanFilter published() {
        if (published == null) {
            published = new BooleanFilter();
        }
        return published;
    }

    public void setPublished(BooleanFilter published) {
        this.published = published;
    }

    public StringFilter getCreator() {
        return creator;
    }

    public StringFilter creator() {
        if (creator == null) {
            creator = new StringFilter();
        }
        return creator;
    }

    public void setCreator(StringFilter creator) {
        this.creator = creator;
    }

    public ZonedDateTimeFilter getCreationDate() {
        return creationDate;
    }

    public ZonedDateTimeFilter creationDate() {
        if (creationDate == null) {
            creationDate = new ZonedDateTimeFilter();
        }
        return creationDate;
    }

    public void setCreationDate(ZonedDateTimeFilter creationDate) {
        this.creationDate = creationDate;
    }

    public ZonedDateTimeFilter getUpdateDate() {
        return updateDate;
    }

    public ZonedDateTimeFilter updateDate() {
        if (updateDate == null) {
            updateDate = new ZonedDateTimeFilter();
        }
        return updateDate;
    }

    public void setUpdateDate(ZonedDateTimeFilter updateDate) {
        this.updateDate = updateDate;
    }

    public LongFilter getRecipeId() {
        return recipeId;
    }

    public LongFilter recipeId() {
        if (recipeId == null) {
            recipeId = new LongFilter();
        }
        return recipeId;
    }

    public void setRecipeId(LongFilter recipeId) {
        this.recipeId = recipeId;
    }

    public Boolean getDistinct() {
        return distinct;
    }

    public void setDistinct(Boolean distinct) {
        this.distinct = distinct;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final BookCriteria that = (BookCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(name, that.name) &&
            Objects.equals(published, that.published) &&
            Objects.equals(creator, that.creator) &&
            Objects.equals(creationDate, that.creationDate) &&
            Objects.equals(updateDate, that.updateDate) &&
            Objects.equals(recipeId, that.recipeId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, published, creator, creationDate, updateDate, recipeId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "BookCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (name != null ? "name=" + name + ", " : "") +
            (published != null ? "published=" + published + ", " : "") +
            (creator != null ? "creator=" + creator + ", " : "") +
            (creationDate != null ? "creationDate=" + creationDate + ", " : "") +
            (updateDate != null ? "updateDate=" + updateDate + ", " : "") +
            (recipeId != null ? "recipeId=" + recipeId + ", " : "") +
            (distinct != null ? "distinct=" + distinct + ", " : "") +
            "}";
    }
}
