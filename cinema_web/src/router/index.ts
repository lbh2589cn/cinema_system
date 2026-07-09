import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const routes: RouteRecordRaw[] = [
    // Public routes
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/user/LoginPage.vue'),
        meta: { requiresAuth: false },
    },
    {
        path: '/register',
        name: 'Register',
        component: () => import('@/views/user/RegisterPage.vue'),
        meta: { requiresAuth: false },
    },
    // User routes
    {
        path: '/',
        component: () => import('@/views/LayoutPage.vue'),
        redirect: '/movies',
        children: [
            {
                path: 'movies',
                name: 'MovieList',
                component: () => import('@/views/movie/MovieListPage.vue'),
                meta: { requiresAuth: false },
            },
            {
                path: 'movies/:id',
                name: 'MovieDetail',
                component: () => import('@/views/movie/MovieDetailPage.vue'),
                meta: { requiresAuth: false },
            },
            {
                path: 'showings',
                name: 'Showing',
                component: () => import('@/views/booking/ShowingPage.vue'),
                meta: { requiresAuth: true },
            },
            {
                path: 'seats',
                name: 'SeatSelect',
                component: () => import('@/views/booking/SeatSelectPage.vue'),
                meta: { requiresAuth: true },
            },
            {
                path: 'snacks',
                name: 'SnackOrder',
                component: () => import('@/views/booking/SnackOrderPage.vue'),
                meta: { requiresAuth: true },
            },
            {
                path: 'order/confirm',
                name: 'OrderConfirm',
                component: () => import('@/views/order/OrderConfirmPage.vue'),
                meta: { requiresAuth: true },
            },
            {
                path: 'order/payment',
                name: 'Payment',
                component: () => import('@/views/order/PaymentPage.vue'),
                meta: { requiresAuth: true },
            },
            {
                path: 'order/success',
                name: 'PaymentSuccess',
                component: () => import('@/views/order/PaymentSuccessPage.vue'),
                meta: { requiresAuth: true },
            },
            {
                path: 'orders',
                name: 'OrderList',
                component: () => import('@/views/order/OrderListPage.vue'),
                meta: { requiresAuth: true },
            },
            {
                path: 'orders/:id',
                name: 'OrderDetail',
                component: () => import('@/views/order/OrderDetailPage.vue'),
                meta: { requiresAuth: true },
            },
            {
                path: 'profile',
                name: 'Profile',
                component: () => import('@/views/user/ProfilePage.vue'),
                meta: { requiresAuth: true },
            },
        ],
    },
    // Admin routes
    {
        path: '/admin',
        component: () => import('@/views/admin/AdminLayoutPage.vue'),
        redirect: '/admin/dashboard',
        meta: { requiresAuth: true, role: 'ADMIN' },
        children: [
            {
                path: 'dashboard',
                name: 'Dashboard',
                component: () => import('@/views/admin/DashboardPage.vue'),
            },
            {
                path: 'movies',
                name: 'MovieManage',
                component: () => import('@/views/admin/MovieManagePage.vue'),
            },
            {
                path: 'showings',
                name: 'ShowingManage',
                component: () => import('@/views/admin/ShowingManagePage.vue'),
            },
            {
                path: 'halls',
                name: 'HallManage',
                component: () => import('@/views/admin/HallManagePage.vue'),
            },
            {
                path: 'users',
                name: 'UserManage',
                component: () => import('@/views/admin/UserManagePage.vue'),
            },
            {
                path: 'pricing',
                name: 'PricingRule',
                component: () => import('@/views/admin/PricingRulePage.vue'),
            },
            {
                path: 'orders',
                name: 'OrderManage',
                component: () => import('@/views/admin/OrderManagePage.vue'),
            },
            {
                path: 'payments',
                name: 'PaymentManage',
                component: () => import('@/views/admin/PaymentManagePage.vue'),
            },
            {
                path: 'snacks',
                name: 'SnackManage',
                component: () => import('@/views/admin/SnackManagePage.vue'),
            },
        ],
    },
]

const router = createRouter({
    history: createWebHistory(),
    routes,
})

router.beforeEach(async (to, _from, next) => {
    const userStore = useUserStore()

    // Not logged in → redirect to login page
    if (to.meta.requiresAuth && !userStore.isLoggedIn) {
        ElMessage.warning('Please sign in first')
        next({ name: 'Login', query: { redirect: to.fullPath } })
        return
    }
    // Logged in → login page → redirect to home
    if (to.name === 'Login' && userStore.isLoggedIn) {
        next({ name: 'MovieList' })
        return
    }
    // Requires admin permissions
    if (to.matched.some(r => r.meta.role === 'ADMIN')) {
        // Fetch user info if not loaded yet
        if (!userStore.userInfo) {
            try {
                await userStore.fetchUserInfo()
            } catch {
                userStore.logout()
                next({ name: 'Login', query: { redirect: to.fullPath } })
                return
            }
        }
        if (!userStore.isAdmin) {
            ElMessage.error('Access denied')
            next({ name: 'MovieList' })
            return
        }
    }
    next()
})

export default router
