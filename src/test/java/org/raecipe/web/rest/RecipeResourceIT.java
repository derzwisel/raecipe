package org.raecipe.web.rest;

import org.raecipe.RaecipeApp;
import org.raecipe.domain.Recipe;
import org.raecipe.repository.RecipeRepository;
import org.raecipe.repository.search.RecipeSearchRepository;
import org.raecipe.service.RecipeService;
import org.raecipe.service.dto.RecipeDTO;
import org.raecipe.service.mapper.RecipeMapper;
import org.raecipe.service.dto.RecipeCriteria;
import org.raecipe.service.RecipeQueryService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityManager;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link RecipeResource} REST controller.
 */
@SpringBootTest(classes = RaecipeApp.class)
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
public class RecipeResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final Duration DEFAULT_DURATION = Duration.ofHours(6);
    private static final Duration UPDATED_DURATION = Duration.ofHours(12);
    private static final Duration SMALLER_DURATION = Duration.ofHours(5);

    private static final String DEFAULT_INSTRUCTIONS = "AAAAAAAAAA";
    private static final String UPDATED_INSTRUCTIONS = "BBBBBBBBBB";

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private RecipeMapper recipeMapper;

    @Autowired
    private RecipeService recipeService;

    /**
     * This repository is mocked in the org.raecipe.repository.search test package.
     *
     * @see org.raecipe.repository.search.RecipeSearchRepositoryMockConfiguration
     */
    @Autowired
    private RecipeSearchRepository mockRecipeSearchRepository;

    @Autowired
    private RecipeQueryService recipeQueryService;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restRecipeMockMvc;

    private Recipe recipe;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Recipe createEntity(EntityManager em) {
        Recipe recipe = new Recipe()
            .name(DEFAULT_NAME)
            .duration(DEFAULT_DURATION)
            .instructions(DEFAULT_INSTRUCTIONS);
        return recipe;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Recipe createUpdatedEntity(EntityManager em) {
        Recipe recipe = new Recipe()
            .name(UPDATED_NAME)
            .duration(UPDATED_DURATION)
            .instructions(UPDATED_INSTRUCTIONS);
        return recipe;
    }

    @BeforeEach
    public void initTest() {
        recipe = createEntity(em);
    }

    @Test
    @Transactional
    public void createRecipe() throws Exception {
        int databaseSizeBeforeCreate = recipeRepository.findAll().size();
        // Create the Recipe
        RecipeDTO recipeDTO = recipeMapper.toDto(recipe);
        restRecipeMockMvc.perform(post("/api/recipes")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(recipeDTO)))
            .andExpect(status().isCreated());

        // Validate the Recipe in the database
        List<Recipe> recipeList = recipeRepository.findAll();
        assertThat(recipeList).hasSize(databaseSizeBeforeCreate + 1);
        Recipe testRecipe = recipeList.get(recipeList.size() - 1);
        assertThat(testRecipe.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testRecipe.getDuration()).isEqualTo(DEFAULT_DURATION);
        assertThat(testRecipe.getInstructions()).isEqualTo(DEFAULT_INSTRUCTIONS);

        // Validate the Recipe in Elasticsearch
        verify(mockRecipeSearchRepository, times(1)).save(testRecipe);
    }

    @Test
    @Transactional
    public void createRecipeWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = recipeRepository.findAll().size();

        // Create the Recipe with an existing ID
        recipe.setId(1L);
        RecipeDTO recipeDTO = recipeMapper.toDto(recipe);

        // An entity with an existing ID cannot be created, so this API call must fail
        restRecipeMockMvc.perform(post("/api/recipes")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(recipeDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Recipe in the database
        List<Recipe> recipeList = recipeRepository.findAll();
        assertThat(recipeList).hasSize(databaseSizeBeforeCreate);

        // Validate the Recipe in Elasticsearch
        verify(mockRecipeSearchRepository, times(0)).save(recipe);
    }


    @Test
    @Transactional
    public void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = recipeRepository.findAll().size();
        // set the field null
        recipe.setName(null);

        // Create the Recipe, which fails.
        RecipeDTO recipeDTO = recipeMapper.toDto(recipe);


        restRecipeMockMvc.perform(post("/api/recipes")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(recipeDTO)))
            .andExpect(status().isBadRequest());

        List<Recipe> recipeList = recipeRepository.findAll();
        assertThat(recipeList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllRecipes() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList
        restRecipeMockMvc.perform(get("/api/recipes?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(recipe.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].duration").value(hasItem(DEFAULT_DURATION.toString())))
            .andExpect(jsonPath("$.[*].instructions").value(hasItem(DEFAULT_INSTRUCTIONS)));
    }
    
    @Test
    @Transactional
    public void getRecipe() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get the recipe
        restRecipeMockMvc.perform(get("/api/recipes/{id}", recipe.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(recipe.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.duration").value(DEFAULT_DURATION.toString()))
            .andExpect(jsonPath("$.instructions").value(DEFAULT_INSTRUCTIONS));
    }


    @Test
    @Transactional
    public void getRecipesByIdFiltering() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        Long id = recipe.getId();

        defaultRecipeShouldBeFound("id.equals=" + id);
        defaultRecipeShouldNotBeFound("id.notEquals=" + id);

        defaultRecipeShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultRecipeShouldNotBeFound("id.greaterThan=" + id);

        defaultRecipeShouldBeFound("id.lessThanOrEqual=" + id);
        defaultRecipeShouldNotBeFound("id.lessThan=" + id);
    }


    @Test
    @Transactional
    public void getAllRecipesByNameIsEqualToSomething() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where name equals to DEFAULT_NAME
        defaultRecipeShouldBeFound("name.equals=" + DEFAULT_NAME);

        // Get all the recipeList where name equals to UPDATED_NAME
        defaultRecipeShouldNotBeFound("name.equals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    public void getAllRecipesByNameIsNotEqualToSomething() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where name not equals to DEFAULT_NAME
        defaultRecipeShouldNotBeFound("name.notEquals=" + DEFAULT_NAME);

        // Get all the recipeList where name not equals to UPDATED_NAME
        defaultRecipeShouldBeFound("name.notEquals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    public void getAllRecipesByNameIsInShouldWork() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where name in DEFAULT_NAME or UPDATED_NAME
        defaultRecipeShouldBeFound("name.in=" + DEFAULT_NAME + "," + UPDATED_NAME);

        // Get all the recipeList where name equals to UPDATED_NAME
        defaultRecipeShouldNotBeFound("name.in=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    public void getAllRecipesByNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where name is not null
        defaultRecipeShouldBeFound("name.specified=true");

        // Get all the recipeList where name is null
        defaultRecipeShouldNotBeFound("name.specified=false");
    }
                @Test
    @Transactional
    public void getAllRecipesByNameContainsSomething() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where name contains DEFAULT_NAME
        defaultRecipeShouldBeFound("name.contains=" + DEFAULT_NAME);

        // Get all the recipeList where name contains UPDATED_NAME
        defaultRecipeShouldNotBeFound("name.contains=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    public void getAllRecipesByNameNotContainsSomething() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where name does not contain DEFAULT_NAME
        defaultRecipeShouldNotBeFound("name.doesNotContain=" + DEFAULT_NAME);

        // Get all the recipeList where name does not contain UPDATED_NAME
        defaultRecipeShouldBeFound("name.doesNotContain=" + UPDATED_NAME);
    }


    @Test
    @Transactional
    public void getAllRecipesByDurationIsEqualToSomething() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where duration equals to DEFAULT_DURATION
        defaultRecipeShouldBeFound("duration.equals=" + DEFAULT_DURATION);

        // Get all the recipeList where duration equals to UPDATED_DURATION
        defaultRecipeShouldNotBeFound("duration.equals=" + UPDATED_DURATION);
    }

    @Test
    @Transactional
    public void getAllRecipesByDurationIsNotEqualToSomething() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where duration not equals to DEFAULT_DURATION
        defaultRecipeShouldNotBeFound("duration.notEquals=" + DEFAULT_DURATION);

        // Get all the recipeList where duration not equals to UPDATED_DURATION
        defaultRecipeShouldBeFound("duration.notEquals=" + UPDATED_DURATION);
    }

    @Test
    @Transactional
    public void getAllRecipesByDurationIsInShouldWork() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where duration in DEFAULT_DURATION or UPDATED_DURATION
        defaultRecipeShouldBeFound("duration.in=" + DEFAULT_DURATION + "," + UPDATED_DURATION);

        // Get all the recipeList where duration equals to UPDATED_DURATION
        defaultRecipeShouldNotBeFound("duration.in=" + UPDATED_DURATION);
    }

    @Test
    @Transactional
    public void getAllRecipesByDurationIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where duration is not null
        defaultRecipeShouldBeFound("duration.specified=true");

        // Get all the recipeList where duration is null
        defaultRecipeShouldNotBeFound("duration.specified=false");
    }

    @Test
    @Transactional
    public void getAllRecipesByDurationIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where duration is greater than or equal to DEFAULT_DURATION
        defaultRecipeShouldBeFound("duration.greaterThanOrEqual=" + DEFAULT_DURATION);

        // Get all the recipeList where duration is greater than or equal to UPDATED_DURATION
        defaultRecipeShouldNotBeFound("duration.greaterThanOrEqual=" + UPDATED_DURATION);
    }

    @Test
    @Transactional
    public void getAllRecipesByDurationIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where duration is less than or equal to DEFAULT_DURATION
        defaultRecipeShouldBeFound("duration.lessThanOrEqual=" + DEFAULT_DURATION);

        // Get all the recipeList where duration is less than or equal to SMALLER_DURATION
        defaultRecipeShouldNotBeFound("duration.lessThanOrEqual=" + SMALLER_DURATION);
    }

    @Test
    @Transactional
    public void getAllRecipesByDurationIsLessThanSomething() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where duration is less than DEFAULT_DURATION
        defaultRecipeShouldNotBeFound("duration.lessThan=" + DEFAULT_DURATION);

        // Get all the recipeList where duration is less than UPDATED_DURATION
        defaultRecipeShouldBeFound("duration.lessThan=" + UPDATED_DURATION);
    }

    @Test
    @Transactional
    public void getAllRecipesByDurationIsGreaterThanSomething() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where duration is greater than DEFAULT_DURATION
        defaultRecipeShouldNotBeFound("duration.greaterThan=" + DEFAULT_DURATION);

        // Get all the recipeList where duration is greater than SMALLER_DURATION
        defaultRecipeShouldBeFound("duration.greaterThan=" + SMALLER_DURATION);
    }


    @Test
    @Transactional
    public void getAllRecipesByInstructionsIsEqualToSomething() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where instructions equals to DEFAULT_INSTRUCTIONS
        defaultRecipeShouldBeFound("instructions.equals=" + DEFAULT_INSTRUCTIONS);

        // Get all the recipeList where instructions equals to UPDATED_INSTRUCTIONS
        defaultRecipeShouldNotBeFound("instructions.equals=" + UPDATED_INSTRUCTIONS);
    }

    @Test
    @Transactional
    public void getAllRecipesByInstructionsIsNotEqualToSomething() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where instructions not equals to DEFAULT_INSTRUCTIONS
        defaultRecipeShouldNotBeFound("instructions.notEquals=" + DEFAULT_INSTRUCTIONS);

        // Get all the recipeList where instructions not equals to UPDATED_INSTRUCTIONS
        defaultRecipeShouldBeFound("instructions.notEquals=" + UPDATED_INSTRUCTIONS);
    }

    @Test
    @Transactional
    public void getAllRecipesByInstructionsIsInShouldWork() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where instructions in DEFAULT_INSTRUCTIONS or UPDATED_INSTRUCTIONS
        defaultRecipeShouldBeFound("instructions.in=" + DEFAULT_INSTRUCTIONS + "," + UPDATED_INSTRUCTIONS);

        // Get all the recipeList where instructions equals to UPDATED_INSTRUCTIONS
        defaultRecipeShouldNotBeFound("instructions.in=" + UPDATED_INSTRUCTIONS);
    }

    @Test
    @Transactional
    public void getAllRecipesByInstructionsIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where instructions is not null
        defaultRecipeShouldBeFound("instructions.specified=true");

        // Get all the recipeList where instructions is null
        defaultRecipeShouldNotBeFound("instructions.specified=false");
    }
                @Test
    @Transactional
    public void getAllRecipesByInstructionsContainsSomething() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where instructions contains DEFAULT_INSTRUCTIONS
        defaultRecipeShouldBeFound("instructions.contains=" + DEFAULT_INSTRUCTIONS);

        // Get all the recipeList where instructions contains UPDATED_INSTRUCTIONS
        defaultRecipeShouldNotBeFound("instructions.contains=" + UPDATED_INSTRUCTIONS);
    }

    @Test
    @Transactional
    public void getAllRecipesByInstructionsNotContainsSomething() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where instructions does not contain DEFAULT_INSTRUCTIONS
        defaultRecipeShouldNotBeFound("instructions.doesNotContain=" + DEFAULT_INSTRUCTIONS);

        // Get all the recipeList where instructions does not contain UPDATED_INSTRUCTIONS
        defaultRecipeShouldBeFound("instructions.doesNotContain=" + UPDATED_INSTRUCTIONS);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultRecipeShouldBeFound(String filter) throws Exception {
        restRecipeMockMvc.perform(get("/api/recipes?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(recipe.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].duration").value(hasItem(DEFAULT_DURATION.toString())))
            .andExpect(jsonPath("$.[*].instructions").value(hasItem(DEFAULT_INSTRUCTIONS)));

        // Check, that the count call also returns 1
        restRecipeMockMvc.perform(get("/api/recipes/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultRecipeShouldNotBeFound(String filter) throws Exception {
        restRecipeMockMvc.perform(get("/api/recipes?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restRecipeMockMvc.perform(get("/api/recipes/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    public void getNonExistingRecipe() throws Exception {
        // Get the recipe
        restRecipeMockMvc.perform(get("/api/recipes/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateRecipe() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        int databaseSizeBeforeUpdate = recipeRepository.findAll().size();

        // Update the recipe
        Recipe updatedRecipe = recipeRepository.findById(recipe.getId()).get();
        // Disconnect from session so that the updates on updatedRecipe are not directly saved in db
        em.detach(updatedRecipe);
        updatedRecipe
            .name(UPDATED_NAME)
            .duration(UPDATED_DURATION)
            .instructions(UPDATED_INSTRUCTIONS);
        RecipeDTO recipeDTO = recipeMapper.toDto(updatedRecipe);

        restRecipeMockMvc.perform(put("/api/recipes")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(recipeDTO)))
            .andExpect(status().isOk());

        // Validate the Recipe in the database
        List<Recipe> recipeList = recipeRepository.findAll();
        assertThat(recipeList).hasSize(databaseSizeBeforeUpdate);
        Recipe testRecipe = recipeList.get(recipeList.size() - 1);
        assertThat(testRecipe.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testRecipe.getDuration()).isEqualTo(UPDATED_DURATION);
        assertThat(testRecipe.getInstructions()).isEqualTo(UPDATED_INSTRUCTIONS);

        // Validate the Recipe in Elasticsearch
        verify(mockRecipeSearchRepository, times(1)).save(testRecipe);
    }

    @Test
    @Transactional
    public void updateNonExistingRecipe() throws Exception {
        int databaseSizeBeforeUpdate = recipeRepository.findAll().size();

        // Create the Recipe
        RecipeDTO recipeDTO = recipeMapper.toDto(recipe);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restRecipeMockMvc.perform(put("/api/recipes")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(recipeDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Recipe in the database
        List<Recipe> recipeList = recipeRepository.findAll();
        assertThat(recipeList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Recipe in Elasticsearch
        verify(mockRecipeSearchRepository, times(0)).save(recipe);
    }

    @Test
    @Transactional
    public void deleteRecipe() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        int databaseSizeBeforeDelete = recipeRepository.findAll().size();

        // Delete the recipe
        restRecipeMockMvc.perform(delete("/api/recipes/{id}", recipe.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Recipe> recipeList = recipeRepository.findAll();
        assertThat(recipeList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the Recipe in Elasticsearch
        verify(mockRecipeSearchRepository, times(1)).deleteById(recipe.getId());
    }

    @Test
    @Transactional
    public void searchRecipe() throws Exception {
        // Configure the mock search repository
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);
        when(mockRecipeSearchRepository.search(queryStringQuery("id:" + recipe.getId()), PageRequest.of(0, 20)))
            .thenReturn(new PageImpl<>(Collections.singletonList(recipe), PageRequest.of(0, 1), 1));

        // Search the recipe
        restRecipeMockMvc.perform(get("/api/_search/recipes?query=id:" + recipe.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(recipe.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].duration").value(hasItem(DEFAULT_DURATION.toString())))
            .andExpect(jsonPath("$.[*].instructions").value(hasItem(DEFAULT_INSTRUCTIONS)));
    }
}
