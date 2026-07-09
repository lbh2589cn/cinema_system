<template>
    <div class="register-page">
        <el-card class="register-card" shadow="always">
            <h2 class="title">Register</h2>
            <el-form :model="registerForm" :rules="rules" ref="formRef" label-width="0" @keyup.enter="handleRegister">
                <el-form-item prop="userId">
                    <el-input v-model="registerForm.userId" placeholder="Please enter Account" size="large" prefix-icon="User" />
                </el-form-item>
                <el-form-item prop="username">
                    <el-input v-model="registerForm.username" placeholder="Please enter Username" size="large" prefix-icon="Edit" />
                </el-form-item>
                <el-form-item prop="password">
                    <el-input v-model="registerForm.password" type="password" placeholder="Please enter Password (at least 6 characters)" size="large" prefix-icon="Lock" show-password />
                </el-form-item>
                <el-form-item prop="confirmPassword">
                    <el-input v-model="registerForm.confirmPassword" type="password" placeholder="Confirm Password" size="large" prefix-icon="Lock" show-password />
                </el-form-item>
                <el-form-item prop="phone">
                    <el-input v-model="registerForm.phone" placeholder="Phone Number (optional)" size="large" prefix-icon="Phone" />
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="handleRegister">
                        Register
                    </el-button>
                </el-form-item>
            </el-form>
            <div class="login-link">
                Already have an account? <router-link to="/login">Sign In Now</router-link>
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
        callback(new Error('The passwords entered do not match'))
    } else {
        callback()
    }
}

const rules = {
    userId: [{ required: true, message: 'Please enter Account', trigger: 'blur' }],
    username: [{ required: true, message: 'Please enter Username', trigger: 'blur' }],
    password: [
        { required: true, message: 'Please enter Password', trigger: 'blur' },
        { min: 6, message: 'Password must be at least 6 characters', trigger: 'blur' },
    ],
    confirmPassword: [
        { required: true, message: 'Please confirm Password', trigger: 'blur' },
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
        ElMessage.success('Registration successful, please sign in')
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
