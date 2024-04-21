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
  events: Event[] = [];
  moreBtnVisible = true;
  isLoading = true;
  type: string = 'recommended';
  searchBarInput: string = '';
  categoryId: number = -1;
  noEvents: boolean = false;
  isUserLogged: boolean = true;

  constructor(private eventService: EventService, private router: Router){}

  ngOnInit(){
    this.isLoading= true;
    this.noEvents = false;
    this.eventService.getFilteredEvents(0, 'recommended', '', -1).subscribe({
      next: (data) => {
        this.pageEvent = data;
        this.events = this.pageEvent.events;

        this.checkFirstPage();

      },
      error: () => {
        this.router.navigate(['/error']); 
      }
    });
    
  }

  newPage(){
    this.isLoading= true;
    this.eventService.getFilteredEvents(this.pageEvent.page+1, this.type, this.searchBarInput, this.categoryId).subscribe({
      next: (data) => {
        this.pageEvent = data;
        this.events = this.events.concat(this.pageEvent.events)
        
        if (this.pageEvent.page+1 >= this.pageEvent.totalPages){
          this.moreBtnVisible = false;
        }

        this.isLoading= false;
      },
      error: () => {
        this.router.navigate(['/error']); 
      }
    });
    
  }

  filterBySearchBar(input: string){
    this.isLoading= true;
    this.type = 'searchBar';
    this.searchBarInput = input;
    this.noEvents = false;
    this.eventService.getFilteredEvents(0, this.type, this.searchBarInput, this.categoryId).subscribe({
      next: (data) => {
        this.pageEvent = data;
        this.events = this.pageEvent.events

        this.checkFirstPage();

      },
      error: () => {
        this.router.navigate(['/error']); 
      }
    });
    
  }

  FilterByCategoryId(id: number){
    this.isLoading= true;
    this.type = 'category';
    this.categoryId = id;
    this.noEvents = false;
    this.eventService.getFilteredEvents(0, this.type, this.searchBarInput, this.categoryId).subscribe({
      next: (data) => {
        this.pageEvent = data;
        this.events = this.pageEvent.events

        this.checkFirstPage();

      },
      error: () => {
        this.router.navigate(['/error']); 
      }
    });
  }

  checkFirstPage(){
    if (this.pageEvent.events.length == 0){
      this.noEvents = true;
    }

    if (1 >= this.pageEvent.totalPages){
      this.moreBtnVisible = false;
    } else {
      this.moreBtnVisible = true;
    }

    this.isLoading= false;
  }


}
