import dayjs from 'dayjs/esm';

import { IBook, NewBook } from './book.model';

export const sampleWithRequiredData: IBook = {
  id: 37098,
  name: 'Jewelery',
};

export const sampleWithPartialData: IBook = {
  id: 75340,
  name: 'Rhode bandwidth Fresh',
  creator: 'networks Steel bandwidth',
};

export const sampleWithFullData: IBook = {
  id: 32668,
  name: 'Analyst Streamlined',
  published: true,
  creator: 'harness implement Toys',
  creationDate: dayjs('2020-10-17T02:44'),
};

export const sampleWithNewData: NewBook = {
  name: 'Bedfordshire copying',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
