import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { EventService } from '../../services/event.service'
import { PageEvent } from '../../models/pageEvent.model';
import { Event } from '../../models/event.model';

@Component({
  selector: 'app-index',
  templateUrl: './index.component.html',
  styleUrls: ['../../home.css', './index.component.css']
})
export class IndexComponent {

  pageEvent: PageEvent = {events:  [], page: 0, totalPages: 0}
  eventsInstances: Event[] = [];
  moreBtnVisible = true;
  isLoading = true;
  type: string = 'recommended';
  searchBarInput: string = '';
  filterByCategoryId: number = -1;

  constructor(private eventService: EventService, private router: Router){}

  ngOnInit(){
    this.isLoading= true;
    this.eventService.getFilteredEvents(0, 'recommended', '', -1).subscribe({
      next: (data) => {
        this.pageEvent = data;
        this.eventsInstances = this.pageEvent.events;
        if (1 >= this.pageEvent.totalPages){
          this.moreBtnVisible = false;
        } else {
          this.moreBtnVisible = true;
        }
      },
      error: () => {
        this.router.navigate(['/error']); 
      }
    });
    this.isLoading= false;
  }

  newPage(){
    this.isLoading= true;
    this.eventService.getFilteredEvents(this.pageEvent.page+1, this.type, this.searchBarInput, -1).subscribe({
      next: (data) => {
        this.pageEvent = data;
        this.eventsInstances = this.eventsInstances.concat(this.pageEvent.events)
        if (this.pageEvent.page+1 >= this.pageEvent.totalPages){
          this.moreBtnVisible = false;
        }
      },
      error: () => {
        this.router.navigate(['/error']); 
      }
    });
    this.isLoading= false;
  }

  filterBySearchBar(input: string){
    console.log("hola");
    this.type = 'searchBar';
    this.searchBarInput = input;
    this.eventService.getFilteredEvents(0, this.type, this.searchBarInput, -1).subscribe({
      next: (data) => {
        this.pageEvent = data;
        this.eventsInstances = this.pageEvent.events
        if (1 >= this.pageEvent.totalPages){
          this.moreBtnVisible = false;
        } else {
          this.moreBtnVisible = true;
        }
      },
      error: () => {
        this.router.navigate(['/error']); 
      }
    });
    this.isLoading= false;
  }


}
