import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { EventService } from '../../services/event.service';
import { CategoryService } from '../../services/category.service';

interface Category {
  id: number;
  name: string;
}

@Component({
  selector: 'app-event-form',
  templateUrl: './event-form.component.html',
  styleUrls: ['./event-form.component.css', '../../forms.css']
})
export class EventFormComponent implements OnInit {
  eventForm: FormGroup;
  categories: Category[] = [];
  isEdit = false;
  eventId = '';

  constructor(
    private fb: FormBuilder,
    private eventService: EventService,
    private categoryService: CategoryService,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.eventForm = this.fb.group({
      name: ['', Validators.required],
      description: ['', Validators.required],
      maxCapacity: ['', [Validators.required, Validators.min(1)]],
      price: ['', [Validators.required, Validators.min(0)]],
      location: ['', Validators.required],
      map: ['', Validators.required],
      startDate: ['', Validators.required],
      endDate: ['', Validators.required],
      additionalInfo: ['', Validators.required],
      categoryId: ['', Validators.required]
    });

    this.loadCategories();
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.isEdit = true;
        this.eventId = params['id'];
        this.eventService.getEventById(this.eventId).subscribe(event => {
          this.eventForm.patchValue(event);
        });
      }
    });
  }

  loadCategories(): void {
    this.categoryService.getCategories().subscribe({
      next: (categories: Category[]) => {
        this.categories = categories;
      },
      error: (error) => {
        console.error('Error loading categories:', error);
      }
    });
  }

  onSubmit(): void {
    if (this.eventForm.invalid) {
      return; 
    }

    const formData = this.prepareData(this.eventForm.value);

    if (this.isEdit) {
      // Update existing event
      this.eventService.updateEvent(this.eventId, formData).subscribe({
        next: () => {
          this.router.navigate(['/']);
        },
        error: (error) => {
          console.error('Error updating event:', error);
        }
      });
    } else {
      // Create new event
      this.eventService.createEvent(formData).subscribe({
        next: () => {
          this.router.navigate(['/']);
        },
        error: (error) => {
          console.error('Error creating event:', error);
        }
      });
    }
  }

  prepareData(formData: any) {
    const data = { ...formData };
  
    data.startDate = new Date(data.startDate).toISOString();
    data.endDate = new Date(data.endDate).toISOString();
  
    return data;
  }
}
