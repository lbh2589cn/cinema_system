import request from './request'

export interface LoginRequest {
    userId: string
    password: string
}

export interface LoginResponse {
    token: string
    id: number
    userId: string
    username: string
    role: string
    nickname: string
}

export interface UserProfile {
    id: number
    userId: string
    username: string
    phone: string
    nickname: string
    role: string
    isMember: boolean
    status: string
}

export interface RegisterRequest {
    userId: string
    username: string
    password: string
    phone?: string
    nickname?: string
}

export function loginApi(data: LoginRequest): Promise<LoginResponse> {
    return request.post('/api/auth/login', data)
}

export function registerApi(data: RegisterRequest): Promise<void> {
    return request.post('/api/auth/register', data)
}

export function getProfileApi(): Promise<UserProfile> {
    return request.get('/api/users/me')
}

export function updateProfileApi(data: Partial<UserProfile>): Promise<void> {
    return request.put('/api/users/me', data)
}

export interface ChangePasswordRequest {
    oldPassword: string
    newPassword: string
}

export function changePasswordApi(data: ChangePasswordRequest): Promise<void> {
    return request.put('/api/users/me/password', data)
}

export function deleteAccountApi(): Promise<void> {
    return request.delete('/api/users/me')
}
