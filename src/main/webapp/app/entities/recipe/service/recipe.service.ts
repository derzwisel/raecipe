import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { SearchWithPagination } from 'app/core/request/request.model';
import { IRecipe, NewRecipe } from '../recipe.model';

export type PartialUpdateRecipe = Partial<IRecipe> & Pick<IRecipe, 'id'>;

export type EntityResponseType = HttpResponse<IRecipe>;
export type EntityArrayResponseType = HttpResponse<IRecipe[]>;

@Injectable({ providedIn: 'root' })
export class RecipeService {
  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/recipes');
  protected resourceSearchUrl = this.applicationConfigService.getEndpointFor('api/_search/recipes');

  constructor(protected http: HttpClient, protected applicationConfigService: ApplicationConfigService) {}

  create(recipe: NewRecipe): Observable<EntityResponseType> {
    return this.http.post<IRecipe>(this.resourceUrl, recipe, { observe: 'response' });
  }

  update(recipe: IRecipe): Observable<EntityResponseType> {
    return this.http.put<IRecipe>(`${this.resourceUrl}/${this.getRecipeIdentifier(recipe)}`, recipe, { observe: 'response' });
  }

  partialUpdate(recipe: PartialUpdateRecipe): Observable<EntityResponseType> {
    return this.http.patch<IRecipe>(`${this.resourceUrl}/${this.getRecipeIdentifier(recipe)}`, recipe, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IRecipe>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IRecipe[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  search(req: SearchWithPagination): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IRecipe[]>(this.resourceSearchUrl, { params: options, observe: 'response' });
  }

  getRecipeIdentifier(recipe: Pick<IRecipe, 'id'>): number {
    return recipe.id;
  }

  compareRecipe(o1: Pick<IRecipe, 'id'> | null, o2: Pick<IRecipe, 'id'> | null): boolean {
    return o1 && o2 ? this.getRecipeIdentifier(o1) === this.getRecipeIdentifier(o2) : o1 === o2;
  }

  addRecipeToCollectionIfMissing<Type extends Pick<IRecipe, 'id'>>(
    recipeCollection: Type[],
    ...recipesToCheck: (Type | null | undefined)[]
  ): Type[] {
    const recipes: Type[] = recipesToCheck.filter(isPresent);
    if (recipes.length > 0) {
      const recipeCollectionIdentifiers = recipeCollection.map(recipeItem => this.getRecipeIdentifier(recipeItem)!);
      const recipesToAdd = recipes.filter(recipeItem => {
        const recipeIdentifier = this.getRecipeIdentifier(recipeItem);
        if (recipeCollectionIdentifiers.includes(recipeIdentifier)) {
          return false;
        }
        recipeCollectionIdentifiers.push(recipeIdentifier);
        return true;
      });
      return [...recipesToAdd, ...recipeCollection];
    }
    return recipeCollection;
  }
}
