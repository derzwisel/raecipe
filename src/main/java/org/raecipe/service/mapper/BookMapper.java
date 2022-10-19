package org.raecipe.service.mapper;

import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.*;
import org.raecipe.domain.Book;
import org.raecipe.domain.Recipe;
import org.raecipe.service.dto.BookDTO;
import org.raecipe.service.dto.RecipeDTO;

/**
 * Mapper for the entity {@link Book} and its DTO {@link BookDTO}.
 */
@Mapper(componentModel = "spring")
public interface BookMapper extends EntityMapper<BookDTO, Book> {
    @Mapping(target = "recipes", source = "recipes", qualifiedByName = "recipeNameSet")
    BookDTO toDto(Book s);

    @Mapping(target = "removeRecipe", ignore = true)
    Book toEntity(BookDTO bookDTO);

    @Named("recipeName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    RecipeDTO toDtoRecipeName(Recipe recipe);

    @Named("recipeNameSet")
    default Set<RecipeDTO> toDtoRecipeNameSet(Set<Recipe> recipe) {
        return recipe.stream().map(this::toDtoRecipeName).collect(Collectors.toSet());
    }
}
