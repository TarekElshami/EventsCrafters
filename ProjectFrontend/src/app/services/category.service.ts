import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class CategoryService {
  private apiUrl = '/api/categories';

  constructor(private http: HttpClient) {}

  getCategories(page: number = 1): Observable<any> {
    let params = new HttpParams().set('page', page.toString());
    return this.http.get<{id: number; name: string; color: string}[]>(this.apiUrl, { params });
  }
}

