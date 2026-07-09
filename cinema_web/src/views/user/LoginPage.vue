<template>
    <div class="login-page">
        <el-card class="login-card" shadow="always">
            <h2 class="title">Sign In</h2>
            <el-form :model="loginForm" :rules="rules" ref="formRef" label-width="0" @keyup.enter="handleLogin">
                <el-form-item prop="userId">
                    <el-input v-model="loginForm.userId" placeholder="Please enter User ID" size="large" prefix-icon="User" />
                </el-form-item>
                <el-form-item prop="password">
                    <el-input v-model="loginForm.password" type="password" placeholder="Please enter Password" size="large" prefix-icon="Lock" show-password />
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="handleLogin">
                        Sign In
                    </el-button>
                </el-form-item>
            </el-form>
            <div class="register-link">
                No account yet? <router-link to="/register">Sign Up Now</router-link>
            </div>
        </el-card>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const loginForm = reactive({
    userId: '',
    password: '',
})

const rules = {
    userId: [{ required: true, message: 'Please enter User ID', trigger: 'blur' }],
    password: [{ required: true, message: 'Please enter Password', trigger: 'blur' }],
}

async function handleLogin() {
    const valid = await formRef.value?.validate().catch(() => false)
    if (!valid) return
    loading.value = true
    try {
        await userStore.login(loginForm)
        await userStore.fetchUserInfo()
        ElMessage.success('Sign in successful')
        const redirect = (route.query.redirect as string) || '/movies'
        router.push(redirect)
    } catch {
        // Error handled by interceptor
    } finally {
        loading.value = false
    }
}
</script>

<style scoped lang="scss">
.login-page {
    min-height: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
    width: 400px;
    padding: 20px;

    .title {
        text-align: center;
        margin-bottom: 30px;
        font-size: 24px;
        color: #303133;
    }

    .submit-btn {
        width: 100%;
    }

    .register-link {
        text-align: center;
        font-size: 14px;
        color: #909399;
    }
}
</style>
