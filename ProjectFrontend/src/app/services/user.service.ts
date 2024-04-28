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

  getUser(userId : number):Observable<User>{
    return this.http.get<User>('/api/users/'+userId);
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

  update(user: {id: number, name: string; email: string; username: string}) {
    return this.http.post("/api/users/updateProfile", user);
  }

  delete(id: number) {
    return this.http.delete("/api/users/"+id);
  }

  logout(){
    this.http.post("/api/auth/logout",{}).subscribe({
      next: value => {console.log(value)},
      error: err => {console.log(err)}
    });
  }

  ban(banUser: string) {
    return this.http.post("/api/users/ban", banUser);
  }

  unban(unbanUser: string) {
    return this.http.post("/api/users/unban", unbanUser);
  }

  changePFP(newPPF: File) {
    let formData = new FormData();
    formData.append("profilePicture", newPPF);

    return this.http.post("/api/users/setProfilePicture", formData);
  }
}
