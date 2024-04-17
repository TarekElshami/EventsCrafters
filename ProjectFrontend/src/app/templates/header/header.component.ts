import { Component, Output, EventEmitter } from '@angular/core';
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
    searchBarInput: string = '';
    

    constructor(private categoryService: CategoryService, private router: Router) {}

    ngOnInit() {
        this.categoryService.getAllCategories().subscribe({
        next: (data) => {
            this.categories = data;
        },
        error: () => {
            this.router.navigate(['/error']); 
        }
        });
    }

    @Output() searchBarInfo = new EventEmitter<string>();

    search(){
        console.log(this.searchBarInput)
        this.searchBarInfo.emit(this.searchBarInput);
        this.searchBarInput= '';
    }
}