import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { LeadService, LeadPayload } from './lead.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, FormsModule],
  templateUrl: './app.html',
  styleUrls: ['./app.css']
})
export class App {
  protected readonly title = signal('frontend');

  model: LeadPayload = { email: '', name: '', courses: [] };
  sending = false;
  result: string | null = null;

  constructor(private leads: LeadService) {}

  toggleCourse(c: string, ev: Event) {
    const checked = (ev.target as HTMLInputElement).checked;
    this.model.courses = checked
      ? Array.from(new Set([...this.model.courses, c]))
      : this.model.courses.filter(x => x !== c);
  }

  submit() {
    this.result = null;
    this.sending = true;
    this.leads.submitLead(this.model).subscribe({
      next: () => { this.result = '¡Enviado!'; this.sending = false; },
      error: (err) => { this.result = 'Error al enviar'; this.sending = false; console.error(err); }
    });
  }
}
