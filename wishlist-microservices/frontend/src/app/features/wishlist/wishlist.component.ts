import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { WishlistItem } from '../../core/models/wishlist.model';
import { WishlistService } from '../../core/services/wishlist.service';

@Component({
  selector: 'app-wishlist',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './wishlist.component.html',
  styleUrl: './wishlist.component.css',
})
export class WishlistComponent implements OnInit {
  items = signal<WishlistItem[]>([]);
  loading = signal(true);
  errorMessage = signal<string | null>(null);
  toast = signal<{ type: 'success' | 'error'; text: string } | null>(null);
  busyIds = new Set<number>();

  constructor(private wishlistService: WishlistService) {}

  ngOnInit(): void {
    this.loadWishlist();
  }

  loadWishlist(): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    this.wishlistService.getWishlist().subscribe({
      next: (res) => {
        this.items.set(res.data ?? []);
        this.loading.set(false);
      },
      error: () => {
        this.errorMessage.set('No fue posible cargar tu lista de deseos');
        this.loading.set(false);
      },
    });
  }

  changeQuantity(item: WishlistItem, delta: number): void {
    const newQty = item.quantity + delta;
    if (newQty < 1) return;

    this.busyIds.add(item.id);
    this.wishlistService.updateItem(item.id, { quantity: newQty }).subscribe({
      next: (res) => {
        this.busyIds.delete(item.id);
        if (res.data) {
          this.items.update((list) => list.map((i) => (i.id === item.id ? res.data! : i)));
        } else {
          this.showToast('error', res.message || 'No fue posible actualizar la cantidad');
        }
      },
      error: (err) => {
        this.busyIds.delete(item.id);
        this.showToast('error', err.error?.message || 'No fue posible actualizar la cantidad');
      },
    });
  }

  remove(item: WishlistItem): void {
    this.busyIds.add(item.id);
    this.wishlistService.removeItem(item.id).subscribe({
      next: () => {
        this.busyIds.delete(item.id);
        this.items.update((list) => list.filter((i) => i.id !== item.id));
        this.showToast('success', `"${item.productName}" se eliminó de tu lista de deseos`);
      },
      error: (err) => {
        this.busyIds.delete(item.id);
        this.showToast('error', err.error?.message || 'No fue posible eliminar el producto');
      },
    });
  }

  totalEstimado(): number {
    return this.items().reduce((sum, i) => sum + (i.price || 0) * i.quantity, 0);
  }

  private showToast(type: 'success' | 'error', text: string): void {
    this.toast.set({ type, text });
    setTimeout(() => this.toast.set(null), 3200);
  }
}
