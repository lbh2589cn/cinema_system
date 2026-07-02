<template>
    <el-card class="movie-card" :body-style="{ padding: '0px' }" shadow="hover" @click="goToDetail">
        <div class="movie-poster">
            <el-image :src="posterSrc" fit="cover">
                <template #error>
                    <div class="poster-placeholder">
                        <el-icon :size="48"><VideoCamera /></el-icon>
                    </div>
                </template>
            </el-image>
            <div class="movie-rating" v-if="movie.rating > 0">
                <el-icon><StarFilled /></el-icon>
                <span>{{ movie.rating }}</span>
            </div>
        </div>
        <div class="movie-info">
            <h3 class="movie-title">{{ movie.title }}</h3>
            <div class="movie-meta">
                <span class="genre">{{ movie.genre }}</span>
                <span class="duration">{{ movie.duration }} 分钟</span>
            </div>
        </div>
    </el-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import type { Movie } from '@/api/movie'

const props = defineProps<{ movie: Movie }>()
const router = useRouter()

const posterSrc = computed(() => {
    return props.movie.posterUrl || ''
})

function goToDetail() {
    router.push(`/movies/${props.movie.id}`)
}
</script>

<style scoped lang="scss">
.movie-card {
    cursor: pointer;
    transition: transform 0.2s;
    border-radius: 8px;
    overflow: hidden;

    &:hover {
        transform: translateY(-4px);
    }
}

.movie-poster {
    position: relative;
    width: 100%;
    height: 300px;
    overflow: hidden;
    background: #f0f0f0;

    .el-image {
        width: 100%;
        height: 100%;
    }

    .poster-placeholder {
        width: 100%;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #c0c4cc;
        background: #f5f7fa;
    }

    .movie-rating {
        position: absolute;
        top: 8px;
        right: 8px;
        background: rgba(0, 0, 0, 0.7);
        color: #fff;
        padding: 4px 8px;
        border-radius: 4px;
        font-size: 13px;
        display: flex;
        align-items: center;
        gap: 4px;
    }
}

.movie-info {
    padding: 12px;

    .movie-title {
        font-size: 16px;
        font-weight: 600;
        margin-bottom: 6px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
    }

    .movie-meta {
        display: flex;
        justify-content: space-between;
        font-size: 13px;
        color: #909399;
    }
}
</style>
