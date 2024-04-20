import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { UserService } from '../../services/user.service';
import { EventService } from '../../services/event.service';
import { CategoryService } from '../../services/category.service';
import { User } from '../../models/user.model';
import { PageEvent } from '../../models/pageEvent.model';
import { Event } from '../../models/event.model';
import { Category } from '../../models/category.model';
import { ProfileEventCard } from '../../models/profileEventCard.model';
import { switchMap, catchError } from 'rxjs/operators';
import { Observable, of } from 'rxjs';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent {

  isAdmin: boolean = false;
  role!: string;
  currentUser!: User;
  eventsPages: PageEvent[] = []
  events1: ProfileEventCard = {events: [], categories: [], areThereEvents: false, loadMore: false}
  events2: ProfileEventCard = {events: [], categories: [], areThereEvents: false, loadMore: false}
  events3: ProfileEventCard = {events: [], categories: [], areThereEvents: false, loadMore: false}
  events4: ProfileEventCard = {events: [], categories: [], areThereEvents: false, loadMore: false}
  category!: Category;

  constructor(
    private userService: UserService, 
    private router: Router,
    private eventService: EventService, 
    private categoryService: CategoryService
  ) {}

  ngOnInit(){
    
    this.userService.login('user2', 'pass').pipe(
      switchMap(() => this.userService.getCurrentUser()),
      switchMap((currentUser: User) => {
        this.currentUser = currentUser;
        this.isAdmin = currentUser.roles.includes("ADMIN");
        this.role = this.isAdmin ? 'admin' : 'user';
        return this.eventService.getProfileEvents(this.role); 
      }),
      catchError(error => {
        console.error('Error fetching data: ', error);
        this.router.navigate(['/error']);
        return of(null); 
      })
    ).subscribe({
      next: (eventsData) => {
        if (eventsData) {
          this.eventsPages = eventsData;
          if (!this.isAdmin){
            this.events2.events = this.eventsPages[1].events;
            if (this.events2.events.length == 0){
              this.events2.areThereEvents = false;
              this.events2.loadMore = false;
            }else {
              this.events2.areThereEvents = true;
              if (this.eventsPages[1].totalPages <= 1){
                this.events2.loadMore = false;
              } else {
                this.events2.loadMore = true;
              }
            }
            this.events3.events = this.eventsPages[2].events;
            if (this.events3.events.length == 0){
              this.events3.areThereEvents = false;
              this.events3.loadMore = false;
            }else {
              this.events3.areThereEvents = true;
              if (this.eventsPages[2].totalPages <= 1){
                this.events3.loadMore = false;
              } else {
                this.events3.loadMore = true;
              }
            }
            this.events4.events = this.eventsPages[3].events;
            if (this.events4.events.length == 0){
              this.events4.areThereEvents = false;
              this.events4.loadMore = false;
            }else {
              this.events4.areThereEvents = true;
              if (this.eventsPages[3].totalPages <= 1){
                this.events4.loadMore = false;
              } else {
                this.events4.loadMore = true;
              }
            }
          }
          this.events1.events = this.eventsPages[0].events;
          if (this.events1.events.length == 0){
            this.events1.areThereEvents = false;
            this.events1.loadMore = false;
          }else {
            this.events1.areThereEvents = true;
            if (this.eventsPages[0].totalPages <= 1){
              this.events1.loadMore = false;
            } else {
              this.events1.loadMore = true;
            }
          }
          
        }
      },
      error: (error) => {
        this.router.navigate(['/error']);
      }
    });
  }


  async findCategory(id: number): Promise<Category | undefined> {
    try {
        let category = await this.categoryService.getCategoryById(id).toPromise();
        return category;
    } catch (error) {
        this.router.navigate(['/error']);
        throw error;
    }
  }

}
