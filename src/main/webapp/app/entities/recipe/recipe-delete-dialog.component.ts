import { Component } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { IRecipe } from 'app/shared/model/recipe.model';
import { RecipeService } from './recipe.service';

@Component({
  templateUrl: './recipe-delete-dialog.component.html',
})
export class RecipeDeleteDialogComponent {
  recipe?: IRecipe;

  constructor(protected recipeService: RecipeService, public activeModal: NgbActiveModal, protected eventManager: JhiEventManager) {}

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.recipeService.delete(id).subscribe(() => {
      this.eventManager.broadcast('recipeListModification');
      this.activeModal.close();
    });
  }
}
