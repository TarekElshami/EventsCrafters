import { Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { EventService } from '../../services/event.service';
import { EventGraphData } from '../../models/event-graph-data.model';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Event } from '../../models/event.model';
import { Category } from "../../models/category.model";
import { CategoryService } from '../../services/category.service';
import { UserService } from '../../services/user.service';
import { User } from "../../models/user.model";
import { EventStats } from "../../models/eventLiveStats.model";
import { DomSanitizer, SafeResourceUrl } from "@angular/platform-browser";
import { faUsers, faMapMarkerAlt, faEuroSign, faClock, faHourglassHalf, faInfoCircle, faTrashAlt, faEdit, faTicketAlt, faClipboardList, faStar, faSave, faChartPie, faStarHalfAlt } from '@fortawesome/free-solid-svg-icons';

@Component({
  selector: 'app-view-events',
  templateUrl: './view-events.component.html',
  styleUrls: ['./view-events.component.css']
})
export class ViewEventsComponent {

  // Icons
  faUsers = faUsers;
  faMapMarkerAlt = faMapMarkerAlt;
  faEuroSign = faEuroSign;
  faClock = faClock;
  faHourglassHalf = faHourglassHalf;
  faInfoCircle = faInfoCircle;
  faTrashAlt = faTrashAlt;
  faEdit = faEdit;
  faTicketAlt = faTicketAlt;
  faClipboardList = faClipboardList;
  faStar = faStar;
  faSave = faSave;
  faChartPie = faChartPie;
  faStarHalfAlt = faStarHalfAlt;

  //Miscellaneous
  isLoading: boolean = false;
  mapHtml!: SafeResourceUrl;

  //Graph data
  graphData?: EventGraphData;
  showForm = false;
  attendeeForm!: FormGroup;
  gradient: boolean = false;
  colorScheme = 'vivid';
  chartData: any[] = [];
  attendeesCountSet: boolean = false;

  //Event
  requestedEventId: number = 0;
  event!: Event;
  category: Category = { id: -1, name: '', color: '', eventIdInCategories: [] };
  creator!: User;
  stats: EventStats = { eventFinished: false, hasUserJoined: false, hasUserReviewed: false };

  //User
  currentUserId: number = 0;
  isUserLogged: boolean = false;
  isUserAdmin: boolean = false;
  isUserCreator: boolean = false;


  constructor(
    private sanitizer: DomSanitizer,
    private eventService: EventService,
    private categoryService: CategoryService,
    private userService: UserService,
    private route: ActivatedRoute,
    private formBuilder: FormBuilder,
    private router: Router
  ) {

  }

  ngOnInit() {
    this.isLoading = true;
    this.attendeesCountSet = false;

    const eventIdString = this.route.snapshot.paramMap.get('id');
    if (eventIdString) {
      this.requestedEventId = +eventIdString;
      if (this.requestedEventId > 0) {
        this.eventService.getEventById(eventIdString).subscribe({
          next: (event) => {
            this.event = event;
            this.mapHtml = this.sanitizer.bypassSecurityTrustHtml(this.event.map);
            this.findCategory();
          },
          error: (error) => {
            console.error('Error al obtener el evento:', error);
            this.router.navigate(['/error']);
          }
        });
      }
    }
  }

  getEventLiveStats() {
    this.eventService.getEventLiveStats(this.requestedEventId).subscribe({
      next: (eventStats) => {
        this.stats = eventStats;
        this.isLoading = false;
      },
      error: (error) => {
        this.router.navigate(['/error']);
      }
    });
  }

  loadGraphData() {
    this.eventService.getEventGraphData(this.event.id).subscribe({
      next: (data) => {
        if (data.registeredUsers === -1) {
          this.showForm = true;
        } else {
          this.graphData = data;
          this.updateChartData();
          this.showForm = false;
        }
      },
      error: () => {
        console.log("error");
      }
    });
  }

  findCategory() {
    this.categoryService.getCategoryById(this.event.categoryId).subscribe({
      next: (data) => {
        this.category = data;
        this.getCreatorData();
      },
      error: () => {
        this.router.navigate(['/error']);
      }
    });
  }

  updateChartData() {
    if (this.graphData) {
      this.chartData = [
        {
          "name": "Attendees",
          "value": this.graphData.attendeesCount
        },
        {
          "name": "Did Not Attend",
          "value": this.graphData.didNotAttend
        }
      ];
    }
  }

  updateAttendees() {
    if (this.attendeeForm.valid) {
      this.isLoading = true;
      const attendees = this.attendeeForm.get('attendees')?.value;
      this.eventService.updateEventAttendees(this.event.id, attendees).subscribe({
        next: () => {
          this.loadGraphData();
          this.isLoading = false;
        },
        error: () => {
          this.isLoading = false;
          this.router.navigate(['/error']);
        }
      });
    }
  }

  getCreatorData() {
    this.userService.getUser(this.event.creatorId).subscribe({
      next: (creator) => {
        this.creator = creator;
        this.getLoggedUserData(); //Updates isLogged, isAdmin and isCreator, which are initially false
      },
      error: (error) => {
        this.router.navigate(['/error']);
      }
    });
  }


  getLoggedUserData() {
    this.userService.getCurrentUser().subscribe({
      next: (currentUser) => {
        this.isUserLogged = true;
        this.isUserAdmin = currentUser.roles.includes("ADMIN");
        this.isUserCreator = (this.event.creatorId == currentUser.id);
        if (this.isUserAdmin || this.isUserCreator) {
          this.attendeeForm = this.formBuilder.group({
            attendees: ['', [Validators.required, Validators.min(0), Validators.max(this.event.numRegisteredUsers)]]
          });
          this.loadGraphData();
        }
        this.getEventLiveStats();
      },
      error: (error) => {
        this.isUserLogged = false;
        this.getEventLiveStats();
      }
    });
  }

  onDeleteClick(): void {
    const confirmation = window.confirm('¿Estás seguro de que deseas borrar este evento?');
    if (confirmation) {
      this.isLoading = true;
      this.eventService.deleteEvent(this.event.id).subscribe({
        next: () => {
          this.router.navigate(['/']);
          this.isLoading = false;
        },
        error: () => {
          this.router.navigate(['/error']);
          this.isLoading = false;
        }
      });
    }
  }

  onEditClick() {
    this.router.navigate([`/event/edit/${this.event.id}`]);
  }

  eventTicket() {
    this.router.navigate([`/event/${this.event.id}/ticket`]);
  }

  joinEvent() {
    this.isLoading = true;
    this.eventService.joinToEvent(this.event.id).subscribe({
      next: () => {
        this.stats.hasUserJoined = true;
        this.event.numRegisteredUsers++;
        this.isLoading = false;
      },
      error: () => this.router.navigate(['/error'])
    });
  }

  leaveEvent() {
    this.isLoading = true;
    this.eventService.leaveAnEvent(this.event.id).subscribe({
      next: () => {
        this.stats.hasUserJoined = false;
        this.event.numRegisteredUsers--;
        this.isLoading = false;
      },
      error: () => this.router.navigate(['/error'])
    });
  }

  joinEventNotLogged() {
    this.router.navigate([`/login`]);
  }

  makeReview() {
    this.router.navigate([`/review/${this.event.id}`]);
  }


  scrollToLocation(): void {
    const locationElement = document.getElementById('map');
    if (locationElement) {
      locationElement.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  }

}

