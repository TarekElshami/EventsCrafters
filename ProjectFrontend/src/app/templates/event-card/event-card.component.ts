import { Component, Input } from '@angular/core';
import { Event } from '../../models/event.model';
import { Router } from '@angular/router';
import { Category } from '../../models/category.model';
import { CategoryService } from '../../services/category.service'

@Component({
  selector: 'app-event-card',
  templateUrl: './event-card.component.html',
  styleUrls: ['../../home.css']
})
export class EventCardComponent {

    category: Category = {id : -1, name : '', color : ''};
  
    @Input()
    eventCard!: Event;

    constructor(private categoryService: CategoryService, private router: Router){}

    ngOnInit(){
        if (this.eventCard.id >= 0){
            this.findCategory();
        }
    }

    findCategory(){
        this.categoryService.getCategoryById(this.eventCard.categoryId).subscribe({
            next: (data) => {
              this.category = data;
            },
            error: () => {
              this.router.navigate(['/error']); 
            }
          });
    }

    viewDetails(id: number){
      this.router.navigate(['/event/'+id]);
    }
  
}