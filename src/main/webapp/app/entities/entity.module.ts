import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

@NgModule({
  imports: [
    RouterModule.forChild([
      {
        path: 'recipe',
        loadChildren: () => import('./recipe/recipe.module').then(m => m.RaecipeRecipeModule),
      },
      {
        path: 'book',
        loadChildren: () => import('./book/book.module').then(m => m.RaecipeBookModule),
      },
      /* jhipster-needle-add-entity-route - JHipster will add entity modules routes here */
    ]),
  ],
})
export class RaecipeEntityModule {}
