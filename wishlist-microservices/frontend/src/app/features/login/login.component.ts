import { CommonModule } from '@angular/common';
import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
})
export class LoginComponent {
  user = '';
  password = '';
  loading = signal(false);
  errorMessage = signal<string | null>(null);

  constructor(private authService: AuthService, private router: Router) {}

  submit(): void {
    if (!this.user || !this.password) {
      this.errorMessage.set('Ingresa tu usuario/correo y tu contraseña');
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);

    this.authService.login({ user: this.user, password: this.password }).subscribe({
      next: (res) => {
        this.loading.set(false);
        if (res.data?.jwt) {
          this.router.navigate(['/catalog']);
        } else {
          this.errorMessage.set(res.message || 'No fue posible iniciar sesión');
        }
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(err.error?.message || 'Usuario o contraseña incorrectos');
      },
    });
  }
}
