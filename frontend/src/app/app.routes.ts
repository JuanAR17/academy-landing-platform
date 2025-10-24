import { Routes } from '@angular/router';
import { Home } from './pages/home/home';
import { Auth } from './pages/auth/auth';
import { SignUp } from './pages/sign-up/sign-up';
import { Contact } from './pages/contact/contact';
import { About } from './pages/about/about';
import { Courses } from './pages/courses/courses';
import { Payment } from './pages/payment/payment';
import { DebitCreditCard } from './pages/payment/debit-credit-card/debit-credit-card';
import { Pse } from './pages/payment/pse/pse';
import { Daviplata } from './pages/payment/daviplata/daviplata';

export const routes: Routes = [
  {
    path: 'home',
    component: Home
  },
  {
    path: 'login',
    component: Auth,
  },
  {
    path: 'signup',
    component: SignUp,
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
    path: 'payment',
    component: Payment,
    children:[
      {
        path: 'pse',
        component: Pse 
      },
      {
        path: 'debit-credit-card',
        component: DebitCreditCard
      },
      {
        path: 'daviplata',
        component: Daviplata
      }
    ],
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
