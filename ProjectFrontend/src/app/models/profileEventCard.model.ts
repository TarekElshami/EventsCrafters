import { Category } from "./category.model";
import { Event } from "./event.model";

interface EventCategory{
    event: Event;
    category: Category;
}

export interface ProfileEventCard{
    events: Event[],
    categories: Category[],
    areThereEvents: boolean,
    loadMore: boolean
}