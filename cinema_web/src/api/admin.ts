import request from './request'

export interface DashboardStats {
    totalUsers: number
    totalMovies?: number
    totalOrders?: number
    totalRevenue?: number
}

export function getDashboardApi(): Promise<DashboardStats> {
    return request.get('/api/admin/dashboard')
}

export function getAdminUsersApi(params?: { page?: number; size?: number }): Promise<{ content: any[]; total: number }> {
    return request.get('/api/admin/users', { params })
}

export function updateAdminUserApi(id: number, data: { isMember?: boolean; status?: string }): Promise<void> {
    return request.put(`/api/admin/users/${id}`, data)
}
