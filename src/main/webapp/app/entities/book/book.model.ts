import dayjs from 'dayjs/esm';
import { IRecipe } from 'app/entities/recipe/recipe.model';

export interface IBook {
  id: number;
  name?: string | null;
  published?: boolean | null;
  creator?: string | null;
  creationDate?: dayjs.Dayjs | null;
  updateDate?: dayjs.Dayjs | null;
  recipes?: Pick<IRecipe, 'id' | 'name'>[] | null;
}

export type NewBook = Omit<IBook, 'id'> & { id: null };
