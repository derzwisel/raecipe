import { IBook, NewBook } from './book.model';

export const sampleWithRequiredData: IBook = {
  id: 37098,
  name: 'Jewelery',
};

export const sampleWithPartialData: IBook = {
  id: 528,
  name: 'Naira object-oriented Wooden',
  creator: 'AI',
};

export const sampleWithFullData: IBook = {
  id: 56591,
  name: 'HTTP',
  published: true,
  creator: 'Intelligent invoice Egypt',
};

export const sampleWithNewData: NewBook = {
  name: 'programming',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
