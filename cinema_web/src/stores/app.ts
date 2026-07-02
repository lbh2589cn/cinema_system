import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
    const sidebarCollapsed = ref(false)
    const currentMovieId = ref<number | null>(null)
    const currentShowingId = ref<number | null>(null)
    const selectedSeats = ref<any[]>([])
    const selectedSnacks = ref<any[]>([])

    function setCurrentMovie(id: number) {
        currentMovieId.value = id
    }

    function setCurrentShowing(id: number) {
        currentShowingId.value = id
    }

    function setSelectedSeats(seats: any[]) {
        selectedSeats.value = seats
    }

    function setSelectedSnacks(snacks: any[]) {
        selectedSnacks.value = snacks
    }

    function toggleSidebar() {
        sidebarCollapsed.value = !sidebarCollapsed.value
    }

    function resetBooking() {
        currentMovieId.value = null
        currentShowingId.value = null
        selectedSeats.value = []
        selectedSnacks.value = []
    }

    return {
        sidebarCollapsed,
        currentMovieId,
        currentShowingId,
        selectedSeats,
        selectedSnacks,
        setCurrentMovie,
        setCurrentShowing,
        setSelectedSeats,
        setSelectedSnacks,
        toggleSidebar,
        resetBooking,
    }
})
