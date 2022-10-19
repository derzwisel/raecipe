export interface IRecipe {
  id: number;
  name?: string | null;
  starred?: boolean | null;
  tags?: string | null;
  ingredients?: string | null;
  steps?: string | null;
  comment?: string | null;
  duration?: string | null;
  pictures?: string | null;
}

export type NewRecipe = Omit<IRecipe, 'id'> & { id: null };
