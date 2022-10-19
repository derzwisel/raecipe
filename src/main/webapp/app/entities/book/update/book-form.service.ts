import { Injectable } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';
import { DATE_TIME_FORMAT } from 'app/config/input.constants';
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

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends IBook | NewBook> = Omit<T, 'creationDate'> & {
  creationDate?: string | null;
};

type BookFormRawValue = FormValueOf<IBook>;

type NewBookFormRawValue = FormValueOf<NewBook>;

type BookFormDefaults = Pick<NewBook, 'id' | 'published' | 'creationDate' | 'recipes'>;

type BookFormGroupContent = {
  id: FormControl<BookFormRawValue['id'] | NewBook['id']>;
  name: FormControl<BookFormRawValue['name']>;
  published: FormControl<BookFormRawValue['published']>;
  creator: FormControl<BookFormRawValue['creator']>;
  creationDate: FormControl<BookFormRawValue['creationDate']>;
  recipes: FormControl<BookFormRawValue['recipes']>;
};

export type BookFormGroup = FormGroup<BookFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class BookFormService {
  createBookFormGroup(book: BookFormGroupInput = { id: null }): BookFormGroup {
    const bookRawValue = this.convertBookToBookRawValue({
      ...this.getFormDefaults(),
      ...book,
    });
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
      creationDate: new FormControl(bookRawValue.creationDate),
      recipes: new FormControl(bookRawValue.recipes ?? []),
    });
  }

  getBook(form: BookFormGroup): IBook | NewBook {
    return this.convertBookRawValueToBook(form.getRawValue() as BookFormRawValue | NewBookFormRawValue);
  }

  resetForm(form: BookFormGroup, book: BookFormGroupInput): void {
    const bookRawValue = this.convertBookToBookRawValue({ ...this.getFormDefaults(), ...book });
    form.reset(
      {
        ...bookRawValue,
        id: { value: bookRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */
    );
  }

  private getFormDefaults(): BookFormDefaults {
    const currentTime = dayjs();

    return {
      id: null,
      published: false,
      creationDate: currentTime,
      recipes: [],
    };
  }

  private convertBookRawValueToBook(rawBook: BookFormRawValue | NewBookFormRawValue): IBook | NewBook {
    return {
      ...rawBook,
      creationDate: dayjs(rawBook.creationDate, DATE_TIME_FORMAT),
    };
  }

  private convertBookToBookRawValue(
    book: IBook | (Partial<NewBook> & BookFormDefaults)
  ): BookFormRawValue | PartialWithRequiredKeyOf<NewBookFormRawValue> {
    return {
      ...book,
      creationDate: book.creationDate ? book.creationDate.format(DATE_TIME_FORMAT) : undefined,
      recipes: book.recipes ?? [],
    };
  }
}
