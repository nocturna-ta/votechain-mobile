package com.nocturna.votechain.utils

/**
 * Data class containing all localized strings used in the application
 */
data class LocalizedStrings(
    // Common
    val appName: String,
    val loading: String,
    val error: String,
    val retry: String,
    val submit: String,
    val cancel: String,
    val close: String,
    val next: String,
    val back: String,
    val done: String,

    // Settings
    val settings: String,
    val darkMode: String,
    val language: String,
    val about: String,
    val faq: String,

    // Navigation
    val homeNav: String,
    val votesNav: String,
    val profileNav: String,

    // Login & Registration
    val login: String,
    val loginAccount: String,
    val loginDescription: String,
    val email: String,
    val password: String,
    val forgotPassword: String,
    val register: String,
    val registerAccount: String,
    val registerDescription: String,
    val dontHaveAccount: String,
    val alreadyHaveAccount: String,

    // Registration Form
    val nationalId: String,
    val fullName: String,
    val birthPlace: String,
    val birthDate: String,
    val address: String,
    val province: String,
    val region: String,
    val gender: String,
    val male: String,
    val female: String,
    val setPassword: String,
    val uploadKtp: String,
    val uploadKtpDescription: String,
    val browseFiles: String,

    // Registration States
    val verifyingData: String,
    val verifyingDataDescription: String,
    val verificationApproved: String,
    val verificationApprovedDescription: String,
    val verificationDenied: String,
    val verificationDeniedDescription: String,
    val retryRegistration: String,

    // Home
    val empoweringDemocracy: String,
    val oneVoteAtATime: String,
    val latestNews: String,
    val activeVotesList: String,
    val moreDetails: String,

    // Voting
    val candidatePresident: String,
    val allCandidates: String,
    val candidateSelection: String,
    val selectCandidate: String,
    val presidentialCandidate: String,
    val vicePresidentialCandidate: String,
    val proposingParties: String,
    val visionMission: String,
    val vision: String,
    val mission: String,
    val vote: String,
    val voteComplete: String,
    val voteIncomplete: String,
    val votingVerification: String,
    val otpVerification: String,
    val otpDescription: String,
    val didntReceiveOtp: String,
    val resendOtp: String,
    val verify: String,
    val verifyToVote: String,

    // Candidate Details
    val detailCandidate: String,
    val personalInfo: String,
    val genderCandidate: String,
    val birthInfo: String,
    val religion: String,
    val education: String,
    val occupation: String,
    val educationHistory: String,
    val workHistory: String,
    val institution: String,
    val position: String,
    val period: String,
    val candidate: String,
    val viewProfile: String,

    // Results
    val results: String,
    val totalVotes: String,
    val votes: String,
    val noResultsAvailable: String,
    val noResultsDescription: String,

    // Profile
    val account: String,
    val balance: String,
    val nik: String,
    val privateKey: String,
    val publicKey: String,
    val view: String,
    val moreInformation: String,

    // Notifications
    val notification: String,
    val noNotifications: String
)

/**
 * Get localized strings based on the selected language
 */
fun getLocalizedStrings(language: String): LocalizedStrings {
    return when (language) {
        "Indonesia" -> getIndonesianStrings()
        else -> getEnglishStrings()
    }
}

/**
 * Get strings in Indonesian language
 */
private fun getIndonesianStrings(): LocalizedStrings {
    return LocalizedStrings(
        // Common
        appName = "VoteChain",
        loading = "Memuat",
        error = "Error",
        retry = "Coba Lagi",
        submit = "Kirim",
        cancel = "Batal",
        close = "Tutup",
        next = "Lanjut",
        back = "Kembali",
        done = "Selesai",

        // Settings
        settings = "Pengaturan",
        darkMode = "Mode Gelap",
        language = "Bahasa",
        about = "Tentang",
        faq = "Pertanyaan Umum",

        // Navigation
        homeNav = "Beranda",
        votesNav = "Voting",
        profileNav = "Profil",

        // Login & Registration
        login = "Masuk",
        loginAccount = "Masuk ke Akun",
        loginDescription = "Akses akun Anda untuk dapat berpartisipasi dan mengelola aktivitas pemungutan suara",
        email = "Email",
        password = "Kata Sandi",
        forgotPassword = "Lupa Kata Sandi?",
        register = "Daftar",
        registerAccount = "Buat Akun",
        registerDescription = "Buat akun untuk mengakses dan menjelajahi semua kesempatan pemungutan suara yang tersedia",
        dontHaveAccount = "Belum punya akun? ",
        alreadyHaveAccount = "Sudah punya akun? ",

        // Registration Form
        nationalId = "Nomor Induk Kependudukan",
        fullName = "Nama Lengkap",
        birthPlace = "Tempat Lahir",
        birthDate = "Tanggal Lahir",
        address = "Alamat",
        province = "Provinsi",
        region = "Kabupaten/Kota",
        gender = "Jenis Kelamin",
        male = "Laki-laki",
        female = "Perempuan",
        setPassword = "Buat Kata Sandi",
        uploadKtp = "Unggah Kartu Tanda Penduduk (KTP) Anda",
        uploadKtpDescription = "Format file yang diterima: JPG, JPEG, dan PNG",
        browseFiles = "Pilih File",

        // Registration States
        verifyingData = "Memverifikasi Data Anda",
        verifyingDataDescription = "Kami sedang memeriksa data Anda dengan Daftar Pemilih. Proses ini akan selesai dalam waktu singkat",
        verificationApproved = "Verifikasi Disetujui",
        verificationApprovedDescription = "Selamat! Akun Anda telah berhasil dibuat. Silakan masuk untuk mengakses akun Anda dan berpartisipasi dalam pemilihan",
        verificationDenied = "Verifikasi Ditolak",
        verificationDeniedDescription = "Data Anda tidak dapat diverifikasi dalam Daftar Pemilih. Harap periksa kembali detail Anda dan coba lagi",
        retryRegistration = "Coba Pendaftaran Lagi",

        // Home
        empoweringDemocracy = "Pemberdayaan Demokrasi",
        oneVoteAtATime = "Satu Suara Setiap Kali",
        latestNews = "Berita Terbaru",
        activeVotesList = "Daftar Voting Aktif",
        moreDetails = "Detail lebih lanjut",

        // Voting
        candidatePresident = "Kandidat Presiden",
        allCandidates = "Semua Kandidat",
        candidateSelection = "Pemilihan Kandidat",
        selectCandidate = "Pilih Kandidat",
        presidentialCandidate = "Kandidat Presiden",
        vicePresidentialCandidate = "Kandidat Wakil Presiden",
        proposingParties = "Partai Pengusung",
        visionMission = "Visi & Misi",
        vision = "Visi:",
        mission = "Misi:",
        vote = "Voting",
        voteComplete = "Sudah Memilih",
        voteIncomplete = "Belum Memilih",
        votingVerification = "Verifikasi Voting",
        otpVerification = "Verifikasi OTP",
        otpDescription = "Masukkan OTP yang dikirim ke nomor telepon terdaftar Anda untuk memverifikasi identitas Anda sebelum memilih",
        didntReceiveOtp = "Tidak menerima OTP? ",
        resendOtp = "Kirim Ulang OTP",
        verify = "Verifikasi",
        verifyToVote = "Verifikasi untuk Memilih",

        // Candidate Details
        detailCandidate = "Detail Kandidat",
        personalInfo = "Informasi Pribadi",
        genderCandidate = "Jenis Kelamin",
        birthInfo = "Tempat/Tanggal Lahir",
        religion = "Agama",
        education = "Pendidikan Terakhir",
        occupation = "Pekerjaan",
        educationHistory = "Riwayat Pendidikan",
        workHistory = "Riwayat Pekerjaan",
        institution = "Nama Institusi",
        position = "Jabatan",
        period = "Periode",
        candidate = "Kandidat",
        viewProfile = "Lihat Profil",

        // Results
        results = "Hasil",
        totalVotes = "Total Suara",
        votes = "suara",
        noResultsAvailable = "Tidak ada hasil voting tersedia",
        noResultsDescription = "Hasil akan muncul di sini setelah periode voting berakhir",

        // Profile
        account = "Akun",
        balance = "Saldo",
        nik = "Nomor Induk Kependudukan",
        privateKey = "Private Key",
        publicKey = "Public Key",
        view = "Lihat",
        moreInformation = "Informasi Lebih Lanjut",

        // Notifications
        notification = "Notifikasi",
        noNotifications = "Tidak ada notifikasi"
    )
}

/**
 * Get strings in English language
 */
private fun getEnglishStrings(): LocalizedStrings {
    return LocalizedStrings(
        // Common
        appName = "VoteChain",
        loading = "Loading",
        error = "Error",
        retry = "Retry",
        submit = "Submit",
        cancel = "Cancel",
        close = "Close",
        next = "Next",
        back = "Back",
        done = "Done",

        // Settings
        settings = "Settings",
        darkMode = "Dark Mode",
        language = "Language",
        about = "About",
        faq = "FAQ",

        // Navigation
        homeNav = "Home",
        votesNav = "Votes",
        profileNav = "Profile",

        // Login & Registration
        login = "Login",
        loginAccount = "Login Account",
        loginDescription = "Access your account to participate in and manage your voting activities",
        email = "Email",
        password = "Password",
        forgotPassword = "Forgot Password?",
        register = "Register",
        registerAccount = "Create Account",
        registerDescription = "Create an account to access and explore all available voting opportunities",
        dontHaveAccount = "Don't have an account? ",
        alreadyHaveAccount = "Already have an account? ",

        // Registration Form
        nationalId = "National Identification Number",
        fullName = "Full Name",
        birthPlace = "Birth Place",
        birthDate = "Birth Date",
        address = "Address",
        province = "Province",
        region = "Region",
        gender = "Gender",
        male = "Male",
        female = "Female",
        setPassword = "Set Password",
        uploadKtp = "Upload your ID card (KTP)",
        uploadKtpDescription = "Accepted file formats: JPG, JPEG, and PNG",
        browseFiles = "Browse files",

        // Registration States
        verifyingData = "Verifying Your Data",
        verifyingDataDescription = "We're cross-checking your data with the Voter List. This process will be completed shortly",
        verificationApproved = "Verification Approved",
        verificationApprovedDescription = "Congratulations! Your account has been created successfully. Please log in to access your account and participate in the election",
        verificationDenied = "Verification Denied",
        verificationDeniedDescription = "Your data could not be verified in the Voter List. Please review your details and try again",
        retryRegistration = "Retry Registration",

        // Home
        empoweringDemocracy = "Empowering Democracy",
        oneVoteAtATime = "One Vote at a Time",
        latestNews = "Latest News",
        activeVotesList = "Active Votes List",
        moreDetails = "More details",

        // Voting
        candidatePresident = "Candidate President",
        allCandidates = "All Candidates",
        candidateSelection = "Candidate Selection",
        selectCandidate = "Select Candidate",
        presidentialCandidate = "Presidential Candidate",
        vicePresidentialCandidate = "Vice Presidential Candidate",
        proposingParties = "Proposing Parties",
        visionMission = "Vision & Mission",
        vision = "Vision:",
        mission = "Mission:",
        vote = "Vote",
        voteComplete = "Vote Complete",
        voteIncomplete = "Vote Incomplete",
        votingVerification = "Voting Verification",
        otpVerification = "OTP Verification",
        otpDescription = "Enter the OTP sent to your registered phone number to verify your identity before voting",
        didntReceiveOtp = "Didn't you receive the OTP? ",
        resendOtp = "Resend OTP",
        verify = "Verify",
        verifyToVote = "Verify to Vote",

        // Candidate Details
        detailCandidate = "Detail Candidate",
        personalInfo = "Personal Information",
        genderCandidate = "Gender",
        birthInfo = "Birth Place/Date",
        religion = "Religion",
        education = "Last Education",
        occupation = "Occupation",
        educationHistory = "Education History",
        workHistory = "Work History",
        institution = "Institution",
        position = "Position",
        period = "Period",
        candidate = "Candidate",
        viewProfile = "View Profile",

        // Results
        results = "Results",
        totalVotes = "Total Votes",
        votes = "votes",
        noResultsAvailable = "No voting results available",
        noResultsDescription = "Results will appear here after voting periods end",

        // Profile
        account = "Account",
        balance = "Balance",
        nik = "National Identification Number",
        privateKey = "Private Key",
        publicKey = "Public Key",
        view = "View",
        moreInformation = "More Information",

        // Notifications
        notification = "Notification",
        noNotifications = "No notifications"
    )
}