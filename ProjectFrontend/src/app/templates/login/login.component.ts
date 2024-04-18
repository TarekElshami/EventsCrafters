import { Component, OnInit  } from '@angular/core';
import { UserService } from '../../services/user.service';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {HttpClient, HttpResponse} from "@angular/common/http";
import {map, Observable, Observer} from "rxjs";
import {Router} from "@angular/router";


@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['../../forms.css', './login.component.css']
})

export class LoginComponent implements OnInit {  // Implement OnInit interface

  loginForm: FormGroup;
  isLoading = false;
  submitButtonEnabled: boolean = false;

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private httpClient:HttpClient,
    private router: Router
  ) {
    this.loginForm = this.fb.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    //this.autoLogin();
  }

  autoLogin(): void {
    //const username = 'user2';
    //const password = 'pass';
    //this.userService.login(username, password).subscribe({
    //  next: (response) => {
    //    console.log('Auto login successful', response);
    //    // Optionally navigate or perform further actions on successful login
    //  },
    //  error: (error) => {
    //    console.error('Auto login failed', error);
    //    // Handle login failure (e.g., show error message)
    //  }
    //});
  }

  onSubmit() {
    //send a login request to /api/auth/login
    var username : string = this.loginForm.get("username")?.value
    var password : string = this.loginForm.get("password")?.value
    this.httpClient.post("/api/auth/login", {username: username, password: password}).subscribe({
        next: (response) => {
          this.router.navigate(["/"])
        },
      error: (error) => {
            this.router.navigate(["/error"])
      }
      }
    );
  }

  checkUserBanned() {
    var username : string = this.loginForm.get("username")?.value
    if (username===""){
      this.submitButtonEnabled = true;
      return;
    }

    this.httpClient.get<boolean>("api/users/IsUserBanned?username="+username).subscribe(
      (isBanned: boolean) => {
          if (isBanned) {
            alert("Este usuario está baneado")
            this.submitButtonEnabled = false;
          } else {
            this.submitButtonEnabled = true;
          }
      }
    )
  }


   recoverPassword(event: Event) {
    event.preventDefault(); // Prevent the default link behavior

    // Prompt the user for input
    const userInput: string | null = window.prompt('Please enter your username');
    if (userInput === null) {
      return;
    }

    // Check if the user exists
    this.userExists(userInput).subscribe(exists => {
      if (exists) {
        window.location.href = "/recoverPassword/" + userInput;
      } else {
        // Invalid input
        alert('Invalid input. Please try again.');
      }
    });
  }

  userExists(input: string): Observable<boolean> {
    return this.httpClient.post<any>("/IsUsernameTaken", input).pipe(
      map((response: HttpResponse<any>) => !response.ok)
    );
  }


  onClickSubmit() {
    return this.submitButtonEnabled;
  }
}
