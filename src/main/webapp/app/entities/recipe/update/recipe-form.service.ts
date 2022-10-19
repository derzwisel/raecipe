import { Injectable } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';

import { IRecipe, NewRecipe } from '../recipe.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IRecipe for edit and NewRecipeFormGroupInput for create.
 */
type RecipeFormGroupInput = IRecipe | PartialWithRequiredKeyOf<NewRecipe>;

type RecipeFormDefaults = Pick<NewRecipe, 'id' | 'starred'>;

type RecipeFormGroupContent = {
  id: FormControl<IRecipe['id'] | NewRecipe['id']>;
  name: FormControl<IRecipe['name']>;
  starred: FormControl<IRecipe['starred']>;
  tags: FormControl<IRecipe['tags']>;
  ingredients: FormControl<IRecipe['ingredients']>;
  steps: FormControl<IRecipe['steps']>;
  comment: FormControl<IRecipe['comment']>;
  duration: FormControl<IRecipe['duration']>;
  pictures: FormControl<IRecipe['pictures']>;
};

export type RecipeFormGroup = FormGroup<RecipeFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class RecipeFormService {
  createRecipeFormGroup(recipe: RecipeFormGroupInput = { id: null }): RecipeFormGroup {
    const recipeRawValue = {
      ...this.getFormDefaults(),
      ...recipe,
    };
    return new FormGroup<RecipeFormGroupContent>({
      id: new FormControl(
        { value: recipeRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        }
      ),
      name: new FormControl(recipeRawValue.name, {
        validators: [Validators.required],
      }),
      starred: new FormControl(recipeRawValue.starred),
      tags: new FormControl(recipeRawValue.tags),
      ingredients: new FormControl(recipeRawValue.ingredients),
      steps: new FormControl(recipeRawValue.steps),
      comment: new FormControl(recipeRawValue.comment),
      duration: new FormControl(recipeRawValue.duration),
      pictures: new FormControl(recipeRawValue.pictures),
    });
  }

  getRecipe(form: RecipeFormGroup): IRecipe | NewRecipe {
    return form.getRawValue() as IRecipe | NewRecipe;
  }

  resetForm(form: RecipeFormGroup, recipe: RecipeFormGroupInput): void {
    const recipeRawValue = { ...this.getFormDefaults(), ...recipe };
    form.reset(
      {
        ...recipeRawValue,
        id: { value: recipeRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */
    );
  }

  private getFormDefaults(): RecipeFormDefaults {
    return {
      id: null,
      starred: false,
    };
  }
}
