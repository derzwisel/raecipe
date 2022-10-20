import dayjs from 'dayjs/esm';

import { IBook, NewBook } from './book.model';

export const sampleWithRequiredData: IBook = {
  id: 37098,
  name: 'Jewelery',
};

export const sampleWithPartialData: IBook = {
  id: 67174,
  name: 'compress Wooden',
  creator: 'Pizza multi-byte HTTP',
  updateDate: dayjs('2020-10-17T05:25'),
};

export const sampleWithFullData: IBook = {
  id: 70987,
  name: 'pink',
  published: true,
  creator: 'Egypt Stand-alone',
  creationDate: dayjs('2020-10-17T09:55'),
  updateDate: dayjs('2020-10-17T10:55'),
};

export const sampleWithNewData: NewBook = {
  name: 'implement Toys Naira',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
