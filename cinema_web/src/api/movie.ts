import request from './request'

export interface Movie {
    id: number
    title: string
    posterUrl: string
    description: string
    duration: number
    rating: number
    genre: string
    releaseDate: string
    status: string
    createdAt: string
    updatedAt: string
}

export interface MovieCreateRequest {
    title: string
    posterUrl?: string
    description?: string
    duration: number
    rating?: number
    genre?: string
    releaseDate?: string
    status?: string
}

export function getMoviesApi(params?: { keyword?: string; genre?: string; status?: string; page?: number; size?: number }): Promise<{ content: Movie[]; total: number }> {
    return request.get('/api/movies', { params })
}

export function getMovieApi(id: number): Promise<Movie> {
    return request.get(`/api/movies/${id}`)
}

export function createMovieApi(data: MovieCreateRequest): Promise<Movie> {
    return request.post('/api/movies', data)
}

export function updateMovieApi(id: number, data: Partial<MovieCreateRequest>): Promise<Movie> {
    return request.put(`/api/movies/${id}`, data)
}

export function deleteMovieApi(id: number): Promise<void> {
    return request.delete(`/api/movies/${id}`)
}
