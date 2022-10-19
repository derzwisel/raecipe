package org.raecipe.service;

import static org.elasticsearch.index.query.QueryBuilders.*;

import java.util.Optional;
import org.raecipe.domain.Recipe;
import org.raecipe.repository.RecipeRepository;
import org.raecipe.repository.search.RecipeSearchRepository;
import org.raecipe.service.dto.RecipeDTO;
import org.raecipe.service.mapper.RecipeMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link Recipe}.
 */
@Service
@Transactional
public class RecipeService {

    private final Logger log = LoggerFactory.getLogger(RecipeService.class);

    private final RecipeRepository recipeRepository;

    private final RecipeMapper recipeMapper;

    private final RecipeSearchRepository recipeSearchRepository;

    public RecipeService(RecipeRepository recipeRepository, RecipeMapper recipeMapper, RecipeSearchRepository recipeSearchRepository) {
        this.recipeRepository = recipeRepository;
        this.recipeMapper = recipeMapper;
        this.recipeSearchRepository = recipeSearchRepository;
    }

    /**
     * Save a recipe.
     *
     * @param recipeDTO the entity to save.
     * @return the persisted entity.
     */
    public RecipeDTO save(RecipeDTO recipeDTO) {
        log.debug("Request to save Recipe : {}", recipeDTO);
        Recipe recipe = recipeMapper.toEntity(recipeDTO);
        recipe = recipeRepository.save(recipe);
        RecipeDTO result = recipeMapper.toDto(recipe);
        recipeSearchRepository.index(recipe);
        return result;
    }

    /**
     * Update a recipe.
     *
     * @param recipeDTO the entity to save.
     * @return the persisted entity.
     */
    public RecipeDTO update(RecipeDTO recipeDTO) {
        log.debug("Request to update Recipe : {}", recipeDTO);
        Recipe recipe = recipeMapper.toEntity(recipeDTO);
        recipe = recipeRepository.save(recipe);
        RecipeDTO result = recipeMapper.toDto(recipe);
        recipeSearchRepository.index(recipe);
        return result;
    }

    /**
     * Partially update a recipe.
     *
     * @param recipeDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<RecipeDTO> partialUpdate(RecipeDTO recipeDTO) {
        log.debug("Request to partially update Recipe : {}", recipeDTO);

        return recipeRepository
            .findById(recipeDTO.getId())
            .map(existingRecipe -> {
                recipeMapper.partialUpdate(existingRecipe, recipeDTO);

                return existingRecipe;
            })
            .map(recipeRepository::save)
            .map(savedRecipe -> {
                recipeSearchRepository.save(savedRecipe);

                return savedRecipe;
            })
            .map(recipeMapper::toDto);
    }

    /**
     * Get all the recipes.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<RecipeDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Recipes");
        return recipeRepository.findAll(pageable).map(recipeMapper::toDto);
    }

    /**
     * Get one recipe by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<RecipeDTO> findOne(Long id) {
        log.debug("Request to get Recipe : {}", id);
        return recipeRepository.findById(id).map(recipeMapper::toDto);
    }

    /**
     * Delete the recipe by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete Recipe : {}", id);
        recipeRepository.deleteById(id);
        recipeSearchRepository.deleteById(id);
    }

    /**
     * Search for the recipe corresponding to the query.
     *
     * @param query the query of the search.
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<RecipeDTO> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of Recipes for query {}", query);
        return recipeSearchRepository.search(query, pageable).map(recipeMapper::toDto);
    }
}
