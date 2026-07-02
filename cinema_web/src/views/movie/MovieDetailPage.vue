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
                    <span>{{ movie.duration }} 分钟</span>
                    <span>{{ movie.genre }}</span>
                </div>
                <p class="description">{{ movie.description }}</p>
                <div class="actions">
                    <el-button type="primary" size="large" @click="goToBooking">
                        选择场次购票
                    </el-button>
                </div>

                <div class="showings-section" v-if="showings.length > 0">
                    <h3>放映场次</h3>
                    <div class="showings-list">
                        <el-card v-for="showing in showings" :key="showing.id" class="showing-card" shadow="hover" @click="selectShowing(showing)">
                            <div class="showing-date">{{ formatDate(showing.showDate) }}</div>
                            <div class="showing-time">{{ showing.showTime }}</div>
                            <div class="showing-price">¥{{ showing.basePrice }}</div>
                        </el-card>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getMovieApi } from '@/api/movie'
import { getShowingsApi } from '@/api/showing'
import { useAppStore } from '@/stores/app'
import type { Movie } from '@/api/movie'
import type { Showing } from '@/api/showing'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const movie = ref<Movie | null>(null)
const showings = ref<Showing[]>([])
const loading = ref(false)

const posterSrc = computed(() => movie.value?.posterUrl || '')

function formatDate(dateStr: string) {
    const d = new Date(dateStr)
    return `${d.getMonth() + 1}/${d.getDate()}`
}

function goToBooking() {
    router.push(`/showings?movieId=${movie.value?.id}`)
}

function selectShowing(showing: Showing) {
    appStore.setCurrentMovie(movie.value!.id)
    appStore.setCurrentShowing(showing.id)
    router.push(`/seats?showingId=${showing.id}`)
}

onMounted(async () => {
    loading.value = true
    try {
        const id = Number(route.params.id)
        movie.value = await getMovieApi(id)
        showings.value = await getShowingsApi({ movieId: id })
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
        margin-bottom: 32px;
    }
}

.showings-section {
    h3 {
        font-size: 18px;
        margin-bottom: 16px;
    }

    .showings-list {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
        gap: 12px;
    }

    .showing-card {
        cursor: pointer;
        text-align: center;
        padding: 8px;

        .showing-date {
            font-size: 13px;
            color: #909399;
        }

        .showing-time {
            font-size: 20px;
            font-weight: 600;
            margin: 8px 0;
            color: #409eff;
        }

        .showing-price {
            font-size: 16px;
            color: #f56c6c;
        }
    }
}
</style>
