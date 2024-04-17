import { Event } from './event.model';

export interface PageEvent{

    events: Event[];
    page: number;
    totalPages: number;

}