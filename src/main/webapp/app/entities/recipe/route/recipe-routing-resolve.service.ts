import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, Router } from '@angular/router';
import { Observable, of, EMPTY } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IRecipe } from '../recipe.model';
import { RecipeService } from '../service/recipe.service';

@Injectable({ providedIn: 'root' })
export class RecipeRoutingResolveService implements Resolve<IRecipe | null> {
  constructor(protected service: RecipeService, protected router: Router) {}

  resolve(route: ActivatedRouteSnapshot): Observable<IRecipe | null | never> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        mergeMap((recipe: HttpResponse<IRecipe>) => {
          if (recipe.body) {
            return of(recipe.body);
          } else {
            this.router.navigate(['404']);
            return EMPTY;
          }
        })
      );
    }
    return of(null);
  }
}
