import { Component } from '@angular/core';
import { User } from "../../models/user.model";
import { Event } from '../../models/event.model';
import { ActivatedRoute, Router } from "@angular/router";
import { EventService } from "../../services/event.service";
import { UserService } from "../../services/user.service";
import html2canvas from 'html2canvas';
import { jsPDF } from 'jspdf';


@Component({
  selector: 'app-ticket',
  templateUrl: './ticket.component.html',
  styleUrl: './ticket.component.css'
})
export class TicketComponent {

  isLoading: boolean = false;
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
      next: () => {
        this.getUserData();
      },
      error: () => {
        this.router.navigate(['/login']);
      }
    });
  }

  getUserData(){
    this.userService.getCurrentUser().subscribe({
      next: (user) => {
        this.user = user;
        this.isLoading = false;
      },
      error: () => {
        this.router.navigate(['/error']);
      }
    });
  }

  getCreatorData(){
    this.userService.getUser(this.event.creatorId).subscribe({
      next: (creator) => {
        this.creator = creator;
      },
      error: () => {
        this.router.navigate(['/error']);
      }
    });
  }

  downloadTicket(){
    this.isLoading = true;
    const data = document.getElementById('ticket-content');
    if (data) {
      html2canvas(data).then(canvas => {
        const imgWidth = 208;
        const imgHeight = canvas.height * imgWidth / canvas.width;

        const contentDataURL = canvas.toDataURL('image/png');
        let pdf = new jsPDF('p', 'mm', 'a4');
        const position = 0;
        pdf.addImage(contentDataURL, 'PNG', 0, position, imgWidth, imgHeight);
        pdf.save(this.event.name+' ticket.pdf');
        //this.router.navigate(['/event', this.event.id]);
        this.isLoading = false;
      });
    }
  }

}
