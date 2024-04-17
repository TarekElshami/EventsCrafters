import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { PageEvent } from '../models/pageEvent.model';

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

  getRecommendedEvents(page: number): Observable<any> {
    let params = new HttpParams().append('page', page.toString());
    params = params.append('type', 'recommended');
    return this.http.get<PageEvent>(`/api/events/filter`, {params})
  }
  
}
