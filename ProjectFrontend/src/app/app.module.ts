import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { IndexComponent } from './templates/index/index.component';
import { ProfileComponent } from './templates/profile/profile.component';
import { LoginComponent } from './templates/login/login.component';
import { RegisterComponent } from './templates/register/register.component';
import { ViewEventsComponent } from './templates/view-events/view-events.component';
import { EventFormComponent } from './templates/event-form/event-form.component';
import { ErrorComponent } from './templates/error/error.component';
import { HeaderComponent } from './templates/header/header.component';
import { EventCardComponent } from './templates/event-card/event-card.component';
import { NgxChartsModule } from '@swimlane/ngx-charts';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import {RecoverPasswordComponent} from "./templates/password-recovery/recoverPassword.component";
import { ProfileEventCardComponent } from './templates/profile-event-card/profile-event-card.component';
import { ReviewFormComponent } from './templates/review-form/review-form.component';
import { TicketComponent } from './templates/ticket/ticket.component';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';

@NgModule({
  declarations: [
    AppComponent,
    IndexComponent,
    ProfileComponent,
    LoginComponent,
    RegisterComponent,
    ViewEventsComponent,
    EventFormComponent,
    ErrorComponent,
    HeaderComponent,
    EventCardComponent,
    RecoverPasswordComponent,
    ProfileEventCardComponent,
    ReviewFormComponent,
    TicketComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    NgbModule,
    ReactiveFormsModule,
    FormsModule,
    FontAwesomeModule,
    BrowserAnimationsModule,
    NgxChartsModule,
    ReactiveFormsModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
