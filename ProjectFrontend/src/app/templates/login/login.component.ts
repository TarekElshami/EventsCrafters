import { Component, OnInit  } from '@angular/core';
import { UserService } from '../../services/user.service';
import {AbstractControl, AsyncValidatorFn, FormBuilder, FormGroup, ValidationErrors, Validators} from "@angular/forms";
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
      username: ['', {validators: [Validators.required], asyncValidators: [this.checkUserBanned()], updateOn: 'blur'}],
      password: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    //this.autoLogin();
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

  checkUserBanned(): AsyncValidatorFn {
    return (control: AbstractControl) => {
      const username: string = this.loginForm.get("username")?.value;
      if (username===""){
        this.submitButtonEnabled = true;
      }
      return this.userService.isUserBanned(username).pipe(
        map( (isBanned: HttpResponse<any>): ValidationErrors | null => {
          if (isBanned) {
            //alert("Este usuario está baneado")
            this.submitButtonEnabled = false;
            return {isBanned:true}
          } else {
            this.submitButtonEnabled = true;
            return null;
          }
          }
        )
      )
    }
  }


   recoverPassword() {
    // Prompt the user for input
    const userInput: string | null = window.prompt('Please enter your username');
    if (userInput === null) {
      return;
    }

    // Check if the user exists
    this.userExists(userInput).subscribe(exists => {
      if (exists) {
        this.router.navigate(["/recoverPassword/" + userInput]);
      } else {
        // Invalid input
        alert('Invalid input. Please try again.');
      }
    });
  }

  userExists(input: string): Observable<boolean> {
    return this.httpClient.get<any>("/api/users/IsUsernameTaken?username="+input).pipe(
      map((response: HttpResponse<any>) => !response.ok)
    );
  }


  onClickSubmit() {
    if (!this.submitButtonEnabled){
      alert("Este usuario está baneado")
    }
    return this.submitButtonEnabled;
  }

}
