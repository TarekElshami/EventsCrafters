import { Component, Input } from '@angular/core';
import { Event } from '../../models/event.model';
import { Router } from '@angular/router';
import { Category } from '../../models/category.model';
import { CategoryService } from '../../services/category.service'
import { faMapMarkerAlt } from '@fortawesome/free-solid-svg-icons';

@Component({
  selector: 'app-event-card',
  templateUrl: './event-card.component.html',
  styleUrls: ['../../home.css']
})
export class EventCardComponent {
  // Icons
  faMapMarkerAlt = faMapMarkerAlt;

  category: Category = { id: -1, name: '', color: '', eventIdInCategories: [] };

  @Input()
  eventCard!: Event;
  description: string = "";

  constructor(private categoryService: CategoryService, private router: Router) { }

  ngOnInit() {
    this.description = this.eventCard.description;
    if (this.description.length >= 100) {
      this.description = this.description.substring(0, 100)
      this.description = this.description.concat("...")
    }
    if (this.eventCard.id >= 0) {
      this.findCategory();
    }
  }

  findCategory() {
    this.categoryService.getCategoryById(this.eventCard.categoryId).subscribe({
      next: (data) => {
        this.category = data;
      },
      error: () => {
        this.router.navigate(['/error']);
      }
    });
  }

  viewDetails(id: number) {
    this.router.navigate(['/event/' + id]);
  }

}
