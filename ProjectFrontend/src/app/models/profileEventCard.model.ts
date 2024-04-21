
import { Event } from "./event.model";


export interface ProfileEventCard{
    events: Event[],
    areThereEvents: boolean,
    loadMore: boolean
}