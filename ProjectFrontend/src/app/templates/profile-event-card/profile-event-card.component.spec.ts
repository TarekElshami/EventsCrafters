import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProfileEventCardComponent } from './profile-event-card.component';

describe('ProfileEventCardComponent', () => {
  let component: ProfileEventCardComponent;
  let fixture: ComponentFixture<ProfilProfileEventCardComponenteComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ProfileEventCardComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ProfileEventCardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
