import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/auth.model';
import { WishlistItem, WishlistRequest, WishlistUpdateRequest } from '../models/wishlist.model';

@Injectable({ providedIn: 'root' })
export class WishlistService {
  private baseUrl = `${environment.wishlistApiUrl}/wishlist`;

  constructor(private http: HttpClient) {}

  getWishlist(): Observable<ApiResponse<WishlistItem[]>> {
    return this.http.get<ApiResponse<WishlistItem[]>>(this.baseUrl);
  }

  addToWishlist(payload: WishlistRequest): Observable<ApiResponse<WishlistItem>> {
    return this.http.post<ApiResponse<WishlistItem>>(this.baseUrl, payload);
  }

  updateItem(id: number, payload: WishlistUpdateRequest): Observable<ApiResponse<WishlistItem>> {
    return this.http.put<ApiResponse<WishlistItem>>(`${this.baseUrl}/${id}`, payload);
  }

  removeItem(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/${id}`);
  }
}
