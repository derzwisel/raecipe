package org.raecipe.repository.search;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

import java.util.List;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.elasticsearch.search.sort.SortBuilder;
import org.raecipe.domain.Recipe;
import org.raecipe.repository.RecipeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Data Elasticsearch repository for the {@link Recipe} entity.
 */
public interface RecipeSearchRepository extends ElasticsearchRepository<Recipe, Long>, RecipeSearchRepositoryInternal {}

interface RecipeSearchRepositoryInternal {
    Page<Recipe> search(String query, Pageable pageable);

    Page<Recipe> search(Query query);

    void index(Recipe entity);
}

class RecipeSearchRepositoryInternalImpl implements RecipeSearchRepositoryInternal {

    private final ElasticsearchRestTemplate elasticsearchTemplate;
    private final RecipeRepository repository;

    RecipeSearchRepositoryInternalImpl(ElasticsearchRestTemplate elasticsearchTemplate, RecipeRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Page<Recipe> search(String query, Pageable pageable) {
        NativeSearchQuery nativeSearchQuery = new NativeSearchQuery(queryStringQuery(query));
        return search(nativeSearchQuery.setPageable(pageable));
    }

    @Override
    public Page<Recipe> search(Query query) {
        SearchHits<Recipe> searchHits = elasticsearchTemplate.search(query, Recipe.class);
        List<Recipe> hits = searchHits.map(SearchHit::getContent).stream().collect(Collectors.toList());
        return new PageImpl<>(hits, query.getPageable(), searchHits.getTotalHits());
    }

    @Override
    public void index(Recipe entity) {
        repository.findById(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }
}
