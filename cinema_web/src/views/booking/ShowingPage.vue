<template>
    <div class="page-container">
        <el-card class="page-card">
            <template #header>
                <span class="card-title">选择场次</span>
            </template>
            <div class="date-selector">
                <el-radio-group v-model="selectedDate" @change="loadShowings">
                    <el-radio-button v-for="d in dateOptions" :key="d.value" :value="d.value">
                        {{ d.label }}
                    </el-radio-button>
                </el-radio-group>
            </div>
            <div class="showings-list" v-loading="loading">
                <el-empty v-if="showings.length === 0" description="暂无放映场次" />
                <el-card
                    v-for="showing in showings"
                    :key="showing.id"
                    class="showing-item"
                    shadow="hover"
                    @click="selectShowing(showing)"
                >
                    <div class="showing-time">{{ showing.showTime }}</div>
                    <div class="showing-info">
                        <span>影厅 #{{ showing.hallId }}</span>
                        <span class="price">¥{{ showing.basePrice }}</span>
                    </div>
                    <el-button type="primary" size="small">选座购票</el-button>
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

const showings = ref<Showing[]>([])
const loading = ref(false)
const selectedDate = ref('')

const dateOptions = computed(() => {
    const options = []
    for (let i = 0; i < 7; i++) {
        const d = new Date()
        d.setDate(d.getDate() + i)
        const value = d.toISOString().split('T')[0]
        const label = i === 0 ? '今天' : i === 1 ? '明天' : `${d.getMonth() + 1}/${d.getDate()}`
        options.push({ value, label })
    }
    return options
})

async function loadShowings() {
    loading.value = true
    try {
        showings.value = await getShowingsApi({
            movieId: Number(route.query.movieId) || undefined,
            date: selectedDate.value || undefined,
        })
    } finally {
        loading.value = false
    }
}

function selectShowing(showing: Showing) {
    appStore.setCurrentShowing(showing.id)
    router.push(`/seats?showingId=${showing.id}`)
}

onMounted(() => {
    selectedDate.value = dateOptions.value[0].value
    loadShowings()
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
