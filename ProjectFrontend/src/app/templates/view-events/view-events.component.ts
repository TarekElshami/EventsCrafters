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

@Component({
  selector: 'app-view-events',
  templateUrl: './view-events.component.html',
  styleUrls: ['./view-events.component.css']
})
export class ViewEventsComponent {

  //Miscellaneous
  isLoading: boolean = false;

  //Graph data
  graphData?: EventGraphData;
  showForm = false;
  attendeeForm!: FormGroup;
  gradient: boolean = false;
  colorScheme = 'vivid';
  chartData: any[] = [];
  attendeesCountSet : boolean = false;

  //Event
  event!: Event;
  category: Category = {id : -1, name : '', color : '', eventIdInCategories: []};
  creator! : User;
  stats!: EventStats;

  //User
  isUserAdmin!: boolean;


  constructor(
    private eventService: EventService,
    private categoryService: CategoryService,
    private userService: UserService,
    private route: ActivatedRoute,
    private formBuilder: FormBuilder,
    private router: Router
  ) {}

  ngOnInit() {
    this.isLoading = true;
    this.attendeesCountSet = false;

    const eventIdString = this.route.snapshot.paramMap.get('id');
    if (eventIdString) {
      let eventIdNum = +eventIdString;
      if (eventIdNum > 0) {

        this.eventService.getEventLiveStats(eventIdNum).subscribe({
          next: (eventStats) => {
            this.stats = eventStats;
          },
          error: (error) => {
            this.router.navigate(['/error']);
          }
        });

        this.eventService.getEventById(eventIdString).subscribe({
          next: (event) => {
            this.event = event;
          },
          error: (error) => {
            console.error('Error al obtener el evento:', error);
            this.router.navigate(['/error']);
          }
        });
        this.findCategory();

      }
    }

    this.userService.getCurrentUser().subscribe({
      next: (currentUser) => {
        this.isUserAdmin = currentUser.roles.includes("ADMIN");
        if (this.isUserAdmin || this.stats.isCreator) {
          this.loadGraphData();
        }
      },
      error: (error) => {
        this.isLoading = false;
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

  findCategory(){
    this.categoryService.getCategoryById(this.event.categoryId).subscribe({
      next: (data) => {
        this.category = data;
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
      const attendees = this.attendeeForm.get('attendees')?.value;
      this.eventService.updateEventAttendees(this.event.id, attendees).subscribe({
        next: () => this.loadGraphData(),
        error: () => this.router.navigate(['/error'])
      });
    }
  }

  onDeleteClick(): void {
    this.eventService.deleteEvent(this.event.id).subscribe({
      next: () => this.router.navigate(['/']),
      error: () => this.router.navigate(['/error'])
    });
  }


  leaveEvent() {

  }

  eventTicket() {

  }

  joinEvent() {

  }

  onEditClick() {

  }
}
