import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { PageEvent } from '../models/pageEvent.model';
import { EventGraphData } from '../models/event-graph-data.model';

@Injectable({
  providedIn: 'root'
})
export class EventService {

  constructor(private http: HttpClient) {}

  getEventById(id: string): Observable<any> {
    return this.http.get(`/api/events/${id}`);
  }

  createEvent(eventData: FormData): Observable<any> {
    return this.http.post('/api/events', eventData);
  }

  updateEvent(id: string, eventData: FormData): Observable<any> {
    return this.http.put(`/api/events/${id}`, eventData);
  }

  uploadEventImage(eventId: string, image: File): Observable<any> {
    const formData = new FormData();
    formData.append('photo', image);
    return this.http.post(`/api/events/${eventId}/photo`, formData);
  }

  getEventImageUrl(id: string): string {
    return `/api/events/image/${id}`;
  }

  getFilteredEvents(page: number, type: string, input: string, id: number): Observable<any> {
    let params = new HttpParams().append('page', page.toString());
    params = params.append('type', type);

    switch (type) {
      case 'recommended':
        break;
      case 'searchBar':
        params = params.append('input', input);
        break;
      case 'category':
        params = params.append('id', id.toString());
        break;
      default:
        throw new Error('Invalid type for event fetching');
    }

    return this.http.get<PageEvent>(`/api/events/filter`, { params });
  }
    
  getEventGraphData(eventId: number): Observable<EventGraphData> {
    return this.http.get<EventGraphData>(`/api/events/graph/${eventId}`);
  }

  updateEventAttendees(eventId: number, attendees: number): Observable<any> {
    return this.http.put(`/api/events/${eventId}/attendees`, { attendeesCount: attendees });
  }
  
}
