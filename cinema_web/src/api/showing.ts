import request from './request'

export interface Showing {
    id: number
    movieId: number
    hallId: number
    showDate: string
    showTime: string
    basePrice: number
    status: string
}

export function getShowingsApi(params?: { movieId?: number; date?: string }): Promise<Showing[]> {
    return request.get('/api/showings', { params })
}

export function getShowingApi(id: number): Promise<Showing> {
    return request.get(`/api/showings/${id}`)
}

export function createShowingApi(data: { movieId: number; hallId: number; showDate: string; showTime: string; basePrice: number }): Promise<Showing> {
    return request.post('/api/showings', data)
}

export function updateShowingApi(id: number, data: { movieId?: number; hallId?: number; showDate?: string; showTime?: string; basePrice?: number }): Promise<Showing> {
    return request.put(`/api/showings/${id}`, data)
}

export function cancelShowingApi(id: number): Promise<void> {
    return request.post(`/api/showings/${id}/cancel`)
}

export function restoreShowingApi(id: number): Promise<void> {
    return request.post(`/api/showings/${id}/restore`)
}

export function deleteShowingApi(id: number): Promise<void> {
    return request.delete(`/api/showings/${id}`)
}
