export interface IRecipe {
  id?: number;
  name?: string;
}

export class Recipe implements IRecipe {
  constructor(public id?: number, public name?: string) {}
}
