import { Component, OnInit } from '@angular/core';
import { EventService } from './services/event.service'; 

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {

  constructor(private eventService: EventService) {}

  ngOnInit() {
    this.getEvent();
  }

  getEvent() {
    this.eventService.getEvent().subscribe(
      data => {
        console.log(data);
      },
      error => {
        console.error('Hubo un error al obtener el evento:', error);
      }
    );
  }
}
