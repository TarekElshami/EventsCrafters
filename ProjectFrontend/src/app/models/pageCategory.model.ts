import { Category } from './category.model';

export interface PageCategory{

    categories: Category[];
    page: number;
    totalPages: number;

}