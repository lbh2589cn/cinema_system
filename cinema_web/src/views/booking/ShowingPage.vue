<template>
    <div class="page-container">
        <el-card class="page-card">
            <template #header>
                <span class="card-title">Select Showings</span>
            </template>
            <div class="date-selector">
                <el-radio-group v-model="selectedDate">
                    <el-radio-button v-for="d in dateOptions" :key="d.value" :value="d.value">
                        {{ d.label }}
                    </el-radio-button>
                </el-radio-group>
            </div>
            <div class="showings-list" v-loading="loading">
                <el-empty v-if="showings.length === 0" description="No showings available" />
                <el-card
                    v-for="showing in showings"
                    :key="showing.id"
                    class="showing-item"
                    shadow="hover"
                    @click="selectShowing(showing)"
                >
                    <div class="showing-time">{{ showing.showTime }}</div>
                    <div class="showing-info">
                        <span>Hall #{{ showing.hallId }}</span>
                        <span class="price">¥{{ showing.basePrice }}</span>
                    </div>
                </el-card>
            </div>
        </el-card>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getShowingsApi } from '@/api/showing'
import { useAppStore } from '@/stores/app'
import type { Showing } from '@/api/showing'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()

const allShowings = ref<Showing[]>([])
const loading = ref(false)
const selectedDate = ref('')

const dateOptions = computed(() => {
    const uniqueDates = new Set(allShowings.value.map(s => s.showDate))
    const sorted = Array.from(uniqueDates).sort()
    return sorted.slice(0, 7).map((value, i) => ({
        value,
        label: formatDateLabel(value, i),
    }))
})

function formatDateLabel(dateStr: string, index: number) {
    const today = new Date().toISOString().split('T')[0]
    const tomorrow = new Date(Date.now() + 86400000).toISOString().split('T')[0]
    if (dateStr === today) return 'Today'
    if (dateStr === tomorrow) return 'Tomorrow'
    const d = new Date(dateStr)
    return `${d.getDate()}/${d.getMonth() + 1}`
}

const showings = computed(() =>
    allShowings.value.filter(s => s.showDate === selectedDate.value)
)

function selectShowing(showing: Showing) {
    appStore.setCurrentShowing(showing.id)
    const movieId = route.query.movieId
    router.push(`/seats?movieId=${movieId}&showingId=${showing.id}`)
}

onMounted(async () => {
    loading.value = true
    try {
        const movieId = Number(route.query.movieId) || undefined
        allShowings.value = await getShowingsApi({ movieId })
        if (dateOptions.value.length > 0) {
            selectedDate.value = dateOptions.value[0].value
        }
    } finally {
        loading.value = false
    }
})
</script>

<style scoped lang="scss">
.card-title {
    font-size: 16px;
    font-weight: 600;
}

.date-selector {
    margin-bottom: 24px;
    display: flex;
    justify-content: center;
}

.showings-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.showing-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    cursor: pointer;

    .showing-time {
        font-size: 22px;
        font-weight: 600;
        color: #409eff;
    }

    .showing-info {
        display: flex;
        gap: 16px;
        color: #909399;
        font-size: 14px;

        .price {
            color: #f56c6c;
            font-weight: 600;
        }
    }
}
</style>
