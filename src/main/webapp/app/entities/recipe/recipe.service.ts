import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption, SearchWithPagination } from 'app/shared/util/request-util';
import { IRecipe } from 'app/shared/model/recipe.model';

type EntityResponseType = HttpResponse<IRecipe>;
type EntityArrayResponseType = HttpResponse<IRecipe[]>;

@Injectable({ providedIn: 'root' })
export class RecipeService {
  public resourceUrl = SERVER_API_URL + 'api/recipes';
  public resourceSearchUrl = SERVER_API_URL + 'api/_search/recipes';

  constructor(protected http: HttpClient) {}

  create(recipe: IRecipe): Observable<EntityResponseType> {
    return this.http.post<IRecipe>(this.resourceUrl, recipe, { observe: 'response' });
  }

  update(recipe: IRecipe): Observable<EntityResponseType> {
    return this.http.put<IRecipe>(this.resourceUrl, recipe, { observe: 'response' });
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
}
