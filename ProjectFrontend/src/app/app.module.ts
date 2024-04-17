import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';
import { ReactiveFormsModule } from '@angular/forms';

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
    EventCardComponent

  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    NgbModule,
    ReactiveFormsModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
