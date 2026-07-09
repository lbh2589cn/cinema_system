<template>
    <div class="page-container">
        <div class="page-card">
            <div class="filters">
                <el-input v-model="keyword" placeholder="Search movie title" clearable style="width: 300px" @clear="search" @input="onKeywordInput">
                    <template #prefix><el-icon><Search /></el-icon></template>
                </el-input>
                <el-select v-model="genre" placeholder="Filter by Genre" clearable style="width: 150px" @change="search">
                    <el-option label="Sci-Fi" value="Sci-Fi" />
                    <el-option label="Action" value="Action" />
                    <el-option label="Adventure" value="Adventure" />
                    <el-option label="Animation" value="Animation" />
                    <el-option label="Drama" value="Drama" />
                    <el-option label="Fantasy" value="Fantasy" />
                    <el-option label="Comedy" value="Comedy" />
                </el-select>
                <el-select v-model="sortBy" style="width: 200px" @change="search">
                    <el-option label="Release Date (New→Old)" value="releaseDate_desc" />
                    <el-option label="Rating (High→Low)" value="rating_desc" />
                </el-select>
            </div>
            <div class="movie-grid" v-loading="loading">
                <MovieCard v-for="movie in movies" :key="movie.id" :movie="movie" />
            </div>
            <div class="pagination" v-if="total > 0">
                <el-pagination
                    v-model:current-page="page"
                    :page-size="size"
                    :total="total"
                    layout="prev, pager, next"
                    @current-change="loadMovies"
                />
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getMoviesApi } from '@/api/movie'
import type { Movie } from '@/api/movie'

const movies = ref<Movie[]>([])
const keyword = ref('')
const genre = ref('')
const sortBy = ref('releaseDate_desc')
const page = ref(1)
const size = ref(20)
const total = ref(0)
const loading = ref(false)

async function loadMovies() {
    loading.value = true
    try {
        const result = await getMoviesApi({
            keyword: keyword.value || undefined,
            genre: genre.value || undefined,
            status: 'ON',
            page: page.value - 1,
            size: size.value,
            sortBy: sortBy.value.split('_')[0],
            sortDir: sortBy.value.split('_')[1],
        })
        movies.value = result.content
        total.value = result.total
    } finally {
        loading.value = false
    }
}

function search() {
    page.value = 1
    loadMovies()
}

let debounceTimer: ReturnType<typeof setTimeout> | null = null
function onKeywordInput() {
    if (debounceTimer) clearTimeout(debounceTimer)
    debounceTimer = setTimeout(() => {
        search()
    }, 300)
}

onMounted(loadMovies)
</script>

<style scoped lang="scss">
.filters {
    display: flex;
    gap: 16px;
    margin-bottom: 24px;
}

.pagination {
    display: flex;
    justify-content: center;
    margin-top: 24px;
}
</style>
