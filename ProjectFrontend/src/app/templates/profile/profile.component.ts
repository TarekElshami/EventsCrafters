import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';
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
import { PageCategory } from '../../models/pageCategory.model';
import { ProfileGraphData } from '../../models/profile-graph-data.model';

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
  eventPage!: PageEvent;
  events1: ProfileEventCard = {events: [], categories: [], areThereEvents: false, loadMore: false}
  events2: ProfileEventCard = {events: [], categories: [], areThereEvents: false, loadMore: false}
  events3: ProfileEventCard = {events: [], categories: [], areThereEvents: false, loadMore: false}
  events4: ProfileEventCard = {events: [], categories: [], areThereEvents: false, loadMore: false}
  
  categories: Category[] = [];
  pageCategory!: PageCategory;

  tagLoadMoreBtn: boolean = true;
  areThereTags: boolean = true;
  showTagPopUp: boolean = false;
  categoryForm: FormGroup;
  showDeleteBtn: boolean = false;
  isEditing: boolean = false;
  categoryId: number = -1;

  graphData?: ProfileGraphData;
  chartData: any[] = [];
  view: [number, number] = [900, 400]; // width, height
  showXAxis = true;
  showYAxis = true;
  gradient = false;
  showLegend = true;
  showXAxisLabel = true;
  xAxisLabel = 'Etiqueta';
  showYAxisLabel = true;
  yAxisLabel = 'Número';


  constructor(
    private userService: UserService, 
    private router: Router,
    private eventService: EventService, 
    private categoryService: CategoryService,
    private fb: FormBuilder,
  ) {
    this.categoryForm = this.fb.group({
      name: ['', Validators.required],
      color: ['', Validators.required],
      eventIdInCategories: []
    })
  }

  ngOnInit(){
    
    this.userService.login('admin', 'adminpass').pipe(
      switchMap(() => this.userService.getCurrentUser()),
      switchMap((currentUser: User) => {
        this.currentUser = currentUser;
        console.log(currentUser.photo)
        this.isAdmin = currentUser.roles.includes("ADMIN");
        if (this.isAdmin){
          this.loadGraphData();
        }
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
          if (this.isAdmin){
            this.eventPage = eventsData;
          }else{
            this.eventsPages = eventsData;
          }
          this.processEventsData();
        }
      },
      error: (error) => {
        this.router.navigate(['/error']);
      }
    });
    
  }


  processEventsData() {
    if (!this.isAdmin) {
      this.processEventCard(this.events1, this.eventsPages[0]);
      this.processEventCard(this.events2, this.eventsPages[1]);
      this.processEventCard(this.events3, this.eventsPages[2]);
      this.processEventCard(this.events4, this.eventsPages[3]);
    } else if (this.isAdmin) {
      this.findCategories();
      this.events1.events = this.eventPage.events;
      this.processEventCard(this.events1, this.eventPage);
    }
  }

  processEventCard(eventCard: ProfileEventCard, pageData: PageEvent) {
    eventCard.events = pageData.events;
    if (eventCard.events.length === 0) {
      eventCard.areThereEvents = false;
      eventCard.loadMore = false;
    } else {
      eventCard.areThereEvents = true;
      eventCard.loadMore = pageData.totalPages > 1;
    }

  }

  nextEvent(i:number){
    let page: number = -1;
    let type: string = ''; 
    let time: string = '';
    let events!: ProfileEventCard;
    let pageEvent!: PageEvent;
    switch(i){
      case 1:
        if (!this.isAdmin){
          time = 'present';
          type = 'created';
          pageEvent = this.eventsPages[0];
        } else{
          pageEvent = this.eventPage;
        }
        events = this.events1;
        page = pageEvent.page + 1;
        break;
      case 2:
        time = 'past';
        type = 'created'
        pageEvent = this.eventsPages[1]
        page = pageEvent.page + 1;
        events = this.events2
        break;
      case 3: 
        time = 'present';
        type = 'registered'
        pageEvent = this.eventsPages[2]
        page = pageEvent.page + 1;
        events = this.events3;
        break;
      case 4: 
        time = 'past';
        type = 'registered'
        pageEvent = this.eventsPages[3]
        page = pageEvent.page + 1;
        events = this.events4;
        break;
      default:
      this.router.navigate(['/error']); 
      return;
    }
    
    if (!pageEvent || !events) {
      this.router.navigate(['/error']);
      return;
    }

    this.eventService.userEventRequest(page, type, time).subscribe({
      next: (data) =>{
        pageEvent =data;
        events.events = events.events.concat(pageEvent.events);
        events.loadMore = page+1 < data.totalPages
      },
      error: ()=>{
        this.router.navigate(['/error']); 
      }
    })
  }

  nextCategory(){
    let page = this.pageCategory.page + 1;
    this.categoryService.getCategories(page).subscribe({
      next: (data) =>{
        this.pageCategory =data;
        this.categories = this.categories.concat(this.pageCategory.categories);
      
        this.tagLoadMoreBtn = page+1 < data.totalPages;
        
      },
      error: ()=>{
        this.router.navigate(['/error']); 
      }
    })
  }

  findCategories(){
      this.categoryService.getCategories(0).subscribe({
        next: (data) =>{
          this.pageCategory =data;
          this.categories = this.pageCategory.categories;
          if (this.categories.length === 0){
            this.areThereTags = false;
            this.tagLoadMoreBtn = false;
          }else {
            this.tagLoadMoreBtn =this.pageCategory.totalPages > 1;
          } 
          
        },
        error: ()=>{
          this.router.navigate(['/error']); 
        }
      })
  }

  showPopUp(id: number){
    if (id != -1){
      this.categoryService.getCategoryById(id).subscribe({
        next: (category) => {
          if (!category) {
            this.router.navigate(['/error']);
          } else {
            this.categoryId = id;
            this.isEditing = true;
            this.showDeleteBtn = true;
            this.categoryForm.patchValue({
              name: category.name,
              color: category.color
            })
          }
        },
        error: () => {
          this.router.navigate(['/error']);
        }
      });
      
    }
    this.showTagPopUp = true;
  }

  closePopUp(){
    this.categoryForm.patchValue({
      name: '',
      color: ''
    })
    this.categoryId = -1;
    this.isEditing = false;
    this.showDeleteBtn = false;
    this.showTagPopUp = false;
  }

  onSubmit(): void {
    if (this.categoryForm.invalid) {
      return; 
    }

    const formData = this.prepareData(this.categoryForm.value);
    if (this.isEditing) {
      this.updateCategory(formData);
    } else {
      this.createCategory(formData);
    }
    location.reload();
  }

  prepareData(categoryData: any) {
    const data = { ...categoryData };
    data.eventIdInCategories = []
    return data;
  }

  updateCategory(categoryData: any){
    this.categoryService.updateCategory(this.categoryId, categoryData).subscribe({
      next: () => {
        this.closePopUp()
      },
      error: () => this.router.navigate(['/error'])
    });
  }

  createCategory(categoryData: any){
    this.categoryService.createCategory(categoryData).subscribe({
      next: () => {
        this.closePopUp()
      },
      error: () => this.router.navigate(['/error'])
    });
  }

  warn(){
    let confirmed = window.confirm("¿Estas seguro de que quieres borrar la categoria?")
  
    if (confirmed){
      this.deleteCategory();
      location.reload();
    }
  } 

  deleteCategory(){
    this.categoryService.deleteCategory(this.categoryId).subscribe({
      next: () => {
        this.closePopUp()
      },
      error: () => this.router.navigate(['/error'])
    });
  }

  loadGraphData() {
    
    this.eventService.getProfileGraphData().subscribe({
      next: (data) => {
          this.graphData = data;
          this.updateChartData();
          console.log(this.chartData);
      },
      error: () => {
        console.log("error");
      }
    });
  }

  updateChartData() {
    if (this.graphData) {
      const newChartData = [];
      for (let i = 0; i < this.graphData.data.length; i++) {
        let value = this.graphData.data[i];
        let name = this.graphData.labels[i];
        newChartData.push({ name: name, value: parseInt(value, 10) });
      }
      this.chartData = [...newChartData];
    }
  }


}
