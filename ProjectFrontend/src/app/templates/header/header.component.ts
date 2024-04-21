import { Component, Output, EventEmitter, Input } from '@angular/core';
import { CategoryService } from '../../services/category.service'
import { Router } from '@angular/router';
import { Category } from '../../models/category.model';


@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['../../home.css', './header.component.css']
})
export class HeaderComponent {
  
    categories: Category[] = []
    isCollapsed = true;
    searchBarInput: string = '';
    logged: boolean = true;
    
    @Input() isIndex!: boolean;

    @Output() searchBarInfo = new EventEmitter<string>();
    @Output() categoryFilter = new EventEmitter<number>();

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

    search(){
        this.searchBarInfo.emit(this.searchBarInput);
        this.searchBarInput= '';
    }

    filterByCategoryId(id:number){
        this.categoryFilter.emit(id);
    }

    goToProfile(){
        this.router.navigate(['/profile']); 
    }

}