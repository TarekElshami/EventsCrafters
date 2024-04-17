export interface Event {
    id: number;
    name: string;
    description: string;
    maxCapacity: number;
    price: number;
    location: string;
    map: string;
    startDate: Date;
    endDate: Date;
    duration: string;
    additionalInfo: string;
    creatorId: number;
    numRegisteredUsers: number;
    categoryId: number;
    imageUrl: string;

}