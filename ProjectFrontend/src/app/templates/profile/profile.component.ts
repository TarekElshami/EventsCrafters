import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { UserService } from '../../services/user.service';
import { User } from '../../models/user.model';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css'
})
export class ProfileComponent {

  isAdmin: boolean = false;
  currentUser!: User;

  constructor(private userService: UserService, private router: Router){}

  ngOnInit(){
    this.userService.login('user', 'pass').subscribe({
      next: (data) => {
        console.log(data);
    
      },
      error: () => {
        this.router.navigate(['/error']); 
      }
    });

    this.userService.getCurrentUser().subscribe({
      next: (data) => {
        this.currentUser = data;
    
      },
      error: () => {
        this.router.navigate(['/error']); 
      }
    });
  }

}
