package org.raecipe.service.mapper;

import org.mapstruct.*;
import org.raecipe.domain.Recipe;
import org.raecipe.service.dto.RecipeDTO;

/**
 * Mapper for the entity {@link Recipe} and its DTO {@link RecipeDTO}.
 */
@Mapper(componentModel = "spring")
public interface RecipeMapper extends EntityMapper<RecipeDTO, Recipe> {}
