import { Component, Input } from '@angular/core';
import { Event } from '../../models/event.model';

@Component({
  selector: 'app-event-card',
  templateUrl: './event-card.component.html',
  styleUrls: ['../../home.css']
})
export class EventCardComponent {
  
  @Input()
  eventCard!: Event;
  
}