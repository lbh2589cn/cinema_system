<template>
    <div class="page-container" v-loading="loading">
        <div class="movie-detail" v-if="movie">
            <div class="movie-poster">
                <el-image :src="posterSrc" fit="cover">
                    <template #error>
                        <div class="poster-placeholder">
                            <el-icon :size="64"><VideoCamera /></el-icon>
                        </div>
                    </template>
                </el-image>
            </div>
            <div class="movie-info">
                <h1 class="title">{{ movie.title }}</h1>
                <div class="meta">
                    <span class="rating" v-if="movie.rating > 0">
                        <el-icon><StarFilled /></el-icon> {{ movie.rating }}
                    </span>
                    <span>{{ movie.duration }} minutes</span>
                    <span>{{ movie.genre }}</span>
                </div>
                <p class="description">{{ movie.description }}</p>
                <div class="actions">
                    <el-button type="primary" size="large" @click="goToBooking">
                        Select showing to purchase
                    </el-button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getMovieApi } from '@/api/movie'
import { useAppStore } from '@/stores/app'
import type { Movie } from '@/api/movie'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const movie = ref<Movie | null>(null)
const loading = ref(false)

const posterSrc = computed(() => movie.value?.posterUrl || '')

function goToBooking() {
    if (movie.value?.id) {
        appStore.setCurrentMovie(movie.value.id)
    }
    router.push(`/showings?movieId=${movie.value?.id}`)
}

onMounted(async () => {
    loading.value = true
    try {
        const id = Number(route.params.id)
        movie.value = await getMovieApi(id)
    } finally {
        loading.value = false
    }
})
</script>

<style scoped lang="scss">
.movie-detail {
    display: flex;
    gap: 40px;
    background: #fff;
    border-radius: 12px;
    padding: 40px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.movie-poster {
    flex-shrink: 0;
    width: 300px;
    height: 450px;
    border-radius: 8px;
    overflow: hidden;
    background: #f0f0f0;

    .el-image, .poster-placeholder {
        width: 100%;
        height: 100%;
    }

    .poster-placeholder {
        display: flex;
        align-items: center;
        justify-content: center;
        color: #c0c4cc;
    }
}

.movie-info {
    flex: 1;

    .title {
        font-size: 28px;
        margin-bottom: 16px;
    }

    .meta {
        display: flex;
        gap: 20px;
        color: #909399;
        margin-bottom: 20px;
        font-size: 14px;

        .rating {
            color: #e6a23c;
        }
    }

    .description {
        color: #606266;
        line-height: 1.8;
        margin-bottom: 24px;
    }

    .actions {
        margin-top: 24px;
    }
}

.page-header {
    margin-bottom: 16px;
}
</style>
