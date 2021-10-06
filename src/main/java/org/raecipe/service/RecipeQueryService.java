package org.raecipe.service;

import java.util.List;

import javax.persistence.criteria.JoinType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.jhipster.service.QueryService;

import org.raecipe.domain.Recipe;
import org.raecipe.domain.*; // for static metamodels
import org.raecipe.repository.RecipeRepository;
import org.raecipe.repository.search.RecipeSearchRepository;
import org.raecipe.service.dto.RecipeCriteria;
import org.raecipe.service.dto.RecipeDTO;
import org.raecipe.service.mapper.RecipeMapper;

/**
 * Service for executing complex queries for {@link Recipe} entities in the database.
 * The main input is a {@link RecipeCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link RecipeDTO} or a {@link Page} of {@link RecipeDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class RecipeQueryService extends QueryService<Recipe> {

    private final Logger log = LoggerFactory.getLogger(RecipeQueryService.class);

    private final RecipeRepository recipeRepository;

    private final RecipeMapper recipeMapper;

    private final RecipeSearchRepository recipeSearchRepository;

    public RecipeQueryService(RecipeRepository recipeRepository, RecipeMapper recipeMapper, RecipeSearchRepository recipeSearchRepository) {
        this.recipeRepository = recipeRepository;
        this.recipeMapper = recipeMapper;
        this.recipeSearchRepository = recipeSearchRepository;
    }

    /**
     * Return a {@link List} of {@link RecipeDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<RecipeDTO> findByCriteria(RecipeCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<Recipe> specification = createSpecification(criteria);
        return recipeMapper.toDto(recipeRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link RecipeDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<RecipeDTO> findByCriteria(RecipeCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Recipe> specification = createSpecification(criteria);
        return recipeRepository.findAll(specification, page)
            .map(recipeMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(RecipeCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<Recipe> specification = createSpecification(criteria);
        return recipeRepository.count(specification);
    }

    /**
     * Function to convert {@link RecipeCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Recipe> createSpecification(RecipeCriteria criteria) {
        Specification<Recipe> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), Recipe_.id));
            }
            if (criteria.getName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getName(), Recipe_.name));
            }
            if (criteria.getStarred() != null) {
                specification = specification.and(buildSpecification(criteria.getStarred(), Recipe_.starred));
            }
            if (criteria.getTags() != null) {
                specification = specification.and(buildStringSpecification(criteria.getTags(), Recipe_.tags));
            }
            if (criteria.getIngredients() != null) {
                specification = specification.and(buildStringSpecification(criteria.getIngredients(), Recipe_.ingredients));
            }
            if (criteria.getSteps() != null) {
                specification = specification.and(buildStringSpecification(criteria.getSteps(), Recipe_.steps));
            }
            if (criteria.getComment() != null) {
                specification = specification.and(buildStringSpecification(criteria.getComment(), Recipe_.comment));
            }
            if (criteria.getDuration() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getDuration(), Recipe_.duration));
            }
        }
        return specification;
    }
}
