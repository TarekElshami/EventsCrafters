import { Component, OnInit  } from '@angular/core';
import { UserService } from '../../services/user.service';


@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['../../forms.css', './login.component.css']
})

export class LoginComponent implements OnInit {  // Implement OnInit interface
  constructor(private userService: UserService) {}

  ngOnInit(): void {
    this.autoLogin();
  }

  autoLogin(): void {
    const username = 'user';
    const password = 'pass';
    this.userService.login(username, password).subscribe({
      next: (response) => {
        console.log('Auto login successful', response);
        // Optionally navigate or perform further actions on successful login
      },
      error: (error) => {
        console.error('Auto login failed', error);
        // Handle login failure (e.g., show error message)
      }
    });
  }
}
