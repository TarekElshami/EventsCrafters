import { Component } from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {HttpClient} from "@angular/common/http";
import {AbstractControl, FormBuilder, FormGroup, ValidationErrors, ValidatorFn, Validators} from "@angular/forms";
import {UserService} from "../../services/user.service";

@Component({
  selector: 'app-error',
  templateUrl: './recoverPassword.component.html',
  styleUrl: './recoverPassword.component.css'
})
export class RecoverPasswordComponent {

  emailSent: boolean = true;
  changePasswordForm: FormGroup;
  user: string = "";
  token:string = "";
  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private httpClient:HttpClient,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.changePasswordForm = this.fb.group({
      password: ['', Validators.required],
      confirmPassword: ['', Validators.required]
    }, {
      validators : [this.matchPasswords()],
      updateOn: 'blur'
    });
  }

  ngOnInit() {
    this.route.url.subscribe(segments => {
      if (segments.length===1 && segments[0].path === 'emailSent') {
        this.emailSent = true;
      } else if (segments.length === 3 && segments[0].path==='recoverPassword') {
        this.user = segments[1].path;
        this.emailSent = false;
        this.route.queryParams.subscribe(params =>
        this.token = params['token'])
      }
    });

  }

  onSubmit() {
    const pass = this.changePasswordForm.get("password")?.value
    const confPass = this.changePasswordForm.get("confirmPassword")?.value
    if (!pass===confPass) return;
    this.httpClient.post("/api/users/changePassword", {username:this.user, password:pass, token:this.token}).subscribe({
      next: (request) => this.router.navigate(["/login"]),
      error: (error) => this.router.navigate(["/error/changePassword"])
    })
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
}
