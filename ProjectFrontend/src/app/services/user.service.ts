import { Injectable } from '@angular/core';
import {HttpClient, HttpResponse} from '@angular/common/http';
import {catchError, map, Observable} from 'rxjs';
import { User } from '../models/user.model';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private apiUrl = '/api/auth/login';

  constructor(private http: HttpClient) {}

  login(username: string, password: string): Observable<any> {
    const body = { username, password };
    return this.http.post(this.apiUrl, body);
  }

  getCurrentUser():Observable<User>{
    return this.http.get<User>('/api/users/me');
  }

  usernameTaken(userName: string) {
    return this.http.get<any>("/api/users/IsUsernameTaken?username="+userName).pipe(
      map(
        (response: HttpResponse<any>) => {
          return response;
        }
      )
    )
  }

  isUserBanned(userName: string) {
    return this.http.get<any>("/api/users/IsUserBanned?username="+userName).pipe(
      map(
        (response: HttpResponse<any>) => {
          return response;
        }
      )
    )
  }


}
