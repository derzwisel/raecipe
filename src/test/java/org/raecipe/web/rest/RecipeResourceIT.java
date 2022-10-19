package org.raecipe.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import org.apache.commons.collections4.IterableUtils;
import org.assertj.core.util.IterableUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.raecipe.IntegrationTest;
import org.raecipe.domain.Recipe;
import org.raecipe.repository.RecipeRepository;
import org.raecipe.repository.search.RecipeSearchRepository;
import org.raecipe.service.criteria.RecipeCriteria;
import org.raecipe.service.dto.RecipeDTO;
import org.raecipe.service.mapper.RecipeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link RecipeResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class RecipeResourceIT {

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

    private static final Duration DEFAULT_DURATION = Duration.ofHours(6);
    private static final Duration UPDATED_DURATION = Duration.ofHours(12);
    private static final Duration SMALLER_DURATION = Duration.ofHours(5);

    private static final String DEFAULT_PICTURES = "AAAAAAAAAA";
    private static final String UPDATED_PICTURES = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/recipes";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/_search/recipes";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private RecipeMapper recipeMapper;

    @Autowired
    private RecipeSearchRepository recipeSearchRepository;

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
            .comment(DEFAULT_COMMENT)
            .duration(DEFAULT_DURATION)
            .pictures(DEFAULT_PICTURES);
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
            .comment(UPDATED_COMMENT)
            .duration(UPDATED_DURATION)
            .pictures(UPDATED_PICTURES);
        return recipe;
    }

    @AfterEach
    public void cleanupElasticSearchRepository() {
        recipeSearchRepository.deleteAll();
        assertThat(recipeSearchRepository.count()).isEqualTo(0);
    }

    @BeforeEach
    public void initTest() {
        recipe = createEntity(em);
    }

    @Test
    @Transactional
    void createRecipe() throws Exception {
        int databaseSizeBeforeCreate = recipeRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(recipeSearchRepository.findAll());
        // Create the Recipe
        RecipeDTO recipeDTO = recipeMapper.toDto(recipe);
        restRecipeMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(recipeDTO)))
            .andExpect(status().isCreated());

        // Validate the Recipe in the database
        List<Recipe> recipeList = recipeRepository.findAll();
        assertThat(recipeList).hasSize(databaseSizeBeforeCreate + 1);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(recipeSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });
        Recipe testRecipe = recipeList.get(recipeList.size() - 1);
        assertThat(testRecipe.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testRecipe.getStarred()).isEqualTo(DEFAULT_STARRED);
        assertThat(testRecipe.getTags()).isEqualTo(DEFAULT_TAGS);
        assertThat(testRecipe.getIngredients()).isEqualTo(DEFAULT_INGREDIENTS);
        assertThat(testRecipe.getSteps()).isEqualTo(DEFAULT_STEPS);
        assertThat(testRecipe.getComment()).isEqualTo(DEFAULT_COMMENT);
        assertThat(testRecipe.getDuration()).isEqualTo(DEFAULT_DURATION);
        assertThat(testRecipe.getPictures()).isEqualTo(DEFAULT_PICTURES);
    }

    @Test
    @Transactional
    void createRecipeWithExistingId() throws Exception {
        // Create the Recipe with an existing ID
        recipe.setId(1L);
        RecipeDTO recipeDTO = recipeMapper.toDto(recipe);

        int databaseSizeBeforeCreate = recipeRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(recipeSearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restRecipeMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(recipeDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Recipe in the database
        List<Recipe> recipeList = recipeRepository.findAll();
        assertThat(recipeList).hasSize(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(recipeSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = recipeRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(recipeSearchRepository.findAll());
        // set the field null
        recipe.setName(null);

        // Create the Recipe, which fails.
        RecipeDTO recipeDTO = recipeMapper.toDto(recipe);

        restRecipeMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(recipeDTO)))
            .andExpect(status().isBadRequest());

        List<Recipe> recipeList = recipeRepository.findAll();
        assertThat(recipeList).hasSize(databaseSizeBeforeTest);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(recipeSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void getAllRecipes() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList
        restRecipeMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(recipe.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].starred").value(hasItem(DEFAULT_STARRED.booleanValue())))
            .andExpect(jsonPath("$.[*].tags").value(hasItem(DEFAULT_TAGS)))
            .andExpect(jsonPath("$.[*].ingredients").value(hasItem(DEFAULT_INGREDIENTS)))
            .andExpect(jsonPath("$.[*].steps").value(hasItem(DEFAULT_STEPS)))
            .andExpect(jsonPath("$.[*].comment").value(hasItem(DEFAULT_COMMENT)))
            .andExpect(jsonPath("$.[*].duration").value(hasItem(DEFAULT_DURATION.toString())))
            .andExpect(jsonPath("$.[*].pictures").value(hasItem(DEFAULT_PICTURES)));
    }

    @Test
    @Transactional
    void getRecipe() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get the recipe
        restRecipeMockMvc
            .perform(get(ENTITY_API_URL_ID, recipe.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(recipe.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.starred").value(DEFAULT_STARRED.booleanValue()))
            .andExpect(jsonPath("$.tags").value(DEFAULT_TAGS))
            .andExpect(jsonPath("$.ingredients").value(DEFAULT_INGREDIENTS))
            .andExpect(jsonPath("$.steps").value(DEFAULT_STEPS))
            .andExpect(jsonPath("$.comment").value(DEFAULT_COMMENT))
            .andExpect(jsonPath("$.duration").value(DEFAULT_DURATION.toString()))
            .andExpect(jsonPath("$.pictures").value(DEFAULT_PICTURES));
    }

    @Test
    @Transactional
    void getRecipesByIdFiltering() throws Exception {
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
    void getAllRecipesByNameIsEqualToSomething() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where name equals to DEFAULT_NAME
        defaultRecipeShouldBeFound("name.equals=" + DEFAULT_NAME);

        // Get all the recipeList where name equals to UPDATED_NAME
        defaultRecipeShouldNotBeFound("name.equals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllRecipesByNameIsInShouldWork() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where name in DEFAULT_NAME or UPDATED_NAME
        defaultRecipeShouldBeFound("name.in=" + DEFAULT_NAME + "," + UPDATED_NAME);

        // Get all the recipeList where name equals to UPDATED_NAME
        defaultRecipeShouldNotBeFound("name.in=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllRecipesByNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where name is not null
        defaultRecipeShouldBeFound("name.specified=true");

        // Get all the recipeList where name is null
        defaultRecipeShouldNotBeFound("name.specified=false");
    }

    @Test
    @Transactional
    void getAllRecipesByNameContainsSomething() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where name contains DEFAULT_NAME
        defaultRecipeShouldBeFound("name.contains=" + DEFAULT_NAME);

        // Get all the recipeList where name contains UPDATED_NAME
        defaultRecipeShouldNotBeFound("name.contains=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllRecipesByNameNotContainsSomething() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where name does not contain DEFAULT_NAME
        defaultRecipeShouldNotBeFound("name.doesNotContain=" + DEFAULT_NAME);

        // Get all the recipeList where name does not contain UPDATED_NAME
        defaultRecipeShouldBeFound("name.doesNotContain=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllRecipesByStarredIsEqualToSomething() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where starred equals to DEFAULT_STARRED
        defaultRecipeShouldBeFound("starred.equals=" + DEFAULT_STARRED);

        // Get all the recipeList where starred equals to UPDATED_STARRED
        defaultRecipeShouldNotBeFound("starred.equals=" + UPDATED_STARRED);
    }

    @Test
    @Transactional
    void getAllRecipesByStarredIsInShouldWork() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where starred in DEFAULT_STARRED or UPDATED_STARRED
        defaultRecipeShouldBeFound("starred.in=" + DEFAULT_STARRED + "," + UPDATED_STARRED);

        // Get all the recipeList where starred equals to UPDATED_STARRED
        defaultRecipeShouldNotBeFound("starred.in=" + UPDATED_STARRED);
    }

    @Test
    @Transactional
    void getAllRecipesByStarredIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where starred is not null
        defaultRecipeShouldBeFound("starred.specified=true");

        // Get all the recipeList where starred is null
        defaultRecipeShouldNotBeFound("starred.specified=false");
    }

    @Test
    @Transactional
    void getAllRecipesByTagsIsEqualToSomething() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where tags equals to DEFAULT_TAGS
        defaultRecipeShouldBeFound("tags.equals=" + DEFAULT_TAGS);

        // Get all the recipeList where tags equals to UPDATED_TAGS
        defaultRecipeShouldNotBeFound("tags.equals=" + UPDATED_TAGS);
    }

    @Test
    @Transactional
    void getAllRecipesByTagsIsInShouldWork() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where tags in DEFAULT_TAGS or UPDATED_TAGS
        defaultRecipeShouldBeFound("tags.in=" + DEFAULT_TAGS + "," + UPDATED_TAGS);

        // Get all the recipeList where tags equals to UPDATED_TAGS
        defaultRecipeShouldNotBeFound("tags.in=" + UPDATED_TAGS);
    }

    @Test
    @Transactional
    void getAllRecipesByTagsIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where tags is not null
        defaultRecipeShouldBeFound("tags.specified=true");

        // Get all the recipeList where tags is null
        defaultRecipeShouldNotBeFound("tags.specified=false");
    }

    @Test
    @Transactional
    void getAllRecipesByTagsContainsSomething() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where tags contains DEFAULT_TAGS
        defaultRecipeShouldBeFound("tags.contains=" + DEFAULT_TAGS);

        // Get all the recipeList where tags contains UPDATED_TAGS
        defaultRecipeShouldNotBeFound("tags.contains=" + UPDATED_TAGS);
    }

    @Test
    @Transactional
    void getAllRecipesByTagsNotContainsSomething() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where tags does not contain DEFAULT_TAGS
        defaultRecipeShouldNotBeFound("tags.doesNotContain=" + DEFAULT_TAGS);

        // Get all the recipeList where tags does not contain UPDATED_TAGS
        defaultRecipeShouldBeFound("tags.doesNotContain=" + UPDATED_TAGS);
    }

    @Test
    @Transactional
    void getAllRecipesByIngredientsIsEqualToSomething() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where ingredients equals to DEFAULT_INGREDIENTS
        defaultRecipeShouldBeFound("ingredients.equals=" + DEFAULT_INGREDIENTS);

        // Get all the recipeList where ingredients equals to UPDATED_INGREDIENTS
        defaultRecipeShouldNotBeFound("ingredients.equals=" + UPDATED_INGREDIENTS);
    }

    @Test
    @Transactional
    void getAllRecipesByIngredientsIsInShouldWork() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where ingredients in DEFAULT_INGREDIENTS or UPDATED_INGREDIENTS
        defaultRecipeShouldBeFound("ingredients.in=" + DEFAULT_INGREDIENTS + "," + UPDATED_INGREDIENTS);

        // Get all the recipeList where ingredients equals to UPDATED_INGREDIENTS
        defaultRecipeShouldNotBeFound("ingredients.in=" + UPDATED_INGREDIENTS);
    }

    @Test
    @Transactional
    void getAllRecipesByIngredientsIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where ingredients is not null
        defaultRecipeShouldBeFound("ingredients.specified=true");

        // Get all the recipeList where ingredients is null
        defaultRecipeShouldNotBeFound("ingredients.specified=false");
    }

    @Test
    @Transactional
    void getAllRecipesByIngredientsContainsSomething() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where ingredients contains DEFAULT_INGREDIENTS
        defaultRecipeShouldBeFound("ingredients.contains=" + DEFAULT_INGREDIENTS);

        // Get all the recipeList where ingredients contains UPDATED_INGREDIENTS
        defaultRecipeShouldNotBeFound("ingredients.contains=" + UPDATED_INGREDIENTS);
    }

    @Test
    @Transactional
    void getAllRecipesByIngredientsNotContainsSomething() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where ingredients does not contain DEFAULT_INGREDIENTS
        defaultRecipeShouldNotBeFound("ingredients.doesNotContain=" + DEFAULT_INGREDIENTS);

        // Get all the recipeList where ingredients does not contain UPDATED_INGREDIENTS
        defaultRecipeShouldBeFound("ingredients.doesNotContain=" + UPDATED_INGREDIENTS);
    }

    @Test
    @Transactional
    void getAllRecipesByStepsIsEqualToSomething() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where steps equals to DEFAULT_STEPS
        defaultRecipeShouldBeFound("steps.equals=" + DEFAULT_STEPS);

        // Get all the recipeList where steps equals to UPDATED_STEPS
        defaultRecipeShouldNotBeFound("steps.equals=" + UPDATED_STEPS);
    }

    @Test
    @Transactional
    void getAllRecipesByStepsIsInShouldWork() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where steps in DEFAULT_STEPS or UPDATED_STEPS
        defaultRecipeShouldBeFound("steps.in=" + DEFAULT_STEPS + "," + UPDATED_STEPS);

        // Get all the recipeList where steps equals to UPDATED_STEPS
        defaultRecipeShouldNotBeFound("steps.in=" + UPDATED_STEPS);
    }

    @Test
    @Transactional
    void getAllRecipesByStepsIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where steps is not null
        defaultRecipeShouldBeFound("steps.specified=true");

        // Get all the recipeList where steps is null
        defaultRecipeShouldNotBeFound("steps.specified=false");
    }

    @Test
    @Transactional
    void getAllRecipesByStepsContainsSomething() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where steps contains DEFAULT_STEPS
        defaultRecipeShouldBeFound("steps.contains=" + DEFAULT_STEPS);

        // Get all the recipeList where steps contains UPDATED_STEPS
        defaultRecipeShouldNotBeFound("steps.contains=" + UPDATED_STEPS);
    }

    @Test
    @Transactional
    void getAllRecipesByStepsNotContainsSomething() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where steps does not contain DEFAULT_STEPS
        defaultRecipeShouldNotBeFound("steps.doesNotContain=" + DEFAULT_STEPS);

        // Get all the recipeList where steps does not contain UPDATED_STEPS
        defaultRecipeShouldBeFound("steps.doesNotContain=" + UPDATED_STEPS);
    }

    @Test
    @Transactional
    void getAllRecipesByCommentIsEqualToSomething() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where comment equals to DEFAULT_COMMENT
        defaultRecipeShouldBeFound("comment.equals=" + DEFAULT_COMMENT);

        // Get all the recipeList where comment equals to UPDATED_COMMENT
        defaultRecipeShouldNotBeFound("comment.equals=" + UPDATED_COMMENT);
    }

    @Test
    @Transactional
    void getAllRecipesByCommentIsInShouldWork() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where comment in DEFAULT_COMMENT or UPDATED_COMMENT
        defaultRecipeShouldBeFound("comment.in=" + DEFAULT_COMMENT + "," + UPDATED_COMMENT);

        // Get all the recipeList where comment equals to UPDATED_COMMENT
        defaultRecipeShouldNotBeFound("comment.in=" + UPDATED_COMMENT);
    }

    @Test
    @Transactional
    void getAllRecipesByCommentIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where comment is not null
        defaultRecipeShouldBeFound("comment.specified=true");

        // Get all the recipeList where comment is null
        defaultRecipeShouldNotBeFound("comment.specified=false");
    }

    @Test
    @Transactional
    void getAllRecipesByCommentContainsSomething() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where comment contains DEFAULT_COMMENT
        defaultRecipeShouldBeFound("comment.contains=" + DEFAULT_COMMENT);

        // Get all the recipeList where comment contains UPDATED_COMMENT
        defaultRecipeShouldNotBeFound("comment.contains=" + UPDATED_COMMENT);
    }

    @Test
    @Transactional
    void getAllRecipesByCommentNotContainsSomething() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where comment does not contain DEFAULT_COMMENT
        defaultRecipeShouldNotBeFound("comment.doesNotContain=" + DEFAULT_COMMENT);

        // Get all the recipeList where comment does not contain UPDATED_COMMENT
        defaultRecipeShouldBeFound("comment.doesNotContain=" + UPDATED_COMMENT);
    }

    @Test
    @Transactional
    void getAllRecipesByDurationIsEqualToSomething() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where duration equals to DEFAULT_DURATION
        defaultRecipeShouldBeFound("duration.equals=" + DEFAULT_DURATION);

        // Get all the recipeList where duration equals to UPDATED_DURATION
        defaultRecipeShouldNotBeFound("duration.equals=" + UPDATED_DURATION);
    }

    @Test
    @Transactional
    void getAllRecipesByDurationIsInShouldWork() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where duration in DEFAULT_DURATION or UPDATED_DURATION
        defaultRecipeShouldBeFound("duration.in=" + DEFAULT_DURATION + "," + UPDATED_DURATION);

        // Get all the recipeList where duration equals to UPDATED_DURATION
        defaultRecipeShouldNotBeFound("duration.in=" + UPDATED_DURATION);
    }

    @Test
    @Transactional
    void getAllRecipesByDurationIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where duration is not null
        defaultRecipeShouldBeFound("duration.specified=true");

        // Get all the recipeList where duration is null
        defaultRecipeShouldNotBeFound("duration.specified=false");
    }

    @Test
    @Transactional
    void getAllRecipesByDurationIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where duration is greater than or equal to DEFAULT_DURATION
        defaultRecipeShouldBeFound("duration.greaterThanOrEqual=" + DEFAULT_DURATION);

        // Get all the recipeList where duration is greater than or equal to UPDATED_DURATION
        defaultRecipeShouldNotBeFound("duration.greaterThanOrEqual=" + UPDATED_DURATION);
    }

    @Test
    @Transactional
    void getAllRecipesByDurationIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where duration is less than or equal to DEFAULT_DURATION
        defaultRecipeShouldBeFound("duration.lessThanOrEqual=" + DEFAULT_DURATION);

        // Get all the recipeList where duration is less than or equal to SMALLER_DURATION
        defaultRecipeShouldNotBeFound("duration.lessThanOrEqual=" + SMALLER_DURATION);
    }

    @Test
    @Transactional
    void getAllRecipesByDurationIsLessThanSomething() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where duration is less than DEFAULT_DURATION
        defaultRecipeShouldNotBeFound("duration.lessThan=" + DEFAULT_DURATION);

        // Get all the recipeList where duration is less than UPDATED_DURATION
        defaultRecipeShouldBeFound("duration.lessThan=" + UPDATED_DURATION);
    }

    @Test
    @Transactional
    void getAllRecipesByDurationIsGreaterThanSomething() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where duration is greater than DEFAULT_DURATION
        defaultRecipeShouldNotBeFound("duration.greaterThan=" + DEFAULT_DURATION);

        // Get all the recipeList where duration is greater than SMALLER_DURATION
        defaultRecipeShouldBeFound("duration.greaterThan=" + SMALLER_DURATION);
    }

    @Test
    @Transactional
    void getAllRecipesByPicturesIsEqualToSomething() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where pictures equals to DEFAULT_PICTURES
        defaultRecipeShouldBeFound("pictures.equals=" + DEFAULT_PICTURES);

        // Get all the recipeList where pictures equals to UPDATED_PICTURES
        defaultRecipeShouldNotBeFound("pictures.equals=" + UPDATED_PICTURES);
    }

    @Test
    @Transactional
    void getAllRecipesByPicturesIsInShouldWork() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where pictures in DEFAULT_PICTURES or UPDATED_PICTURES
        defaultRecipeShouldBeFound("pictures.in=" + DEFAULT_PICTURES + "," + UPDATED_PICTURES);

        // Get all the recipeList where pictures equals to UPDATED_PICTURES
        defaultRecipeShouldNotBeFound("pictures.in=" + UPDATED_PICTURES);
    }

    @Test
    @Transactional
    void getAllRecipesByPicturesIsNullOrNotNull() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where pictures is not null
        defaultRecipeShouldBeFound("pictures.specified=true");

        // Get all the recipeList where pictures is null
        defaultRecipeShouldNotBeFound("pictures.specified=false");
    }

    @Test
    @Transactional
    void getAllRecipesByPicturesContainsSomething() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where pictures contains DEFAULT_PICTURES
        defaultRecipeShouldBeFound("pictures.contains=" + DEFAULT_PICTURES);

        // Get all the recipeList where pictures contains UPDATED_PICTURES
        defaultRecipeShouldNotBeFound("pictures.contains=" + UPDATED_PICTURES);
    }

    @Test
    @Transactional
    void getAllRecipesByPicturesNotContainsSomething() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        // Get all the recipeList where pictures does not contain DEFAULT_PICTURES
        defaultRecipeShouldNotBeFound("pictures.doesNotContain=" + DEFAULT_PICTURES);

        // Get all the recipeList where pictures does not contain UPDATED_PICTURES
        defaultRecipeShouldBeFound("pictures.doesNotContain=" + UPDATED_PICTURES);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultRecipeShouldBeFound(String filter) throws Exception {
        restRecipeMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(recipe.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].starred").value(hasItem(DEFAULT_STARRED.booleanValue())))
            .andExpect(jsonPath("$.[*].tags").value(hasItem(DEFAULT_TAGS)))
            .andExpect(jsonPath("$.[*].ingredients").value(hasItem(DEFAULT_INGREDIENTS)))
            .andExpect(jsonPath("$.[*].steps").value(hasItem(DEFAULT_STEPS)))
            .andExpect(jsonPath("$.[*].comment").value(hasItem(DEFAULT_COMMENT)))
            .andExpect(jsonPath("$.[*].duration").value(hasItem(DEFAULT_DURATION.toString())))
            .andExpect(jsonPath("$.[*].pictures").value(hasItem(DEFAULT_PICTURES)));

        // Check, that the count call also returns 1
        restRecipeMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultRecipeShouldNotBeFound(String filter) throws Exception {
        restRecipeMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restRecipeMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingRecipe() throws Exception {
        // Get the recipe
        restRecipeMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingRecipe() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        int databaseSizeBeforeUpdate = recipeRepository.findAll().size();
        recipeSearchRepository.save(recipe);
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(recipeSearchRepository.findAll());

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
            .comment(UPDATED_COMMENT)
            .duration(UPDATED_DURATION)
            .pictures(UPDATED_PICTURES);
        RecipeDTO recipeDTO = recipeMapper.toDto(updatedRecipe);

        restRecipeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, recipeDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(recipeDTO))
            )
            .andExpect(status().isOk());

        // Validate the Recipe in the database
        List<Recipe> recipeList = recipeRepository.findAll();
        assertThat(recipeList).hasSize(databaseSizeBeforeUpdate);
        Recipe testRecipe = recipeList.get(recipeList.size() - 1);
        assertThat(testRecipe.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testRecipe.getStarred()).isEqualTo(UPDATED_STARRED);
        assertThat(testRecipe.getTags()).isEqualTo(UPDATED_TAGS);
        assertThat(testRecipe.getIngredients()).isEqualTo(UPDATED_INGREDIENTS);
        assertThat(testRecipe.getSteps()).isEqualTo(UPDATED_STEPS);
        assertThat(testRecipe.getComment()).isEqualTo(UPDATED_COMMENT);
        assertThat(testRecipe.getDuration()).isEqualTo(UPDATED_DURATION);
        assertThat(testRecipe.getPictures()).isEqualTo(UPDATED_PICTURES);
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(recipeSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<Recipe> recipeSearchList = IterableUtils.toList(recipeSearchRepository.findAll());
                Recipe testRecipeSearch = recipeSearchList.get(searchDatabaseSizeAfter - 1);
                assertThat(testRecipeSearch.getName()).isEqualTo(UPDATED_NAME);
                assertThat(testRecipeSearch.getStarred()).isEqualTo(UPDATED_STARRED);
                assertThat(testRecipeSearch.getTags()).isEqualTo(UPDATED_TAGS);
                assertThat(testRecipeSearch.getIngredients()).isEqualTo(UPDATED_INGREDIENTS);
                assertThat(testRecipeSearch.getSteps()).isEqualTo(UPDATED_STEPS);
                assertThat(testRecipeSearch.getComment()).isEqualTo(UPDATED_COMMENT);
                assertThat(testRecipeSearch.getDuration()).isEqualTo(UPDATED_DURATION);
                assertThat(testRecipeSearch.getPictures()).isEqualTo(UPDATED_PICTURES);
            });
    }

    @Test
    @Transactional
    void putNonExistingRecipe() throws Exception {
        int databaseSizeBeforeUpdate = recipeRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(recipeSearchRepository.findAll());
        recipe.setId(count.incrementAndGet());

        // Create the Recipe
        RecipeDTO recipeDTO = recipeMapper.toDto(recipe);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restRecipeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, recipeDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(recipeDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Recipe in the database
        List<Recipe> recipeList = recipeRepository.findAll();
        assertThat(recipeList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(recipeSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithIdMismatchRecipe() throws Exception {
        int databaseSizeBeforeUpdate = recipeRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(recipeSearchRepository.findAll());
        recipe.setId(count.incrementAndGet());

        // Create the Recipe
        RecipeDTO recipeDTO = recipeMapper.toDto(recipe);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRecipeMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(recipeDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Recipe in the database
        List<Recipe> recipeList = recipeRepository.findAll();
        assertThat(recipeList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(recipeSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamRecipe() throws Exception {
        int databaseSizeBeforeUpdate = recipeRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(recipeSearchRepository.findAll());
        recipe.setId(count.incrementAndGet());

        // Create the Recipe
        RecipeDTO recipeDTO = recipeMapper.toDto(recipe);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRecipeMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(recipeDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Recipe in the database
        List<Recipe> recipeList = recipeRepository.findAll();
        assertThat(recipeList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(recipeSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void partialUpdateRecipeWithPatch() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        int databaseSizeBeforeUpdate = recipeRepository.findAll().size();

        // Update the recipe using partial update
        Recipe partialUpdatedRecipe = new Recipe();
        partialUpdatedRecipe.setId(recipe.getId());

        partialUpdatedRecipe.name(UPDATED_NAME).starred(UPDATED_STARRED);

        restRecipeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedRecipe.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedRecipe))
            )
            .andExpect(status().isOk());

        // Validate the Recipe in the database
        List<Recipe> recipeList = recipeRepository.findAll();
        assertThat(recipeList).hasSize(databaseSizeBeforeUpdate);
        Recipe testRecipe = recipeList.get(recipeList.size() - 1);
        assertThat(testRecipe.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testRecipe.getStarred()).isEqualTo(UPDATED_STARRED);
        assertThat(testRecipe.getTags()).isEqualTo(DEFAULT_TAGS);
        assertThat(testRecipe.getIngredients()).isEqualTo(DEFAULT_INGREDIENTS);
        assertThat(testRecipe.getSteps()).isEqualTo(DEFAULT_STEPS);
        assertThat(testRecipe.getComment()).isEqualTo(DEFAULT_COMMENT);
        assertThat(testRecipe.getDuration()).isEqualTo(DEFAULT_DURATION);
        assertThat(testRecipe.getPictures()).isEqualTo(DEFAULT_PICTURES);
    }

    @Test
    @Transactional
    void fullUpdateRecipeWithPatch() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);

        int databaseSizeBeforeUpdate = recipeRepository.findAll().size();

        // Update the recipe using partial update
        Recipe partialUpdatedRecipe = new Recipe();
        partialUpdatedRecipe.setId(recipe.getId());

        partialUpdatedRecipe
            .name(UPDATED_NAME)
            .starred(UPDATED_STARRED)
            .tags(UPDATED_TAGS)
            .ingredients(UPDATED_INGREDIENTS)
            .steps(UPDATED_STEPS)
            .comment(UPDATED_COMMENT)
            .duration(UPDATED_DURATION)
            .pictures(UPDATED_PICTURES);

        restRecipeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedRecipe.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedRecipe))
            )
            .andExpect(status().isOk());

        // Validate the Recipe in the database
        List<Recipe> recipeList = recipeRepository.findAll();
        assertThat(recipeList).hasSize(databaseSizeBeforeUpdate);
        Recipe testRecipe = recipeList.get(recipeList.size() - 1);
        assertThat(testRecipe.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testRecipe.getStarred()).isEqualTo(UPDATED_STARRED);
        assertThat(testRecipe.getTags()).isEqualTo(UPDATED_TAGS);
        assertThat(testRecipe.getIngredients()).isEqualTo(UPDATED_INGREDIENTS);
        assertThat(testRecipe.getSteps()).isEqualTo(UPDATED_STEPS);
        assertThat(testRecipe.getComment()).isEqualTo(UPDATED_COMMENT);
        assertThat(testRecipe.getDuration()).isEqualTo(UPDATED_DURATION);
        assertThat(testRecipe.getPictures()).isEqualTo(UPDATED_PICTURES);
    }

    @Test
    @Transactional
    void patchNonExistingRecipe() throws Exception {
        int databaseSizeBeforeUpdate = recipeRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(recipeSearchRepository.findAll());
        recipe.setId(count.incrementAndGet());

        // Create the Recipe
        RecipeDTO recipeDTO = recipeMapper.toDto(recipe);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restRecipeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, recipeDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(recipeDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Recipe in the database
        List<Recipe> recipeList = recipeRepository.findAll();
        assertThat(recipeList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(recipeSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithIdMismatchRecipe() throws Exception {
        int databaseSizeBeforeUpdate = recipeRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(recipeSearchRepository.findAll());
        recipe.setId(count.incrementAndGet());

        // Create the Recipe
        RecipeDTO recipeDTO = recipeMapper.toDto(recipe);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRecipeMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(recipeDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Recipe in the database
        List<Recipe> recipeList = recipeRepository.findAll();
        assertThat(recipeList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(recipeSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamRecipe() throws Exception {
        int databaseSizeBeforeUpdate = recipeRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(recipeSearchRepository.findAll());
        recipe.setId(count.incrementAndGet());

        // Create the Recipe
        RecipeDTO recipeDTO = recipeMapper.toDto(recipe);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restRecipeMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(recipeDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Recipe in the database
        List<Recipe> recipeList = recipeRepository.findAll();
        assertThat(recipeList).hasSize(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(recipeSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void deleteRecipe() throws Exception {
        // Initialize the database
        recipeRepository.saveAndFlush(recipe);
        recipeRepository.save(recipe);
        recipeSearchRepository.save(recipe);

        int databaseSizeBeforeDelete = recipeRepository.findAll().size();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(recipeSearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the recipe
        restRecipeMockMvc
            .perform(delete(ENTITY_API_URL_ID, recipe.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Recipe> recipeList = recipeRepository.findAll();
        assertThat(recipeList).hasSize(databaseSizeBeforeDelete - 1);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(recipeSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    @Transactional
    void searchRecipe() throws Exception {
        // Initialize the database
        recipe = recipeRepository.saveAndFlush(recipe);
        recipeSearchRepository.save(recipe);

        // Search the recipe
        restRecipeMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + recipe.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(recipe.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].starred").value(hasItem(DEFAULT_STARRED.booleanValue())))
            .andExpect(jsonPath("$.[*].tags").value(hasItem(DEFAULT_TAGS)))
            .andExpect(jsonPath("$.[*].ingredients").value(hasItem(DEFAULT_INGREDIENTS)))
            .andExpect(jsonPath("$.[*].steps").value(hasItem(DEFAULT_STEPS)))
            .andExpect(jsonPath("$.[*].comment").value(hasItem(DEFAULT_COMMENT)))
            .andExpect(jsonPath("$.[*].duration").value(hasItem(DEFAULT_DURATION.toString())))
            .andExpect(jsonPath("$.[*].pictures").value(hasItem(DEFAULT_PICTURES)));
    }
}
