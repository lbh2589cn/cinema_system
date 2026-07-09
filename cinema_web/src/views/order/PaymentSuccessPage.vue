<template>
    <div class="page-container">
        <el-card class="page-card success-card">
            <div class="success-icon">
                <el-icon :size="64" color="#67c23a"><CircleCheck /></el-icon>
            </div>
            <h2>Payment Successful!</h2>
            <p class="order-no" v-if="order">Order No.: {{ order.orderNo }}</p>
            <div class="actions">
                <el-button type="primary" @click="router.push(`/orders/${order?.id}`)">
                    View Order
                </el-button>
            </div>
        </el-card>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getOrderApi } from '@/api/order'

const route = useRoute()
const router = useRouter()
const order = ref<any>(null)

onMounted(async () => {
    try {
        const orderId = Number(route.query.orderId)
        order.value = await getOrderApi(orderId)
    } catch {
        // ignore
    }
})
</script>

<style scoped lang="scss">
.success-card {
    text-align: center;
    padding: 60px 40px;

    .success-icon {
        margin-bottom: 20px;
    }

    h2 {
        font-size: 24px;
        margin-bottom: 12px;
    }

    .order-no {
        color: #909399;
        margin-bottom: 32px;
    }

    .actions {
        display: flex;
        justify-content: center;
        gap: 12px;
    }
}
</style>
