export interface WishlistItem {
  id: number;
  productId: number;
  productName: string;
  price: number;
  quantity: number;
  stockDisponible: number;
  disponible: boolean;
  mensaje: string | null;
}

export interface WishlistRequest {
  productId: number;
  quantity: number;
}

export interface WishlistUpdateRequest {
  quantity: number;
}
