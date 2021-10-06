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

    private static final Boolean DEFAULT_STARRED = false;
    private static final Boolean UPDATED_STARRED = true;

    private static final String DEFAULT_TAGS = "AAAAAAAAAA";
    private static final String UPDATED_TAGS = "BBBBBBBBBB";

    private static final String DEFAULT_INGREDIENTS = "AAAAAAAAAA";
    private static final String UPDATED_INGREDIENTS = "BBBBBBBBBB";

    private static final String DEFAULT_STEPS = "AAAAAAAAAA";
    private static final String UPDATED_STEPS = "BBBBBBBBBB";

    private static final String DEFAULT_COMMENT = "AAAAAAAAAA";
    private static final String UPDATED_COMMENT = "BBBBBBBBBB";

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
            .starred(DEFAULT_STARRED)
            .tags(DEFAULT_TAGS)
            .ingredients(DEFAULT_INGREDIENTS)
            .steps(DEFAULT_STEPS)
            .comment(DEFAULT_COMMENT);
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
            .starred(UPDATED_STARRED)
            .tags(UPDATED_TAGS)
            .ingredients(UPDATED_INGREDIENTS)
            .steps(UPDATED_STEPS)
            .comment(UPDATED_COMMENT);
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
        assertThat(testRecipe.isStarred()).isEqualTo(DEFAULT_STARRED);
        assertThat(testRecipe.getTags()).isEqualTo(DEFAULT_TAGS);
        assertThat(testRecipe.getIngredients()).isEqualTo(DEFAULT_INGREDIENTS);
        assertThat(testRecipe.getSteps()).isEqualTo(DEFAULT_STEPS);
        assertThat(testRecipe.getComment()).isEqualTo(DEFAULT_COMMENT);

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
            .andExpect(jsonPath("$.[*].starred").value(hasItem(DEFAULT_STARRED.booleanValue())))
            .andExpect(jsonPath("$.[*].tags").value(hasItem(DEFAULT_TAGS)))
            .andExpect(jsonPath("$.[*].ingredients").value(hasItem(DEFAULT_INGREDIENTS)))
            .andExpect(jsonPath("$.[*].steps").value(hasItem(DEFAULT_STEPS)))
            .andExpect(jsonPath("$.[*].comment").value(hasItem(DEFAULT_COMMENT)));
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
            .andExpect(jsonPath("$.starred").value(DEFAULT_STARRED.booleanValue()))
            .andExpect(jsonPath("$.tags").value(DEFAULT_TAGS))
            .andExpect(jsonPath("$.ingredients").value(DEFAULT_INGREDIENTS))
            .andExpect(jsonPath("$.steps").value(DEFAULT_STEPS))
            .andExpect(jsonPath("$.comment").value(DEFAULT_COMMENT));
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
    public void getAllRecipesByStarredIsEqualToSomething() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where starred equals to DEFAULT_STARRED
        defaultRecipeShouldBeFound("starred.equals=" + DEFAULT_STARRED);

        // Get all the recipeList where starred equals to UPDATED_STARRED
        defaultRecipeShouldNotBeFound("starred.equals=" + UPDATED_STARRED);
    }

    @Test
    @Transactional
    public void getAllRecipesByStarredIsNotEqualToSomething() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where starred not equals to DEFAULT_STARRED
        defaultRecipeShouldNotBeFound("starred.notEquals=" + DEFAULT_STARRED);

        // Get all the recipeList where starred not equals to UPDATED_STARRED
        defaultRecipeShouldBeFound("starred.notEquals=" + UPDATED_STARRED);
    }

    @Test
    @Transactional
    public void getAllRecipesByStarredIsInShouldWork() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where starred in DEFAULT_STARRED or UPDATED_STARRED
        defaultRecipeShouldBeFound("starred.in=" + DEFAULT_STARRED + "," + UPDATED_STARRED);

        // Get all the recipeList where starred equals to UPDATED_STARRED
        defaultRecipeShouldNotBeFound("starred.in=" + UPDATED_STARRED);
    }

    @Test
    @Transactional
    public void getAllRecipesByStarredIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where starred is not null
        defaultRecipeShouldBeFound("starred.specified=true");

        // Get all the recipeList where starred is null
        defaultRecipeShouldNotBeFound("starred.specified=false");
    }

    @Test
    @Transactional
    public void getAllRecipesByTagsIsEqualToSomething() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where tags equals to DEFAULT_TAGS
        defaultRecipeShouldBeFound("tags.equals=" + DEFAULT_TAGS);

        // Get all the recipeList where tags equals to UPDATED_TAGS
        defaultRecipeShouldNotBeFound("tags.equals=" + UPDATED_TAGS);
    }

    @Test
    @Transactional
    public void getAllRecipesByTagsIsNotEqualToSomething() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where tags not equals to DEFAULT_TAGS
        defaultRecipeShouldNotBeFound("tags.notEquals=" + DEFAULT_TAGS);

        // Get all the recipeList where tags not equals to UPDATED_TAGS
        defaultRecipeShouldBeFound("tags.notEquals=" + UPDATED_TAGS);
    }

    @Test
    @Transactional
    public void getAllRecipesByTagsIsInShouldWork() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where tags in DEFAULT_TAGS or UPDATED_TAGS
        defaultRecipeShouldBeFound("tags.in=" + DEFAULT_TAGS + "," + UPDATED_TAGS);

        // Get all the recipeList where tags equals to UPDATED_TAGS
        defaultRecipeShouldNotBeFound("tags.in=" + UPDATED_TAGS);
    }

    @Test
    @Transactional
    public void getAllRecipesByTagsIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where tags is not null
        defaultRecipeShouldBeFound("tags.specified=true");

        // Get all the recipeList where tags is null
        defaultRecipeShouldNotBeFound("tags.specified=false");
    }
                @Test
    @Transactional
    public void getAllRecipesByTagsContainsSomething() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where tags contains DEFAULT_TAGS
        defaultRecipeShouldBeFound("tags.contains=" + DEFAULT_TAGS);

        // Get all the recipeList where tags contains UPDATED_TAGS
        defaultRecipeShouldNotBeFound("tags.contains=" + UPDATED_TAGS);
    }

    @Test
    @Transactional
    public void getAllRecipesByTagsNotContainsSomething() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where tags does not contain DEFAULT_TAGS
        defaultRecipeShouldNotBeFound("tags.doesNotContain=" + DEFAULT_TAGS);

        // Get all the recipeList where tags does not contain UPDATED_TAGS
        defaultRecipeShouldBeFound("tags.doesNotContain=" + UPDATED_TAGS);
    }


    @Test
    @Transactional
    public void getAllRecipesByIngredientsIsEqualToSomething() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where ingredients equals to DEFAULT_INGREDIENTS
        defaultRecipeShouldBeFound("ingredients.equals=" + DEFAULT_INGREDIENTS);

        // Get all the recipeList where ingredients equals to UPDATED_INGREDIENTS
        defaultRecipeShouldNotBeFound("ingredients.equals=" + UPDATED_INGREDIENTS);
    }

    @Test
    @Transactional
    public void getAllRecipesByIngredientsIsNotEqualToSomething() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where ingredients not equals to DEFAULT_INGREDIENTS
        defaultRecipeShouldNotBeFound("ingredients.notEquals=" + DEFAULT_INGREDIENTS);

        // Get all the recipeList where ingredients not equals to UPDATED_INGREDIENTS
        defaultRecipeShouldBeFound("ingredients.notEquals=" + UPDATED_INGREDIENTS);
    }

    @Test
    @Transactional
    public void getAllRecipesByIngredientsIsInShouldWork() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where ingredients in DEFAULT_INGREDIENTS or UPDATED_INGREDIENTS
        defaultRecipeShouldBeFound("ingredients.in=" + DEFAULT_INGREDIENTS + "," + UPDATED_INGREDIENTS);

        // Get all the recipeList where ingredients equals to UPDATED_INGREDIENTS
        defaultRecipeShouldNotBeFound("ingredients.in=" + UPDATED_INGREDIENTS);
    }

    @Test
    @Transactional
    public void getAllRecipesByIngredientsIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where ingredients is not null
        defaultRecipeShouldBeFound("ingredients.specified=true");

        // Get all the recipeList where ingredients is null
        defaultRecipeShouldNotBeFound("ingredients.specified=false");
    }
                @Test
    @Transactional
    public void getAllRecipesByIngredientsContainsSomething() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where ingredients contains DEFAULT_INGREDIENTS
        defaultRecipeShouldBeFound("ingredients.contains=" + DEFAULT_INGREDIENTS);

        // Get all the recipeList where ingredients contains UPDATED_INGREDIENTS
        defaultRecipeShouldNotBeFound("ingredients.contains=" + UPDATED_INGREDIENTS);
    }

    @Test
    @Transactional
    public void getAllRecipesByIngredientsNotContainsSomething() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where ingredients does not contain DEFAULT_INGREDIENTS
        defaultRecipeShouldNotBeFound("ingredients.doesNotContain=" + DEFAULT_INGREDIENTS);

        // Get all the recipeList where ingredients does not contain UPDATED_INGREDIENTS
        defaultRecipeShouldBeFound("ingredients.doesNotContain=" + UPDATED_INGREDIENTS);
    }


    @Test
    @Transactional
    public void getAllRecipesByStepsIsEqualToSomething() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where steps equals to DEFAULT_STEPS
        defaultRecipeShouldBeFound("steps.equals=" + DEFAULT_STEPS);

        // Get all the recipeList where steps equals to UPDATED_STEPS
        defaultRecipeShouldNotBeFound("steps.equals=" + UPDATED_STEPS);
    }

    @Test
    @Transactional
    public void getAllRecipesByStepsIsNotEqualToSomething() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where steps not equals to DEFAULT_STEPS
        defaultRecipeShouldNotBeFound("steps.notEquals=" + DEFAULT_STEPS);

        // Get all the recipeList where steps not equals to UPDATED_STEPS
        defaultRecipeShouldBeFound("steps.notEquals=" + UPDATED_STEPS);
    }

    @Test
    @Transactional
    public void getAllRecipesByStepsIsInShouldWork() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where steps in DEFAULT_STEPS or UPDATED_STEPS
        defaultRecipeShouldBeFound("steps.in=" + DEFAULT_STEPS + "," + UPDATED_STEPS);

        // Get all the recipeList where steps equals to UPDATED_STEPS
        defaultRecipeShouldNotBeFound("steps.in=" + UPDATED_STEPS);
    }

    @Test
    @Transactional
    public void getAllRecipesByStepsIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where steps is not null
        defaultRecipeShouldBeFound("steps.specified=true");

        // Get all the recipeList where steps is null
        defaultRecipeShouldNotBeFound("steps.specified=false");
    }
                @Test
    @Transactional
    public void getAllRecipesByStepsContainsSomething() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where steps contains DEFAULT_STEPS
        defaultRecipeShouldBeFound("steps.contains=" + DEFAULT_STEPS);

        // Get all the recipeList where steps contains UPDATED_STEPS
        defaultRecipeShouldNotBeFound("steps.contains=" + UPDATED_STEPS);
    }

    @Test
    @Transactional
    public void getAllRecipesByStepsNotContainsSomething() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where steps does not contain DEFAULT_STEPS
        defaultRecipeShouldNotBeFound("steps.doesNotContain=" + DEFAULT_STEPS);

        // Get all the recipeList where steps does not contain UPDATED_STEPS
        defaultRecipeShouldBeFound("steps.doesNotContain=" + UPDATED_STEPS);
    }


    @Test
    @Transactional
    public void getAllRecipesByCommentIsEqualToSomething() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where comment equals to DEFAULT_COMMENT
        defaultRecipeShouldBeFound("comment.equals=" + DEFAULT_COMMENT);

        // Get all the recipeList where comment equals to UPDATED_COMMENT
        defaultRecipeShouldNotBeFound("comment.equals=" + UPDATED_COMMENT);
    }

    @Test
    @Transactional
    public void getAllRecipesByCommentIsNotEqualToSomething() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where comment not equals to DEFAULT_COMMENT
        defaultRecipeShouldNotBeFound("comment.notEquals=" + DEFAULT_COMMENT);

        // Get all the recipeList where comment not equals to UPDATED_COMMENT
        defaultRecipeShouldBeFound("comment.notEquals=" + UPDATED_COMMENT);
    }

    @Test
    @Transactional
    public void getAllRecipesByCommentIsInShouldWork() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where comment in DEFAULT_COMMENT or UPDATED_COMMENT
        defaultRecipeShouldBeFound("comment.in=" + DEFAULT_COMMENT + "," + UPDATED_COMMENT);

        // Get all the recipeList where comment equals to UPDATED_COMMENT
        defaultRecipeShouldNotBeFound("comment.in=" + UPDATED_COMMENT);
    }

    @Test
    @Transactional
    public void getAllRecipesByCommentIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where comment is not null
        defaultRecipeShouldBeFound("comment.specified=true");

        // Get all the recipeList where comment is null
        defaultRecipeShouldNotBeFound("comment.specified=false");
    }
                @Test
    @Transactional
    public void getAllRecipesByCommentContainsSomething() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where comment contains DEFAULT_COMMENT
        defaultRecipeShouldBeFound("comment.contains=" + DEFAULT_COMMENT);

        // Get all the recipeList where comment contains UPDATED_COMMENT
        defaultRecipeShouldNotBeFound("comment.contains=" + UPDATED_COMMENT);
    }

    @Test
    @Transactional
    public void getAllRecipesByCommentNotContainsSomething() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where comment does not contain DEFAULT_COMMENT
        defaultRecipeShouldNotBeFound("comment.doesNotContain=" + DEFAULT_COMMENT);

        // Get all the recipeList where comment does not contain UPDATED_COMMENT
        defaultRecipeShouldBeFound("comment.doesNotContain=" + UPDATED_COMMENT);
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
            .andExpect(jsonPath("$.[*].starred").value(hasItem(DEFAULT_STARRED.booleanValue())))
            .andExpect(jsonPath("$.[*].tags").value(hasItem(DEFAULT_TAGS)))
            .andExpect(jsonPath("$.[*].ingredients").value(hasItem(DEFAULT_INGREDIENTS)))
            .andExpect(jsonPath("$.[*].steps").value(hasItem(DEFAULT_STEPS)))
            .andExpect(jsonPath("$.[*].comment").value(hasItem(DEFAULT_COMMENT)));

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
            .starred(UPDATED_STARRED)
            .tags(UPDATED_TAGS)
            .ingredients(UPDATED_INGREDIENTS)
            .steps(UPDATED_STEPS)
            .comment(UPDATED_COMMENT);
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
        assertThat(testRecipe.isStarred()).isEqualTo(UPDATED_STARRED);
        assertThat(testRecipe.getTags()).isEqualTo(UPDATED_TAGS);
        assertThat(testRecipe.getIngredients()).isEqualTo(UPDATED_INGREDIENTS);
        assertThat(testRecipe.getSteps()).isEqualTo(UPDATED_STEPS);
        assertThat(testRecipe.getComment()).isEqualTo(UPDATED_COMMENT);

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
            .andExpect(jsonPath("$.[*].starred").value(hasItem(DEFAULT_STARRED.booleanValue())))
            .andExpect(jsonPath("$.[*].tags").value(hasItem(DEFAULT_TAGS)))
            .andExpect(jsonPath("$.[*].ingredients").value(hasItem(DEFAULT_INGREDIENTS)))
            .andExpect(jsonPath("$.[*].steps").value(hasItem(DEFAULT_STEPS)))
            .andExpect(jsonPath("$.[*].comment").value(hasItem(DEFAULT_COMMENT)));
    }
}
