import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Product } from '../../core/models/product.model';
import { ProductService } from '../../core/services/product.service';
import { WishlistService } from '../../core/services/wishlist.service';

@Component({
  selector: 'app-catalog',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './catalog.component.html',
  styleUrl: './catalog.component.css',
})
export class CatalogComponent implements OnInit {
  products = signal<Product[]>([]);
  loading = signal(true);
  errorMessage = signal<string | null>(null);
  toast = signal<{ type: 'success' | 'error'; text: string } | null>(null);

  /** id de producto -> cantidad seleccionada para agregar */
  quantities: Record<number, number> = {};
  /** ids de producto que ya se están agregando (para deshabilitar el botón) */
  addingIds = new Set<number>();

  constructor(private productService: ProductService, private wishlistService: WishlistService) {}

  ngOnInit(): void {
    this.loadProducts();
  }

  loadProducts(): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    this.productService.listProducts().subscribe({
      next: (products) => {
        this.products.set(products);
        products.forEach((p) => (this.quantities[p.id] = this.quantities[p.id] ?? 1));
        this.loading.set(false);
      },
      error: () => {
        this.errorMessage.set('No fue posible cargar el catálogo de productos');
        this.loading.set(false);
      },
    });
  }

  addToWishlist(product: Product): void {
    const quantity = this.quantities[product.id] || 1;
    this.addingIds.add(product.id);

    this.wishlistService.addToWishlist({ productId: product.id, quantity }).subscribe({
      next: (res) => {
        this.addingIds.delete(product.id);
        if (res.data) {
          this.showToast('success', `"${product.name}" se agregó a tu lista de deseos`);
        } else {
          this.showToast('error', res.message || 'No fue posible agregar el producto');
        }
      },
      error: (err) => {
        this.addingIds.delete(product.id);
        this.showToast('error', err.error?.message || 'No fue posible agregar el producto');
      },
    });
  }

  private showToast(type: 'success' | 'error', text: string): void {
    this.toast.set({ type, text });
    setTimeout(() => this.toast.set(null), 3200);
  }
}
