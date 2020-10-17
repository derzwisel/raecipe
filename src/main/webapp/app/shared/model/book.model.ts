import { IRecipe } from 'app/shared/model/recipe.model';

export interface IBook {
  id?: number;
  name?: string;
  published?: boolean;
  recipes?: IRecipe[];
}

export class Book implements IBook {
  constructor(public id?: number, public name?: string, public published?: boolean, public recipes?: IRecipe[]) {
    this.published = this.published || false;
  }
}
