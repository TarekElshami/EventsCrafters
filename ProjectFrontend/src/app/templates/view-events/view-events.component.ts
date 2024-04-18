import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { EventService } from '../../services/event.service';
import { EventGraphData } from '../../models/event-graph-data.model';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

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

  constructor(
    private eventService: EventService,
    private route: ActivatedRoute,
    private formBuilder: FormBuilder,
    private router: Router
  ) {}

  ngOnInit() {
    const eventIdString = this.route.snapshot.paramMap.get('id');
    if (eventIdString) {
      this.eventId = +eventIdString;  // String to num
      // @Marcos debes controlar que solo se cargue cuando el evento ha terminado y
      // el /me del usuario es el creador o admin
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
