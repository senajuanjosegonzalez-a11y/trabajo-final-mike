export interface LoginRequest {
  user: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  rolId: number;
}

export interface LoginResponseData {
  jwt: string;
}

export interface ApiResponse<T> {
  message: string;
  data: T | null;
}

export interface JwtPayload {
  sub: string;
  userId: number;
  rolId: number;
  iat: number;
  exp: number;
}
