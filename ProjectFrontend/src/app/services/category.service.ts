import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Category } from '../models/category.model';
import { PageCategory } from '../models/pageCategory.model';

@Injectable({
  providedIn: 'root'
})
export class CategoryService {
  private apiUrl = '/api/categories';

  constructor(private http: HttpClient) {}

  getCategories(page: number): Observable<any> {
    let params = new HttpParams().set('page', page.toString());
    return this.http.get<PageCategory>(this.apiUrl, { params });
  }

  getAllCategories(): Observable<any>{
    return this.http.get<Category>('/api/allCategories');
  }

  getCategoryById(id: number): Observable<Category>{
    return this.http.get<Category>(`/api/categories/${id}`);
  }

  createCategory(categoryData: FormData): Observable<any> {
    return this.http.post('/api/categories', categoryData);
  }

}

