import { NgModule } from '@angular/core';
import { SharedModule } from 'app/shared/shared.module';
import { RecipeComponent } from './list/recipe.component';
import { RecipeDetailComponent } from './detail/recipe-detail.component';
import { RecipeUpdateComponent } from './update/recipe-update.component';
import { RecipeDeleteDialogComponent } from './delete/recipe-delete-dialog.component';
import { RecipeRoutingModule } from './route/recipe-routing.module';

@NgModule({
  imports: [SharedModule, RecipeRoutingModule],
  declarations: [RecipeComponent, RecipeDetailComponent, RecipeUpdateComponent, RecipeDeleteDialogComponent],
})
export class RecipeModule {}
