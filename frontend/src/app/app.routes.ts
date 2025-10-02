import { Routes } from '@angular/router';
import { Home } from './pages/home/home';
import { Auth } from './pages/auth/auth';
import { SignUp } from './pages/auth/sign-up/sign-up';
import { Login } from './pages/auth/login/login';
import { Contact } from './pages/contact/contact';
import { About } from './pages/about/about';
import { Courses } from './pages/courses/courses';

export const routes: Routes = [
  {
    path: 'home',
    component: Home
  },
  {
    path: 'access',
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
    ]
  },
  {
    path: 'courses',
    component: Courses,
  },
  {
    path: 'about',
    component: About,
  },
  {
    path: 'contact',
    component: Contact,
  },
  {
    path: '',
    redirectTo: 'home',
    pathMatch: 'full'
  },
  {
    path: '**',
    redirectTo: 'home'
  }
];
