import { Component } from '@angular/core';
import {HttpClient, HttpResponse} from "@angular/common/http";
import {
  AbstractControl,
  AsyncValidatorFn,
  FormBuilder,
  FormGroup,
  ValidationErrors,
  ValidatorFn,
  Validators
} from "@angular/forms";
import {UserService} from "../../services/user.service";
import {Router} from "@angular/router";
import {map} from "rxjs";

@Component({
  selector: 'app-sign-in',
  templateUrl: './register.component.html',
  styleUrls: ['../../forms.css', './register.component.css']
})
export class RegisterComponent {

  registerForm: FormGroup;
  isLoading = false;
  submitButtonEnabled: boolean = false;
  imgUrl = "./assets/logo.png";

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private http:HttpClient,
    private router: Router,
  ) {
    this.registerForm = this.fb.group({
      fullName: ['', Validators.required],
      username: ['', { validators: [Validators.required], asyncValidators: [this.userNameTaken()], updateOn: 'blur' }],
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required],
      confirmPassword: ['', Validators.required]
    }, {
      validators : [this.matchPasswords()],
      updateOn: 'blur'
    });
  }

  onSubmit() {
    //send a login request to /api/auth/login
    const fullName: string = this.registerForm.get("fullName")?.value;
    const username: string = this.registerForm.get("username")?.value;
    const email: string = this.registerForm.get("email")?.value;
    const password: string = this.registerForm.get("password")?.value;
    this.http.post("/api/users",
      {
        name:fullName,
        username: username,
        email: email,
        password: password
      }).subscribe({
        next: (response) => {
          this.router.navigate(["/login"])
        },
        error: (error) => {
          this.router.navigate(["/error"])
        }
      }
    );
  }

  userNameTaken(): AsyncValidatorFn {
    return (control: AbstractControl) => {
      const userName = control.value;
      return this.userService.usernameTaken(userName).pipe(
        map(
          (isTaken: HttpResponse<any>) => {
            if (isTaken) {
              //alert("UserName is taken. Try a different one");
              this.submitButtonOnClick = this.warnNameTaken;
              return {userNameTaken:true};
            } else {
              this.submitButtonOnClick = this.validateForm;
              return null
            }
          }
        )
      )
    }
  }

  warnNameTaken() {
    alert("Can't create an account with that nickname. Try a different one.");
    return false;
  }
  validateForm() {
    return true;
  }

  matchPasswords(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const password = control.get("password")?.value;
      const confirmPassword = control.get("confirmPassword")?.value;
      if (password !== confirmPassword) {
        //alert("Passwords do not match");
        return {passwordsMatch:true};
      }
      return null;
    }
  }

  submitButtonOnClick: () => boolean = this.validateForm;

}
