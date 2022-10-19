import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { RecipeDetailComponent } from './recipe-detail.component';

describe('Recipe Management Detail Component', () => {
  let comp: RecipeDetailComponent;
  let fixture: ComponentFixture<RecipeDetailComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [RecipeDetailComponent],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: { data: of({ recipe: { id: 123 } }) },
        },
      ],
    })
      .overrideTemplate(RecipeDetailComponent, '')
      .compileComponents();
    fixture = TestBed.createComponent(RecipeDetailComponent);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('Should load recipe on init', () => {
      // WHEN
      comp.ngOnInit();

      // THEN
      expect(comp.recipe).toEqual(expect.objectContaining({ id: 123 }));
    });
  });
});
