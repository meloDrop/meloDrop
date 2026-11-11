import { apiFetch } from './client';
import type { LoginBody, Me, Token } from './types/auth';

// POST /auth/login: 200 with a JWT, 401 wrong credentials, 403 email not confirmed
export function login(identifier: string, password: string) {
    const body: LoginBody = { identifier, password };
    return apiFetch<Token>('/auth/login', {
        method: 'POST',
        body: JSON.stringify(body),
    });
}

// GET /me: the logged-in user
export function getMe() {
    return apiFetch<Me>('/me');
}