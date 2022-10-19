import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { RecipeComponent } from '../list/recipe.component';
import { RecipeDetailComponent } from '../detail/recipe-detail.component';
import { RecipeUpdateComponent } from '../update/recipe-update.component';
import { RecipeRoutingResolveService } from './recipe-routing-resolve.service';
import { ASC } from 'app/config/navigation.constants';

const recipeRoute: Routes = [
  {
    path: '',
    component: RecipeComponent,
    data: {
      defaultSort: 'id,' + ASC,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: RecipeDetailComponent,
    resolve: {
      recipe: RecipeRoutingResolveService,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: RecipeUpdateComponent,
    resolve: {
      recipe: RecipeRoutingResolveService,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: RecipeUpdateComponent,
    resolve: {
      recipe: RecipeRoutingResolveService,
    },
    canActivate: [UserRouteAccessService],
  },
];

@NgModule({
  imports: [RouterModule.forChild(recipeRoute)],
  exports: [RouterModule],
})
export class RecipeRoutingModule {}
