import { Component } from '@angular/core';
import { Router } from '@angular/router';
import {
  FormBuilder,
  FormGroup,
  Validators,
  AbstractControl,
  ValidationErrors,
  ValidatorFn,
  AsyncValidatorFn
} from '@angular/forms';
import { UserService } from '../../services/user.service';
import { EventService } from '../../services/event.service';
import { CategoryService } from '../../services/category.service';
import { User } from '../../models/user.model';
import { PageEvent } from '../../models/pageEvent.model';
import { Event } from '../../models/event.model';
import { Category } from '../../models/category.model';
import { ProfileEventCard } from '../../models/profileEventCard.model';
import { switchMap, catchError } from 'rxjs/operators';
import {map, Observable, of} from 'rxjs';
import { PageCategory } from '../../models/pageCategory.model';
import { ProfileGraphData } from '../../models/profile-graph-data.model';
import {HttpErrorResponse, HttpResponse} from "@angular/common/http";
import {error} from "@angular/compiler-cli/src/transformers/util";

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent {

  isLoading: boolean = false;
  isAdmin: boolean = false;
  role!: string;
  currentUser!: User;

  eventsPages: PageEvent[] = []
  eventPage!: PageEvent;
  events1: ProfileEventCard = {events: [], areThereEvents: false, loadMore: false, loadingEvents: false}
  events2: ProfileEventCard = {events: [], areThereEvents: false, loadMore: false, loadingEvents: false}
  events3: ProfileEventCard = {events: [], areThereEvents: false, loadMore: false, loadingEvents: false}
  events4: ProfileEventCard = {events: [], areThereEvents: false, loadMore: false, loadingEvents: false}

  categories: Category[] = [];
  pageCategory!: PageCategory;

  tagLoadMoreBtn: boolean = true;
  areThereTags: boolean = true;
  showTagPopUp: boolean = false;
  categoryForm: FormGroup;
  showDeleteBtn: boolean = false;
  isEditing: boolean = false;
  categoryId: number = -1;
  loadingTags: boolean = false;

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

  editProfileForm: FormGroup;
  banUserForm: FormGroup;
  unbanUserForm: FormGroup;
  editingProfile = false;
  currentName:string = "";
  currentEmail:string = "";
  currentUsername:string = "";
  banFormOpen: boolean = false;
  unbanFormOpen: boolean = false;

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
    this.editProfileForm = this.fb.group({
      name: [this.currentName, Validators.required],
      email: [this.currentEmail, [Validators.required, Validators.email]],
      username: [this.currentUsername, { validators: [Validators.required], asyncValidators: [this.userNameTaken()], updateOn: 'blur' }]
    })
    this.banUserForm = this.fb.group({
      banUser: ['', Validators.required]
    })
    this.unbanUserForm = this.fb.group({
      unbanUser: ['', Validators.required]
    })
  }

  ngOnInit(){
    this.isLoading = true;
    this.userService.getCurrentUser().pipe(
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
          this.currentName = this.currentUser.name;
          this.currentEmail = this.currentUser.email;
          this.currentUsername = this.currentUser.username;
          if (this.isAdmin){
            this.eventPage = eventsData;
          }else{
            this.eventsPages = eventsData;
          }
          this.processEventsData();
        }
        this.isLoading= false;
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

    events.loadingEvents = true;

    this.eventService.userEventRequest(page, type, time).subscribe({
      next: (data) =>{
        events.loadingEvents = false;
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
    this.loadingTags = true;
    let page = this.pageCategory.page + 1;
    this.categoryService.getCategories(page).subscribe({
      next: (data) =>{
        this.loadingTags = false;
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

  onSubmitProfile(){
    let confirmed = window.confirm("Por razones de seguridad, si has modificado tu nombre de usuario, se te cerrará sesión. ¿Quieres continuar?")

    if (!confirmed){
      this.editingProfile = false;
      //location.reload();
      return;
    }

    //asegurarse de no cerrar sesión
    this.userService.update({
      id: this.currentUser.id,
      name:this.currentName,
      username:this.currentUsername,
      email:this.currentEmail
    }).subscribe({
      next: (response) => {this.router.navigate(["/login"])},
      error: (error) => {alert("Ha habido un error al editar el perfil.");}
    }
    )
    this.editingProfile = false;
  }

  warnProfile(){
    let confirmed = window.confirm("Cuidao que te cerramos la sesión")

    if (confirmed){
      this.editingProfile = false;
      //location.reload();
    }
    return confirmed
  }
  cancel() {
    //window.location.href = 'profile';
    this.currentName = this.currentUser.name;
    this.currentEmail = this.currentUser.email;
    this.currentUsername = this.currentUser.username;
    this.editingProfile = false;
    return false;
  }

  activateEditing() {
    this.editingProfile=true;
  }

  userNameTaken(): AsyncValidatorFn {
    return (control: AbstractControl) => {
      const userName = control.value;
      if (userName===this.currentUser.username) return of(null);
      return this.userService.usernameTaken(userName).pipe(
        map(
          (isTaken: HttpResponse<any>) => {
            if (isTaken) {
              return {userNameTaken:true};
            } else {
              return null
            }
          }
        )
      )
    }
  }

  deleteUser() {
    const result = confirm("Si continua, se eliminará la cuenta. ¿Está seguro?");
    if (!result) {
      alert("Operación cancelada");
      return;
    }
    this.userService.delete(this.currentUser.id).subscribe({
      next: (response) => {
        alert("La cuenta ha sido eliminada");
        this.router.navigate(["/"]);
      },
      error: (error: HttpErrorResponse) => {
        if (error.status===403) {
          alert("No tienes permiso para borrar esta cuenta.");
        } else if (error.status === 404) {
          alert("No se ha encontrado la cuenta a borrar.")
        }
      }
    });



  }

  onSubmitBan() {
    this.userService.ban(this.banUserForm.get('banUser')?.value).subscribe({
      next: (response) => {
        alert("Usuario baneado.");
        this.banFormOpen = false;
        this.banUserForm.get('banUser')?.setValue("");
      },
      error: (error: HttpErrorResponse) => {
        if (error.status === 403) alert("No tienes permiso para banear a este usuario.")
        else if (error.status == 404) alert("No se ha encontrado el usuario a banear.")
        else alert("Error al banear al usuario.")
      }
    });
  }

  onSubmitUnban() {
    this.userService.unban(this.unbanUserForm.get('unbanUser')?.value).subscribe({
      next: (response) => {
        alert("Usuario desbaneado.");
        this.unbanFormOpen = false;
        this.unbanUserForm.get('unbanUser')?.setValue("");
      },
      error: (error: HttpErrorResponse) => {
        if (error.status === 403) alert("No tienes permiso para desbanear a este usuario.")
        else if (error.status == 404) alert("No se ha encontrado el usuario a desbanear.")
        else alert("Error al desbanear al usuario.")
      }
    });
  }

  warnBan() {
    return confirm("Seguro que quieres banear a este usuario?");
  }

  warnUnBan() {
    return confirm("Seguro que quieres desbanear a este usuario?");
  }
}
