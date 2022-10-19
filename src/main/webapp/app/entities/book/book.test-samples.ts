import { IBook, NewBook } from './book.model';

export const sampleWithRequiredData: IBook = {
  id: 37098,
  name: 'Jewelery',
};

export const sampleWithPartialData: IBook = {
  id: 60289,
  name: 'synthesize',
};

export const sampleWithFullData: IBook = {
  id: 77314,
  name: 'bandwidth',
  published: false,
};

export const sampleWithNewData: NewBook = {
  name: 'Pizza multi-byte HTTP',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
