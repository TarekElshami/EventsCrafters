import { Component } from '@angular/core';
import { User } from "../../models/user.model";
import { Event } from '../../models/event.model';
import { ActivatedRoute, Router } from "@angular/router";
import { EventService } from "../../services/event.service";
import { UserService } from "../../services/user.service";
import {toRelativeImport} from "@angular/compiler-cli";

@Component({
  selector: 'app-ticket',
  templateUrl: './ticket.component.html',
  styleUrl: './ticket.component.css'
})
export class TicketComponent {

  isLoading: boolean = false;
  showDownloadButton: boolean = true;

  isUserLogged: boolean = false;
  event!: Event;
  user!: User;
  creator!: User;

  constructor(
      private eventService: EventService,
      private userService: UserService,
      private route: ActivatedRoute,
      private router: Router)
  {}

  ngOnInit(){
    this.isLoading = true;
    const eventIdString = this.route.snapshot.paramMap.get('id');
    if (eventIdString) {
      let requestedEventId = +eventIdString;
      if (requestedEventId > 0) {
        this.eventService.getEventById(eventIdString).subscribe({
          next: (event) => {
            this.event = event;
            this.checkLogin();
            this.getCreatorData();
          },
          error: (error) => {
            console.error('Error al obtener el evento:', error);
            this.router.navigate(['/error']);
          }
        });
      }
    }
  }

  checkLogin(){
    this.userService.getCurrentUser().subscribe({
      next: (currentUser) => {
        this.isUserLogged = true;
        this.getUserData();
      },
      error: (error) => {
        this.isUserLogged = false;
      }
    });
  }

  getUserData(){
    this.userService.getCurrentUser().subscribe({
      next: (user) => {
        this.user = user;
        this.isLoading = false;
      },
      error: (error) => {
        this.router.navigate(['/error']);
      }
    });
  }

  getCreatorData(){
    this.userService.getUser(this.event.creatorId).subscribe({
      next: (creator) => {
        this.creator = creator;
      },
      error: (error) => {
        this.router.navigate(['/error']);
      }
    });
  }

  downloadTicket(){
    this.showDownloadButton = false;
    setTimeout(() => {
      window.print();
    }, 100);
    setTimeout(() => {
      this.showDownloadButton = true;
    }, 100); // Cambia 1000 por el tiempo necesario para que la impresión se complete en tu caso
  }
}
