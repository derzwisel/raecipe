import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { IRecipe } from '../recipe.model';
import { sampleWithRequiredData, sampleWithNewData, sampleWithPartialData, sampleWithFullData } from '../recipe.test-samples';

import { RecipeService } from './recipe.service';

const requireRestSample: IRecipe = {
  ...sampleWithRequiredData,
};

describe('Recipe Service', () => {
  let service: RecipeService;
  let httpMock: HttpTestingController;
  let expectedResult: IRecipe | IRecipe[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
    });
    expectedResult = null;
    service = TestBed.inject(RecipeService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  describe('Service methods', () => {
    it('should find an element', () => {
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.find(123).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should create a Recipe', () => {
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      const recipe = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(recipe).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a Recipe', () => {
      const recipe = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(recipe).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a Recipe', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of Recipe', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a Recipe', () => {
      const expected = true;

      service.delete(123).subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    describe('addRecipeToCollectionIfMissing', () => {
      it('should add a Recipe to an empty array', () => {
        const recipe: IRecipe = sampleWithRequiredData;
        expectedResult = service.addRecipeToCollectionIfMissing([], recipe);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(recipe);
      });

      it('should not add a Recipe to an array that contains it', () => {
        const recipe: IRecipe = sampleWithRequiredData;
        const recipeCollection: IRecipe[] = [
          {
            ...recipe,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addRecipeToCollectionIfMissing(recipeCollection, recipe);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a Recipe to an array that doesn't contain it", () => {
        const recipe: IRecipe = sampleWithRequiredData;
        const recipeCollection: IRecipe[] = [sampleWithPartialData];
        expectedResult = service.addRecipeToCollectionIfMissing(recipeCollection, recipe);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(recipe);
      });

      it('should add only unique Recipe to an array', () => {
        const recipeArray: IRecipe[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const recipeCollection: IRecipe[] = [sampleWithRequiredData];
        expectedResult = service.addRecipeToCollectionIfMissing(recipeCollection, ...recipeArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const recipe: IRecipe = sampleWithRequiredData;
        const recipe2: IRecipe = sampleWithPartialData;
        expectedResult = service.addRecipeToCollectionIfMissing([], recipe, recipe2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(recipe);
        expect(expectedResult).toContain(recipe2);
      });

      it('should accept null and undefined values', () => {
        const recipe: IRecipe = sampleWithRequiredData;
        expectedResult = service.addRecipeToCollectionIfMissing([], null, recipe, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(recipe);
      });

      it('should return initial array if no Recipe is added', () => {
        const recipeCollection: IRecipe[] = [sampleWithRequiredData];
        expectedResult = service.addRecipeToCollectionIfMissing(recipeCollection, undefined, null);
        expect(expectedResult).toEqual(recipeCollection);
      });
    });

    describe('compareRecipe', () => {
      it('Should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareRecipe(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('Should return false if one entity is null', () => {
        const entity1 = { id: 123 };
        const entity2 = null;

        const compareResult1 = service.compareRecipe(entity1, entity2);
        const compareResult2 = service.compareRecipe(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey differs', () => {
        const entity1 = { id: 123 };
        const entity2 = { id: 456 };

        const compareResult1 = service.compareRecipe(entity1, entity2);
        const compareResult2 = service.compareRecipe(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey matches', () => {
        const entity1 = { id: 123 };
        const entity2 = { id: 123 };

        const compareResult1 = service.compareRecipe(entity1, entity2);
        const compareResult2 = service.compareRecipe(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
