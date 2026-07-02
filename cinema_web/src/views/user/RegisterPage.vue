<template>
    <div class="register-page">
        <el-card class="register-card" shadow="always">
            <h2 class="title">注册</h2>
            <el-form :model="registerForm" :rules="rules" ref="formRef" label-width="0" @keyup.enter="handleRegister">
                <el-form-item prop="userId">
                    <el-input v-model="registerForm.userId" placeholder="请输入账号" size="large" prefix-icon="User" />
                </el-form-item>
                <el-form-item prop="username">
                    <el-input v-model="registerForm.username" placeholder="请输入用户名" size="large" prefix-icon="Edit" />
                </el-form-item>
                <el-form-item prop="password">
                    <el-input v-model="registerForm.password" type="password" placeholder="请输入密码（至少6位）" size="large" prefix-icon="Lock" show-password />
                </el-form-item>
                <el-form-item prop="confirmPassword">
                    <el-input v-model="registerForm.confirmPassword" type="password" placeholder="确认密码" size="large" prefix-icon="Lock" show-password />
                </el-form-item>
                <el-form-item prop="phone">
                    <el-input v-model="registerForm.phone" placeholder="手机号（选填）" size="large" prefix-icon="Phone" />
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="handleRegister">
                        注册
                    </el-button>
                </el-form-item>
            </el-form>
            <div class="login-link">
                已有账号？<router-link to="/login">立即登录</router-link>
            </div>
        </el-card>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { registerApi } from '@/api/auth'

const router = useRouter()
const formRef = ref()
const loading = ref(false)

const registerForm = reactive({
    userId: '',
    username: '',
    password: '',
    confirmPassword: '',
    phone: '',
})

const validatePass = (_rule: any, value: string, callback: any) => {
    if (value !== registerForm.password) {
        callback(new Error('两次输入的密码不一致'))
    } else {
        callback()
    }
}

const rules = {
    userId: [{ required: true, message: '请输入账号', trigger: 'blur' }],
    username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
    password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, message: '密码至少6位', trigger: 'blur' },
    ],
    confirmPassword: [
        { required: true, message: '请确认密码', trigger: 'blur' },
        { validator: validatePass, trigger: 'blur' },
    ],
}

async function handleRegister() {
    const valid = await formRef.value?.validate().catch(() => false)
    if (!valid) return
    loading.value = true
    try {
        await registerApi({
            userId: registerForm.userId,
            username: registerForm.username,
            password: registerForm.password,
            phone: registerForm.phone || undefined,
        })
        ElMessage.success('注册成功，请登录')
        router.push('/login')
    } catch {
        // Error handled by interceptor
    } finally {
        loading.value = false
    }
}
</script>

<style scoped lang="scss">
.register-page {
    min-height: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.register-card {
    width: 420px;
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

    .login-link {
        text-align: center;
        font-size: 14px;
        color: #909399;
    }
}
</style>
