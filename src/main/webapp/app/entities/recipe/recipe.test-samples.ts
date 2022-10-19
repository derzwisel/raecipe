import { IRecipe, NewRecipe } from './recipe.model';

export const sampleWithRequiredData: IRecipe = {
  id: 37149,
  name: 'Cambridgeshire Fresh IB',
};

export const sampleWithPartialData: IRecipe = {
  id: 46848,
  name: 'Swaziland frictionless Dollar',
  starred: false,
  tags: 'responsive Berkshire',
  ingredients: 'Organic',
  duration: '74530',
  pictures: 'Croatia',
};

export const sampleWithFullData: IRecipe = {
  id: 71049,
  name: 'synergy',
  starred: true,
  tags: 'Clothing navigate Directives',
  ingredients: 'Incredible',
  steps: 'Incredible Plaza',
  comment: 'driver salmon',
  duration: '8065',
  pictures: 'Metal',
};

export const sampleWithNewData: NewRecipe = {
  name: 'Officer Human green',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
