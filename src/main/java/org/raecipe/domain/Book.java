package org.raecipe.domain;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import javax.validation.constraints.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Book.
 */
@Entity
@Table(name = "book")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@org.springframework.data.elasticsearch.annotations.Document(indexName = "book")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Book implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "published")
    private Boolean published;

    @Column(name = "creator")
    private String creator;

    @Column(name = "creation_date")
    private ZonedDateTime creationDate;

    @Column(name = "update_date")
    private ZonedDateTime updateDate;

    @ManyToMany
    @JoinTable(name = "rel_book__recipe", joinColumns = @JoinColumn(name = "book_id"), inverseJoinColumns = @JoinColumn(name = "recipe_id"))
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private Set<Recipe> recipes = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Book id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public Book name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getPublished() {
        return this.published;
    }

    public Book published(Boolean published) {
        this.setPublished(published);
        return this;
    }

    public void setPublished(Boolean published) {
        this.published = published;
    }

    public String getCreator() {
        return this.creator;
    }

    public Book creator(String creator) {
        this.setCreator(creator);
        return this;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public ZonedDateTime getCreationDate() {
        return this.creationDate;
    }

    public Book creationDate(ZonedDateTime creationDate) {
        this.setCreationDate(creationDate);
        return this;
    }

    public void setCreationDate(ZonedDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public ZonedDateTime getUpdateDate() {
        return this.updateDate;
    }

    public Book updateDate(ZonedDateTime updateDate) {
        this.setUpdateDate(updateDate);
        return this;
    }

    public void setUpdateDate(ZonedDateTime updateDate) {
        this.updateDate = updateDate;
    }

    public Set<Recipe> getRecipes() {
        return this.recipes;
    }

    public void setRecipes(Set<Recipe> recipes) {
        this.recipes = recipes;
    }

    public Book recipes(Set<Recipe> recipes) {
        this.setRecipes(recipes);
        return this;
    }

    public Book addRecipe(Recipe recipe) {
        this.recipes.add(recipe);
        return this;
    }

    public Book removeRecipe(Recipe recipe) {
        this.recipes.remove(recipe);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Book)) {
            return false;
        }
        return id != null && id.equals(((Book) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Book{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", published='" + getPublished() + "'" +
            ", creator='" + getCreator() + "'" +
            ", creationDate='" + getCreationDate() + "'" +
            ", updateDate='" + getUpdateDate() + "'" +
            "}";
    }
}
