package org.raecipe.service.mapper;


import org.raecipe.domain.*;
import org.raecipe.service.dto.RecipeDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity {@link Recipe} and its DTO {@link RecipeDTO}.
 */
@Mapper(componentModel = "spring", uses = {})
public interface RecipeMapper extends EntityMapper<RecipeDTO, Recipe> {



    default Recipe fromId(Long id) {
        if (id == null) {
            return null;
        }
        Recipe recipe = new Recipe();
        recipe.setId(id);
        return recipe;
    }
}
