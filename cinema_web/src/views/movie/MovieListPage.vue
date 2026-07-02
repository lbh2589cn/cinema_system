<template>
    <div class="page-container">
        <div class="page-card">
            <div class="filters">
                <el-input v-model="keyword" placeholder="搜索电影名称" clearable style="width: 300px" @clear="search" @keyup.enter="search">
                    <template #prefix><el-icon><Search /></el-icon></template>
                </el-input>
                <el-select v-model="genre" placeholder="类型筛选" clearable style="width: 150px" @change="search">
                    <el-option label="科幻" value="Sci-Fi" />
                    <el-option label="动作" value="Action" />
                    <el-option label="冒险" value="Adventure" />
                    <el-option label="动画" value="Animation" />
                    <el-option label="剧情" value="Drama" />
                    <el-option label="奇幻" value="Fantasy" />
                    <el-option label="喜剧" value="Comedy" />
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
const page = ref(0)
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
            page: page.value,
            size: size.value,
        })
        movies.value = result.content
        total.value = result.total
    } finally {
        loading.value = false
    }
}

function search() {
    page.value = 0
    loadMovies()
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
