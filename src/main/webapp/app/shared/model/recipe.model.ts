export interface IRecipe {
  id?: number;
  name?: string;
  starred?: boolean;
  tags?: string;
  ingredients?: string;
}

export class Recipe implements IRecipe {
  constructor(public id?: number, public name?: string, public starred?: boolean, public tags?: string, public ingredients?: string) {
    this.starred = this.starred || false;
  }
}
