import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { EventService } from '../../services/event.service';
import { EventGraphData } from '../../models/event-graph-data.model';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Event } from '../../models/event.model';
import {Category} from "../../models/category.model";
import { CategoryService } from '../../services/category.service';
import { UserService } from '../../services/user.service';
import {User} from "../../models/user.model";

@Component({
  selector: 'app-view-events',
  templateUrl: './view-events.component.html',
  styleUrls: ['./view-events.component.css']
})
export class ViewEventsComponent implements OnInit {
  eventId!: number;
  graphData?: EventGraphData;
  showForm = false;
  attendeeForm!: FormGroup;

  gradient: boolean = false;
  colorScheme = 'vivid';
  chartData: any[] = [];

  event!: Event;
  category: Category = {id : -1, name : '', color : ''};
  creator! : User;
  token : any;

  constructor(
    private eventService: EventService,
    private route: ActivatedRoute,
    private formBuilder: FormBuilder,
    private categoryService: CategoryService,
    private userService: UserService,
    private router: Router
  ) {}

  ngOnInit() {
    const eventIdString = this.route.snapshot.paramMap.get('id');
    this.token = localStorage.getItem('token');
    if (eventIdString) {
      let eventIdNum = +eventIdString;
      if (eventIdNum >= 0){
        this.findCategory();
      }
      this.eventService.getEventById(eventIdString).subscribe({
        next: (data: Event) => {
          this.event = data;
          this.findCreator();
        },
        error: (error) => {
          console.error('Error al obtener el evento:', error);
        }
      });

      // Controlar que solo se cargue cuando el evento ha terminado y el /me del usuario es el creador o admin
      this.attendeeForm = this.formBuilder.group({
        attendees: ['', [Validators.required, Validators.min(0)]] // Poner aquí el máximo (registred users)
      });
      this.loadGraphData();

    } else {
      this.router.navigate(['/error']);
    }
  }

  loadGraphData() {
    this.eventService.getEventGraphData(this.eventId).subscribe({
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

  findCreator(){
    this.userService.getUser(this.event.creatorId).subscribe({
      next: (data) => {
        this.creator = data;
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
      this.eventService.updateEventAttendees(this.eventId, attendees).subscribe({
        next: () => this.loadGraphData(),
        error: () => this.router.navigate(['/error'])
      });
    }
  }

  onDeleteClick(): void {
    this.eventService.deleteEvent(this.eventId).subscribe({
      next: () => this.router.navigate(['']),
      error: () => this.router.navigate(['/error'])
    });
  }
}
