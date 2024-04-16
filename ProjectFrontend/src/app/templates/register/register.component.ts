import { Component } from '@angular/core';
import {HttpClient} from "@angular/common/http";

const BASE_URL = "https://localhost:8443/api/users";

@Component({
  selector: 'app-sign-in',
  templateUrl: './register.component.html',
  styleUrls: ['../../forms.css', './register.component.css']
})
export class RegisterComponent {

  constructor(private http: HttpClient){};

  imgUrl = "/assets/logo.png";


}
