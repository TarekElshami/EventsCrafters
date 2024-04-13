import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { IndexComponent } from './templates/index/index.component';
import { ProfileComponent } from './templates/profile/profile.component';
import { LoginComponent } from './templates/login/login.component';
import { SignInComponent } from './templates/sign-in/sign-in.component';
import { ViewEventsComponent } from './templates/view-events/view-events.component';
import { EventFormComponent } from './templates/event-form/event-form.component';

const routes: Routes = [
  { 
    path: '', 
    component: IndexComponent 
  },
  { 
    path: 'profile',
    component: ProfileComponent
  },
  { 
    path: 'login', 
    component: LoginComponent
  },
  { 
    path: 'register', 
    component: SignInComponent
  },
  { 
    path: 'event/:id', 
    component: ViewEventsComponent
  },
  { 
    path: 'event/edit/:id', 
    component: EventFormComponent
  },
  { 
    path: 'event-create', 
    component: EventFormComponent
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
