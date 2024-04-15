import { Component } from '@angular/core';
import { CategoryService } from '../../services/category.service'

interface Category {
  id: number;
  name: string;
  color: string;
}

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['../../home.css']
})
export class HeaderComponent {
  
  categories: Category[] = []

  constructor(private categoryService: CategoryService) {}

  ngOnInit() {
    this.categoryService.getCategories(-1).subscribe(
     data => {
        this.categories = data; 
      },
      error => {
        console.error('Error al cargar las categorías:', error);
      }
    );
  }
  
}