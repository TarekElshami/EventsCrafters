import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { IndexComponent } from './templates/index/index.component';
import { ProfileComponent } from './templates/profile/profile.component';
import { LoginComponent } from './templates/login/login.component';
import { RegisterComponent } from './templates/register/register.component';
import { ViewEventsComponent } from './templates/view-events/view-events.component';
import { EventFormComponent } from './templates/event-form/event-form.component';
import { ErrorComponent } from './templates/error/error.component';

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
    component: RegisterComponent
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
  },
  {
    path: '**',
    component: ErrorComponent
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
