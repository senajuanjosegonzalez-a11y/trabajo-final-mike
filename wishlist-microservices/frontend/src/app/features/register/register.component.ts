import { CommonModule } from '@angular/common';
import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrl: '../login/login.component.css',
})
export class RegisterComponent {
  username = '';
  email = '';
  password = '';
  rolId = 1; // 1 = CLIENTE, 2 = ADMIN (ver data.sql de auth-service)

  loading = signal(false);
  errorMessage = signal<string | null>(null);
  successMessage = signal<string | null>(null);

  constructor(private authService: AuthService, private router: Router) {}

  submit(): void {
    if (!this.username || !this.email || !this.password) {
      this.errorMessage.set('Completa todos los campos');
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);
    this.successMessage.set(null);

    this.authService
      .register({ username: this.username, email: this.email, password: this.password, rolId: this.rolId })
      .subscribe({
        next: (res) => {
          this.loading.set(false);
          if (res.data) {
            this.successMessage.set('Cuenta creada correctamente, ya puedes iniciar sesión');
            setTimeout(() => this.router.navigate(['/login']), 1200);
          } else {
            this.errorMessage.set(res.message || 'No fue posible crear la cuenta');
          }
        },
        error: (err) => {
          this.loading.set(false);
          this.errorMessage.set(err.error?.message || 'No fue posible crear la cuenta');
        },
      });
  }
}
