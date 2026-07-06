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

export function getAdminOrdersApi(params?: { page?: number; size?: number }): Promise<{ content: any[]; total: number }> {
    return request.get('/api/admin/orders', { params })
}

export function hardDeleteOrderApi(id: number): Promise<void> {
    return request.delete(`/api/admin/orders/${id}`)
}

export function getAdminHallsApi(params?: { page?: number; size?: number }): Promise<{ content: any[]; total: number }> {
    return request.get('/api/admin/halls', { params })
}

export function getAdminSnacksApi(params?: { page?: number; size?: number }): Promise<{ content: any[]; total: number }> {
    return request.get('/api/admin/snacks', { params })
}

export function getAdminShowingsApi(params?: { date?: string; page?: number; size?: number }): Promise<{ content: any[]; total: number }> {
    return request.get('/api/admin/showings', { params })
}

export function getAdminPricingRulesApi(params?: { page?: number; size?: number }): Promise<{ content: any[]; total: number }> {
    return request.get('/api/admin/pricing-rules', { params })
}
