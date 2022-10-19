import { Injectable } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';

import { IBook, NewBook } from '../book.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IBook for edit and NewBookFormGroupInput for create.
 */
type BookFormGroupInput = IBook | PartialWithRequiredKeyOf<NewBook>;

type BookFormDefaults = Pick<NewBook, 'id' | 'published' | 'recipes'>;

type BookFormGroupContent = {
  id: FormControl<IBook['id'] | NewBook['id']>;
  name: FormControl<IBook['name']>;
  published: FormControl<IBook['published']>;
  creator: FormControl<IBook['creator']>;
  recipes: FormControl<IBook['recipes']>;
};

export type BookFormGroup = FormGroup<BookFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class BookFormService {
  createBookFormGroup(book: BookFormGroupInput = { id: null }): BookFormGroup {
    const bookRawValue = {
      ...this.getFormDefaults(),
      ...book,
    };
    return new FormGroup<BookFormGroupContent>({
      id: new FormControl(
        { value: bookRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        }
      ),
      name: new FormControl(bookRawValue.name, {
        validators: [Validators.required],
      }),
      published: new FormControl(bookRawValue.published),
      creator: new FormControl(bookRawValue.creator),
      recipes: new FormControl(bookRawValue.recipes ?? []),
    });
  }

  getBook(form: BookFormGroup): IBook | NewBook {
    return form.getRawValue() as IBook | NewBook;
  }

  resetForm(form: BookFormGroup, book: BookFormGroupInput): void {
    const bookRawValue = { ...this.getFormDefaults(), ...book };
    form.reset(
      {
        ...bookRawValue,
        id: { value: bookRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */
    );
  }

  private getFormDefaults(): BookFormDefaults {
    return {
      id: null,
      published: false,
      recipes: [],
    };
  }
}
