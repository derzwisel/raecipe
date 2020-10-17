package org.raecipe.service.mapper;


import org.raecipe.domain.*;
import org.raecipe.service.dto.BookDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity {@link Book} and its DTO {@link BookDTO}.
 */
@Mapper(componentModel = "spring", uses = {RecipeMapper.class})
public interface BookMapper extends EntityMapper<BookDTO, Book> {


    @Mapping(target = "removeRecipe", ignore = true)

    default Book fromId(Long id) {
        if (id == null) {
            return null;
        }
        Book book = new Book();
        book.setId(id);
        return book;
    }
}
