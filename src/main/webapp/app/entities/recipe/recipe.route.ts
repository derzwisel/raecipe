import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, Routes, Router } from '@angular/router';
import { Observable, of, EMPTY } from 'rxjs';
import { flatMap } from 'rxjs/operators';

import { Authority } from 'app/shared/constants/authority.constants';
import { UserRouteAccessService } from 'app/core/auth/user-route-access-service';
import { IRecipe, Recipe } from 'app/shared/model/recipe.model';
import { RecipeService } from './recipe.service';
import { RecipeComponent } from './recipe.component';
import { RecipeDetailComponent } from './recipe-detail.component';
import { RecipeUpdateComponent } from './recipe-update.component';

@Injectable({ providedIn: 'root' })
export class RecipeResolve implements Resolve<IRecipe> {
  constructor(private service: RecipeService, private router: Router) {}

  resolve(route: ActivatedRouteSnapshot): Observable<IRecipe> | Observable<never> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        flatMap((recipe: HttpResponse<Recipe>) => {
          if (recipe.body) {
            return of(recipe.body);
          } else {
            this.router.navigate(['404']);
            return EMPTY;
          }
        })
      );
    }
    return of(new Recipe());
  }
}

export const recipeRoute: Routes = [
  {
    path: '',
    component: RecipeComponent,
    data: {
      authorities: [Authority.USER],
      pageTitle: 'raecipeApp.recipe.home.title',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: RecipeDetailComponent,
    resolve: {
      recipe: RecipeResolve,
    },
    data: {
      authorities: [Authority.USER],
      pageTitle: 'raecipeApp.recipe.home.title',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: RecipeUpdateComponent,
    resolve: {
      recipe: RecipeResolve,
    },
    data: {
      authorities: [Authority.USER],
      pageTitle: 'raecipeApp.recipe.home.title',
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: RecipeUpdateComponent,
    resolve: {
      recipe: RecipeResolve,
    },
    data: {
      authorities: [Authority.USER],
      pageTitle: 'raecipeApp.recipe.home.title',
    },
    canActivate: [UserRouteAccessService],
  },
];
