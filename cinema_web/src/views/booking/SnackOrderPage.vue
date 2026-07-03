<template>
    <div class="page-container">
        <el-card class="page-card">
            <template #header>
                <span class="card-title">选购零食</span>
            </template>
            <div class="snack-grid" v-loading="loading">
                <el-card v-for="snack in snacks" :key="snack.id" class="snack-card" shadow="hover">
                    <div class="snack-image">
                        <el-image :src="snack.imageUrl || ''" fit="contain">
                            <template #error>
                                <div class="snack-placeholder">
                                    <el-icon :size="36"><Goods /></el-icon>
                                </div>
                            </template>
                        </el-image>
                    </div>
                    <h4 class="snack-name">{{ snack.name }}</h4>
                    <div class="snack-price">¥{{ snack.price.toFixed(2) }}</div>
                    <div class="snack-actions">
                        <el-input-number
                            v-model="quantities[snack.id]"
                            :min="0"
                            :max="snack.stock"
                            size="small"
                        />
                    </div>
                </el-card>
            </div>
            <div class="actions">
                <span class="selected-total">
                    已选 {{ totalQuantity }} 件，合计 ¥{{ totalPrice.toFixed(2) }}
                </span>
                <el-button type="primary" size="large" @click="goToConfirm">
                    {{ hasSelection ? '去结算' : '跳过选购' }}
                </el-button>
            </div>
        </el-card>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getSnacksApi } from '@/api/snack'
import { useAppStore } from '@/stores/app'
import type { Snack } from '@/api/snack'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()

const snacks = ref<Snack[]>([])
const quantities = ref<Record<number, number>>({})
const loading = ref(false)

const hasSelection = computed(() => totalQuantity.value > 0)
const totalQuantity = computed(() => Object.values(quantities.value).reduce((a, b) => a + b, 0))
const totalPrice = computed(() => {
    return snacks.value.reduce((sum, snack) => {
        const qty = quantities.value[snack.id] || 0
        return sum + snack.price * qty
    }, 0)
})

function goToConfirm() {
    const selected = snacks.value
        .filter(s => (quantities.value[s.id] || 0) > 0)
        .map(s => ({ ...s, quantity: quantities.value[s.id] }))
    appStore.setSelectedSnacks(selected)
    const movieId = route.query.movieId || appStore.currentMovieId
    const showingId = route.query.showingId || appStore.currentShowingId
    router.push(`/order/confirm?movieId=${movieId}&showingId=${showingId}`)
}

onMounted(async () => {
    loading.value = true
    try {
        snacks.value = await getSnacksApi()
        snacks.value.forEach(s => { quantities.value[s.id] = 0 })
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

.snack-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 16px;
}

.snack-card {
    text-align: center;
    padding: 8px;

    .snack-image {
        height: 120px;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-bottom: 12px;

        .el-image, .snack-placeholder {
            width: 100%;
            height: 100%;
        }

        .snack-placeholder {
            display: flex;
            align-items: center;
            justify-content: center;
            color: #c0c4cc;
            background: #f5f7fa;
            border-radius: 8px;
        }
    }

    .snack-name {
        font-size: 15px;
        margin-bottom: 4px;
    }

    .snack-price {
        color: #f56c6c;
        font-size: 16px;
        font-weight: 600;
        margin-bottom: 12px;
    }
}

.actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 24px;
    padding-top: 16px;
    border-top: 1px solid #ebeef5;

    .selected-total {
        font-size: 14px;
        color: #606266;
    }
}
</style>
