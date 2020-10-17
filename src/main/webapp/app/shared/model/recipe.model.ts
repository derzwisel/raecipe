export interface IRecipe {
  id?: number;
  name?: string;
  duration?: number;
  instructions?: string;
}

export class Recipe implements IRecipe {
  constructor(public id?: number, public name?: string, public duration?: number, public instructions?: string) {}
}
