import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, forkJoin } from 'rxjs';
import { PageEvent } from '../models/pageEvent.model';
import { EventGraphData } from '../models/event-graph-data.model';
import { ProfileGraphData } from '../models/profile-graph-data.model';
import {EventStats} from "../models/eventLiveStats.model";

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

  getEventLiveStats(eventId: number): Observable<EventStats> {
    return this.http.get<EventStats>(`/api/events/stats/${eventId}`);
  }

  updateEventAttendees(eventId: number, attendees: number): Observable<any> {
    return this.http.put(`/api/events/${eventId}/attendees`, { attendeesCount: attendees });
  }

  deleteEvent(eventId: number): Observable<any> {
    return this.http.delete(`/api/events/${eventId}`);
  }

  getProfileEvents(role: string): Observable<any>{

    switch(role){
      case 'user':
        // user current created events
        let request1 = this.userEventRequest(0, 'created', 'present');
        // user past created events
        let request2 = this.userEventRequest(0, 'created', 'past');
        // use current registered events
        let request3 = this.userEventRequest(0, 'registered', 'present');
        //user past registered events
        let request4 = this.userEventRequest(0, 'registered', 'past');

        return forkJoin([request1,request2,request3,request4]);
      case 'admin':
        let params = new HttpParams().append('page',0);
        return this.http.get<PageEvent>(`/api/events/user`, { params });

        default:
      throw new Error(`Unsupported role: ${role}`);
    }
  }

  userEventRequest(page: number, type: string, time: string): Observable<any>{
        let params = new HttpParams()
                  .append('page',page)
                  .append('time',time)
                  .append('type',type);
        return this.http.get<PageEvent>(`/api/events/user`, { params });
  }

  getProfileGraphData():Observable<any>{
    return this.http.get<ProfileGraphData>(`/api/events/profile/graph`);
  }


}
