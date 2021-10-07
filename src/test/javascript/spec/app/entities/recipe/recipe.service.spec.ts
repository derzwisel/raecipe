import { TestBed, getTestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { RecipeService } from 'app/entities/recipe/recipe.service';
import { IRecipe, Recipe } from 'app/shared/model/recipe.model';

describe('Service Tests', () => {
  describe('Recipe Service', () => {
    let injector: TestBed;
    let service: RecipeService;
    let httpMock: HttpTestingController;
    let elemDefault: IRecipe;
    let expectedResult: IRecipe | IRecipe[] | boolean | null;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [HttpClientTestingModule],
      });
      expectedResult = null;
      injector = getTestBed();
      service = injector.get(RecipeService);
      httpMock = injector.get(HttpTestingController);

      elemDefault = new Recipe(0, 'AAAAAAA', false, 'AAAAAAA', 'AAAAAAA', 'AAAAAAA', 'AAAAAAA', 0);
    });

    describe('Service methods', () => {
      it('should find an element', () => {
        const returnedFromService = Object.assign({}, elemDefault);

        service.find(123).subscribe(resp => (expectedResult = resp.body));

        const req = httpMock.expectOne({ method: 'GET' });
        req.flush(returnedFromService);
        expect(expectedResult).toMatchObject(elemDefault);
      });

      it('should create a Recipe', () => {
        const returnedFromService = Object.assign(
          {
            id: 0,
          },
          elemDefault
        );

        const expected = Object.assign({}, returnedFromService);

        service.create(new Recipe()).subscribe(resp => (expectedResult = resp.body));

        const req = httpMock.expectOne({ method: 'POST' });
        req.flush(returnedFromService);
        expect(expectedResult).toMatchObject(expected);
      });

      it('should update a Recipe', () => {
        const returnedFromService = Object.assign(
          {
            name: 'BBBBBB',
            starred: true,
            tags: 'BBBBBB',
            ingredients: 'BBBBBB',
            steps: 'BBBBBB',
            comment: 'BBBBBB',
            duration: 'BBBBBB',
          },
          elemDefault
        );

        const expected = Object.assign({}, returnedFromService);

        service.update(expected).subscribe(resp => (expectedResult = resp.body));

        const req = httpMock.expectOne({ method: 'PUT' });
        req.flush(returnedFromService);
        expect(expectedResult).toMatchObject(expected);
      });

      it('should return a list of Recipe', () => {
        const returnedFromService = Object.assign(
          {
            name: 'BBBBBB',
            starred: true,
            tags: 'BBBBBB',
            ingredients: 'BBBBBB',
            steps: 'BBBBBB',
            comment: 'BBBBBB',
            duration: 'BBBBBB',
          },
          elemDefault
        );

        const expected = Object.assign({}, returnedFromService);

        service.query().subscribe(resp => (expectedResult = resp.body));

        const req = httpMock.expectOne({ method: 'GET' });
        req.flush([returnedFromService]);
        httpMock.verify();
        expect(expectedResult).toContainEqual(expected);
      });

      it('should delete a Recipe', () => {
        service.delete(123).subscribe(resp => (expectedResult = resp.ok));

        const req = httpMock.expectOne({ method: 'DELETE' });
        req.flush({ status: 200 });
        expect(expectedResult);
      });
    });

    afterEach(() => {
      httpMock.verify();
    });
  });
});
