import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { IndexComponent } from './templates/index/index.component';
import { ProfileComponent } from './templates/profile/profile.component';
import { LoginComponent } from './templates/login/login.component';
import { RegisterComponent } from './templates/register/register.component';
import { ViewEventsComponent } from './templates/view-events/view-events.component';
import { EventFormComponent } from './templates/event-form/event-form.component';
import { ErrorComponent } from './templates/error/error.component';
import {RecoverPasswordComponent} from "./templates/password-recovery/recoverPassword.component";
import { ReviewFormComponent } from './templates/review-form/review-form.component';

const routes: Routes = [
  {
    path: '',
    component: IndexComponent,
    data: { title: 'Inicio' }
  },
  {
    path: 'profile',
    component: ProfileComponent,
    data: { title: 'Perfil' }
  },
  {
    path: 'login',
    component: LoginComponent,
    data: { title: 'Login' }
  },
  {
    path: 'register',
    component: RegisterComponent,
    data: { title: 'Registro' }
  },
  {
    path: 'review/:id',
    component: ReviewFormComponent,
    data: { title: 'Valorar' }
  },
  {
    path: 'event/:id',
    component: ViewEventsComponent,
    data: { title: 'Ver Evento' }
  },
  {
    path: 'event/edit/:id',
    component: EventFormComponent,
    data: { title: 'Editar Evento' }
  },
  {
    path: 'event-create',
    component: EventFormComponent,
    data: { title: 'Crear Evento' }
  },
  {
    path: 'emailSent',
    component: RecoverPasswordComponent,
    data: { title: 'Recuperar Contraseña' }
  },
  {
    path: 'recoverPassword/:username/randomToken',
    component: RecoverPasswordComponent,
    data: { title: 'Cambiar Contraseña' }
  },
  {
    path: 'recoverPassword/:username/randomToken',
    component: RecoverPasswordComponent,
    data: { title: 'Recuperar Contraseña' }
  },
  {
    path: 'error/:type',
    component: ErrorComponent,
    data: { title: 'Error General' }
  },
  {
    path: '**',
    component: ErrorComponent,
    data: { title: 'Error General' }
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
