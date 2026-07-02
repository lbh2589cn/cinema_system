<template>
    <div class="page-container">
        <el-card class="page-card" v-loading="loading">
            <template #header>
                <span class="card-title">支付</span>
            </template>
            <div class="payment-info" v-if="order">
                <div class="order-no">订单号：{{ order.orderNo }}</div>
                <div class="amount">应付金额：¥{{ order.finalAmount.toFixed(2) }}</div>
            </div>
            <el-divider />
            <h3>选择支付方式</h3>
            <div class="payment-methods">
                <el-radio-group v-model="paymentMethod">
                    <el-radio value="WECHAT" class="payment-method">
                        <el-icon><Iphone /></el-icon>
                        微信支付
                    </el-radio>
                    <el-radio value="ALIPAY" class="payment-method">
                        <el-icon><Wallet /></el-icon>
                        支付宝
                    </el-radio>
                </el-radio-group>
            </div>
            <div class="actions">
                <el-button type="primary" size="large" @click="handlePay" :loading="paying">
                    确认支付 ¥{{ order?.finalAmount.toFixed(2) }}
                </el-button>
            </div>
        </el-card>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getOrderApi } from '@/api/order'
import { payApi } from '@/api/payment'

const route = useRoute()
const router = useRouter()
const order = ref<any>(null)
const paymentMethod = ref('WECHAT')
const loading = ref(false)
const paying = ref(false)

async function handlePay() {
    paying.value = true
    try {
        await payApi({ orderId: order.value.id, paymentMethod: paymentMethod.value })
        ElMessage.success('支付成功')
        router.push(`/order/success?orderId=${order.value.id}`)
    } catch {
        // Error handled by interceptor
    } finally {
        paying.value = false
    }
}

onMounted(async () => {
    loading.value = true
    try {
        const orderId = Number(route.query.orderId)
        order.value = await getOrderApi(orderId)
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

.payment-info {
    text-align: center;

    .order-no {
        font-size: 14px;
        color: #909399;
        margin-bottom: 8px;
    }

    .amount {
        font-size: 32px;
        font-weight: 700;
        color: #f56c6c;
    }
}

.payment-methods {
    display: flex;
    flex-direction: column;
    gap: 12px;
    margin: 20px 0;

    .payment-method {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 12px 16px;
        border: 1px solid #dcdfe6;
        border-radius: 8px;
        width: 100%;
    }
}

.actions {
    display: flex;
    justify-content: center;
    margin-top: 24px;
}
</style>
