import { Routes } from '@angular/router';
import { Home } from './pages/home/home';
import { Auth } from './pages/auth/auth';
import { SignUp } from './pages/auth/sign-up/sign-up';
import { Login } from './pages/auth/login/login';

export const routes: Routes = [
  {
    path: 'home',
    component: Home
  },
  {
    path: '',
    component: Auth,
    children: [
      {
        path: 'signup',
        component: SignUp
      },
      {
        path: 'login',
        component: Login
      },
      {
        path: '',
        redirectTo: 'signup',
        pathMatch: 'full'
      }
    ]
  },
  {
    path: '**',
    redirectTo: 'home'
  }
];
