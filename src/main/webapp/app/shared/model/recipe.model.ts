export interface IRecipe {
  id?: number;
  name?: string;
  starred?: boolean;
  tags?: string;
  ingredients?: string;
  steps?: string;
  comment?: string;
  duration?: number;
}

export class Recipe implements IRecipe {
  constructor(
    public id?: number,
    public name?: string,
    public starred?: boolean,
    public tags?: string,
    public ingredients?: string,
    public steps?: string,
    public comment?: string,
    public duration?: number
  ) {
    this.starred = this.starred || false;
  }
}
