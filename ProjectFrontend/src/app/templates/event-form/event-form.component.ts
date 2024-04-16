import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { EventService } from '../../services/event.service';
import { CategoryService } from '../../services/category.service';

import { Category } from '../../models/category.model';

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
  selectedFile: File | null = null;

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
        this.eventService.getEventById(this.eventId).subscribe({
          next: (event) => {
            if (!event) {
              this.router.navigate(['/error']);
            } else {
              this.eventForm.patchValue(event);
            }
          },
          error: () => {
            this.router.navigate(['/error']);
          }
        });
      }
    });
  }

  loadCategories(): void {
    this.categoryService.getAllCategories().subscribe({
      next: (categories: Category[]) => {
        this.categories = categories;
      },
      error: () => {
        this.router.navigate(['/error']);
      }
    });
  }

  onSubmit(): void {
    if (this.eventForm.invalid) {
      return; 
    }

    const formData = this.prepareData(this.eventForm.value);
    if (this.isEdit) {
      this.updateEvent(formData);
    } else {
      this.createEvent(formData);
    }
  }

  createEvent(formData: any): void {
    this.eventService.createEvent(formData).subscribe({
      next: (event) => {
        this.eventId = event.id; 
        if (this.selectedFile) {
          this.uploadEventImage(this.eventId, this.selectedFile);
        } else {
          this.router.navigate([`/event/${this.eventId}`]);
          alert('Evento creado sin imagen.');
        }
      },
      error: () => this.router.navigate(['/error'])
    });
  }

  updateEvent(formData: any): void {
    this.eventService.updateEvent(this.eventId, formData).subscribe({
      next: () => {
        if (this.selectedFile) {
          this.uploadEventImage(this.eventId, this.selectedFile);
        } else {
          this.router.navigate([`/event/${this.eventId}`]);
          alert('Evento actualizado sin cambiar la imagen.');
        }
      },
      error: () => this.router.navigate(['/error'])
    });
  }

  prepareData(formData: any) {
    const data = { ...formData };
  
    data.startDate = new Date(data.startDate).toISOString();
    data.endDate = new Date(data.endDate).toISOString();
  
    return data;
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input && input.files && input.files.length) {
      this.selectedFile = input.files[0];
    }
  }
  

  uploadEventImage(eventId: string, image: File): void {
    this.eventService.uploadEventImage(eventId, image).subscribe({
      next: () => {
        this.router.navigate([`/event/${this.eventId}`]);
        alert('Imagen subida y evento actualizado.');
      },
      error: () => {
        this.router.navigate(['/error']);
        alert('Error subiendo la imagen.');
      }
    });
  }
}
