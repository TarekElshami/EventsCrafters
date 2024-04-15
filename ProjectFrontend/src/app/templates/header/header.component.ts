import { Component } from '@angular/core';
import { CategoryService } from '../../services/category.service'
import { Router } from '@angular/router';
import { Category } from '../../models/category.model';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['../../home.css']
})
export class HeaderComponent {
  
  categories: Category[] = []
  isCollapsed = true;
  constructor(private categoryService: CategoryService, private router: Router) {}

  ngOnInit() {
    this.categoryService.getCategories(-1).subscribe({
      next: (data) => {
        this.categories = data;
      },
      error: () => {
        this.router.navigate(['/error']); 
      }
    });
  }
  
}