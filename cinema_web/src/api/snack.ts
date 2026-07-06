import request from './request'

export interface Snack {
    id: number
    name: string
    price: number
    imageUrl: string
    stock: number
    status: string
}

export function getSnacksApi(): Promise<Snack[]> {
    return request.get('/api/snacks')
}

export function getAllSnacksApi(): Promise<Snack[]> {
    return request.get('/api/snacks/all')
}

export function createSnackApi(data: { name: string; price: number; imageUrl?: string; stock: number }): Promise<Snack> {
    return request.post('/api/snacks', data)
}

export function updateSnackApi(id: number, data: Partial<Snack>): Promise<Snack> {
    return request.put(`/api/snacks/${id}`, data)
}

export function deleteSnackApi(id: number): Promise<void> {
    return request.delete(`/api/snacks/${id}`)
}
