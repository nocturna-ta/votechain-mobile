package com.nocturna.votechain.data.fallback

import com.nocturna.votechain.R
import com.nocturna.votechain.data.model.Candidate
import com.nocturna.votechain.data.model.Education
import com.nocturna.votechain.data.model.ElectionPair
import com.nocturna.votechain.data.model.Party
import com.nocturna.votechain.data.model.SupportingParty
import com.nocturna.votechain.data.model.WorkExperience

/**
 * Fallback election data for offline use
 * Shared between CandidatePresidentScreen and CandidateSelectionScreen
 */
object FallbackElectionData {

    /**
     * Get fallback election pairs data
     */
    fun getFallbackElectionPairs(): List<ElectionPair> {
        return listOf(
            createCandidatePair1(),
            createCandidatePair2(),
            createCandidatePair3()
        )
    }

    /**
     * Candidate Pair 1: Anies - Muhaimin
     */
    private fun createCandidatePair1(): ElectionPair {
        return ElectionPair(
            id = "fallback-pair-1",
            election_no = "1",
            is_active = true,
            pair_photo_path = "",
            president = Candidate(
                full_name = "H. Anies Rasyid Baswedan, Ph. D.",
                gender = "Male",
                birth_place = "Kuningan, Jawa Barat",
                birth_date = "1969-05-07",
                religion = "Islam",
                last_education = "Ph.D. Political Science",
                job = "Former Governor of DKI Jakarta",
                photo_path = "",
                education_history = listOf(
                    Education("Universitas Gadjah Mada", "1995"),
                    Education("University of Maryland, College Park", "1999"),
                    Education("University of Chicago", "2004")
                ),
                work_experience = listOf(
                    WorkExperience("DKI Jakarta Provincial Government", "Governor", "2017-2022"),
                    WorkExperience("Ministry of Education and Culture", "Minister", "2014-2016"),
                    WorkExperience("Universitas Paramadina", "Rector", "2007-2014"),
                    WorkExperience("Ford Foundation", "Program Officer", "2005-2006")
                )
            ),
            vice_president = Candidate(
                full_name = "DR. (H.C.) H. A. Muhaimin Iskandar",
                gender = "Male",
                birth_place = "Madura, Jawa Timur",
                birth_date = "1966-12-05",
                religion = "Islam",
                last_education = "Doctorate (Honorary)",
                job = "Chairman of PKB Party",
                photo_path = "",
                education_history = listOf(
                    Education("IAIN Sunan Ampel Surabaya", "1992"),
                    Education("Universitas Airlangga", "1998"),
                    Education("Honorary Doctorate from various universities", "2015")
                ),
                work_experience = listOf(
                    WorkExperience("DPR RI", "Deputy Speaker", "2019-2024"),
                    WorkExperience("Partai Kebangkitan Bangsa (PKB)", "General Chairman", "2014-present"),
                    WorkExperience("DPR RI", "Member", "2004-2019"),
                    WorkExperience("Various Islamic Organizations", "Leader", "1990-present")
                )
            ),
            vote_count = 0,
            supporting_parties = listOf(
                SupportingParty(
                    id = "sp-1-1",
                    election_pair_id = "fallback-pair-1",
                    party_id = "party-pkb",
                    party = Party(
                        id = "party-pkb",
                        name = "Partai Kebangkitan Bangsa",
                        logo_path = ""
                    )
                ),
                SupportingParty(
                    id = "sp-1-2",
                    election_pair_id = "fallback-pair-1",
                    party_id = "party-pks",
                    party = Party(
                        id = "party-pks",
                        name = "Partai Keadilan Sejahtera",
                        logo_path = ""
                    )
                ),
                SupportingParty(
                    id = "sp-1-3",
                    election_pair_id = "fallback-pair-1",
                    party_id = "party-nasdem",
                    party = Party(
                        id = "party-nasdem",
                        name = "Partai Nasional Demokrat",
                        logo_path = ""
                    )
                )
            )
        )
    }

    /**
     * Candidate Pair 2: Prabowo - Gibran
     */
    private fun createCandidatePair2(): ElectionPair {
        return ElectionPair(
            id = "fallback-pair-2",
            election_no = "2",
            is_active = true,
            pair_photo_path = "",
            president = Candidate(
                full_name = "H. Prabowo Subianto",
                gender = "Male",
                birth_place = "Jakarta",
                birth_date = "1951-10-17",
                religion = "Islam",
                last_education = "Military Academy Graduate",
                job = "Former Minister of Defense",
                photo_path = "",
                education_history = listOf(
                    Education("Akademi Militer Magelang", "1974"),
                    Education("Infantry Officer Advanced Course (USA)", "1985"),
                    Education("Command and General Staff College (USA)", "1990")
                ),
                work_experience = listOf(
                    WorkExperience("Ministry of Defense", "Minister of Defense", "2019-2024"),
                    WorkExperience("Partai Gerakan Indonesia Raya (Gerindra)", "Chairman", "2008-present"),
                    WorkExperience("Indonesian National Armed Forces (TNI)", "Lieutenant General", "1974-1998"),
                    WorkExperience("Kopassus", "Commander", "1995-1998")
                )
            ),
            vice_president = Candidate(
                full_name = "Gibran Rakabuming Raka",
                gender = "Male",
                birth_place = "Surakarta, Jawa Tengah",
                birth_date = "1987-10-01",
                religion = "Islam",
                last_education = "Bachelor's Degree in Management",
                job = "Mayor of Surakarta",
                photo_path = "",
                education_history = listOf(
                    Education("FEB Universitas Gadjah Mada", "2010"),
                    Education("Université Pierre Mendès France", "2011")
                ),
                work_experience = listOf(
                    WorkExperience("Surakarta City Government", "Mayor", "2021-present"),
                    WorkExperience("Markobar & Mangkobar", "Entrepreneur", "2010-2021"),
                    WorkExperience("Various Business Ventures", "CEO/Founder", "2010-2021")
                )
            ),
            vote_count = 0,
            supporting_parties = listOf(
                SupportingParty(
                    id = "sp-2-1",
                    election_pair_id = "fallback-pair-2",
                    party_id = "party-gerindra",
                    party = Party(
                        id = "party-gerindra",
                        name = "Partai Gerakan Indonesia Raya",
                        logo_path = ""
                    )
                ),
                SupportingParty(
                    id = "sp-2-2",
                    election_pair_id = "fallback-pair-2",
                    party_id = "party-golkar",
                    party = Party(
                        id = "party-golkar",
                        name = "Partai Golongan Karya",
                        logo_path = ""
                    )
                ),
                SupportingParty(
                    id = "sp-2-3",
                    election_pair_id = "fallback-pair-2",
                    party_id = "party-pan",
                    party = Party(
                        id = "party-pan",
                        name = "Partai Amanat Nasional",
                        logo_path = ""
                    )
                ),
                SupportingParty(
                    id = "sp-2-4",
                    election_pair_id = "fallback-pair-2",
                    party_id = "party-demokrat",
                    party = Party(
                        id = "party-demokrat",
                        name = "Partai Demokrat",
                        logo_path = ""
                    )
                ),
                SupportingParty(
                    id = "sp-2-5",
                    election_pair_id = "fallback-pair-2",
                    party_id = "party-psi",
                    party = Party(
                        id = "party-psi",
                        name = "Partai Solidaritas Indonesia",
                        logo_path = ""
                    )
                ),
                SupportingParty(
                    id = "sp-2-6",
                    election_pair_id = "fallback-pair-2",
                    party_id = "party-pbb",
                    party = Party(
                        id = "party-pbb",
                        name = "Partai Bulan Bintang",
                        logo_path = ""
                    )
                )
            )
        )
    }

    /**
     * Candidate Pair 3: Ganjar - Mahfud
     */
    private fun createCandidatePair3(): ElectionPair {
        return ElectionPair(
            id = "fallback-pair-3",
            election_no = "3",
            is_active = true,
            pair_photo_path = "",
            president = Candidate(
                full_name = "H. Ganjar Pranowo, S.H., M.I.P.",
                gender = "Male",
                birth_place = "Karanganyar, Jawa Tengah",
                birth_date = "1968-10-28",
                religion = "Islam",
                last_education = "Master of International Politics",
                job = "Former Governor of Central Java",
                photo_path = "",
                education_history = listOf(
                    Education("Universitas Gadjah Mada Faculty of Law", "1992"),
                    Education("Universitas Gadjah Mada (Master)", "1997"),
                    Education("Various Leadership Programs", "2000-2010")
                ),
                work_experience = listOf(
                    WorkExperience("Central Java Provincial Government", "Governor", "2013-2023"),
                    WorkExperience("DPR RI", "Member", "2004-2013"),
                    WorkExperience("Partai Demokrasi Indonesia Perjuangan (PDIP)", "Politician", "1999-present"),
                    WorkExperience("Various NGOs", "Activist", "1990-2004")
                )
            ),
            vice_president = Candidate(
                full_name = "Prof. Dr. H. M. Mahmud MD",
                gender = "Male",
                birth_place = "Sumenep, Madura",
                birth_date = "1957-05-04",
                religion = "Islam",
                last_education = "Professor of Constitutional Law",
                job = "Former Chief Justice of Constitutional Court",
                photo_path = "",
                education_history = listOf(
                    Education("Universitas Islam Indonesia Faculty of Law", "1982"),
                    Education("University of Washington School of Law", "1995"),
                    Education("Various International Law Programs", "1990-2000")
                ),
                work_experience = listOf(
                    WorkExperience("Constitutional Court of Indonesia", "Chief Justice", "2013-2020"),
                    WorkExperience("Universitas Islam Indonesia", "Professor", "1988-2013"),
                    WorkExperience("Ministry of Defense", "Secretary-General", "2000-2001"),
                    WorkExperience("Various Legal Institutions", "Legal Expert", "1985-present")
                )
            ),
            vote_count = 0,
            supporting_parties = listOf(
                SupportingParty(
                    id = "sp-3-1",
                    election_pair_id = "fallback-pair-3",
                    party_id = "party-pdip",
                    party = Party(
                        id = "party-pdip",
                        name = "Partai Demokrasi Indonesia Perjuangan",
                        logo_path = ""
                    )
                ),
                SupportingParty(
                    id = "sp-3-2",
                    election_pair_id = "fallback-pair-3",
                    party_id = "party-ppp",
                    party = Party(
                        id = "party-ppp",
                        name = "Partai Persatuan Pembangunan",
                        logo_path = ""
                    )
                ),
                SupportingParty(
                    id = "sp-3-3",
                    election_pair_id = "fallback-pair-3",
                    party_id = "party-hanura",
                    party = Party(
                        id = "party-hanura",
                        name = "Partai Hati Nurani Rakyat",
                        logo_path = ""
                    )
                ),
                SupportingParty(
                    id = "sp-3-4",
                    election_pair_id = "fallback-pair-3",
                    party_id = "party-perindo",
                    party = Party(
                        id = "party-perindo",
                        name = "Partai Persatuan Indonesia",
                        logo_path = ""
                    )
                )
            )
        )
    }

    /**
     * Get party logo resource mapping for fallback data
     */
    fun getPartyLogoResource(partyId: String): Int {
        return when (partyId) {
            "party-pkb" -> R.drawable.pp_pkb
            "party-pks" -> R.drawable.pp_pks
            "party-nasdem" -> R.drawable.pp_nasdem
            "party-gerindra" -> R.drawable.pp_gerinda
            "party-golkar" -> R.drawable.pp_golkar
            "party-pan" -> R.drawable.pp_pan
            "party-demokrat" -> R.drawable.pp_demokrat
            "party-psi" -> R.drawable.pp_psi
            "party-pbb" -> R.drawable.pp_pbb
            "party-pdip" -> R.drawable.pp_pdip
            "party-ppp" -> R.drawable.pp_ppp
            "party-hanura" -> R.drawable.pp_hanura
            "party-perindo" -> R.drawable.pp_perindo
            else -> R.drawable.pp_pkb // Default fallback
        }
    }

    /**
     * Get candidate photo resource mapping for fallback data
     */
    fun getCandidatePhotoResource(electionNo: String, isPresident: Boolean): Int {
        return when (electionNo) {
            "1" -> if (isPresident) R.drawable.pc_anies else R.drawable.pc_imin
            "2" -> if (isPresident) R.drawable.pc_prabowo else R.drawable.pc_gibran
            "3" -> if (isPresident) R.drawable.pc_ganjar else R.drawable.pc_mahfud
            else -> if (isPresident) R.drawable.pc_anies else R.drawable.pc_imin
        }
    }

    /**
     * Get combined candidate photo resource for selection screen
     */
    fun getCombinedCandidatePhotoResource(electionNo: String): Int {
        return when (electionNo) {
            "1" -> R.drawable.pc_anies // You may want to create combined photos
            "2" -> R.drawable.pc_prabowo
            "3" -> R.drawable.pc_ganjar
            else -> R.drawable.pc_anies
        }
    }

    /**
     * Get party logos for a specific election pair
     */
    fun getPartyLogosForElectionPair(electionNo: String): List<Int> {
        return when (electionNo) {
            "1" -> listOf(R.drawable.pp_pkb, R.drawable.pp_pks, R.drawable.pp_nasdem)
            "2" -> listOf(
                R.drawable.pp_gerinda, R.drawable.pp_golkar, R.drawable.pp_pan,
                R.drawable.pp_demokrat, R.drawable.pp_psi, R.drawable.pp_pbb
            )
            "3" -> listOf(R.drawable.pp_pdip, R.drawable.pp_ppp, R.drawable.pp_hanura, R.drawable.pp_perindo)
            else -> listOf(R.drawable.pp_pkb)
        }
    }

    /**
     * Check if election pair data is fallback data
     */
    fun isFallbackData(electionPairId: String): Boolean {
        return electionPairId.startsWith("fallback-")
    }

    /**
     * Get fallback data status message
     */
    fun getFallbackStatusMessage(): String {
        return "Using offline data due to connection issues"
    }
}