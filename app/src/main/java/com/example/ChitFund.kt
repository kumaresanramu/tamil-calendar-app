package com.example

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chit_funds")
data class ChitFund(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val chitValue: Double,
    val totalMonths: Int,
    val membersCount: Int,
    val companyCommissionPercent: Double = 5.0,
    val monthlyContribution: Double,
    val auctionBidsJson: String = "[]",
    val isWonByUser: Boolean = false,
    val userWonMonth: Int = 0,
    val userWonAmount: Double = 0.0,
    val startYear: Int,
    val startMonth: Int,
    val paymentDayOfMonth: Int = 10,
    val paidMonthsJson: String = "[]",
    val isActive: Boolean = true
) {
    // Safely parse winning bids per installment (1-based index)
    fun getAuctionBids(): List<Double> {
        if (auctionBidsJson.isBlank() || auctionBidsJson == "[]") return emptyList()
        return auctionBidsJson.trim('[', ']').split(",").mapNotNull { it.trim().toDoubleOrNull() }
    }

    // Bid for a given installment (1-based: installment #1 is index 0)
    fun getBidForInstallment(installmentIndex: Int): Double {
        val bids = getAuctionBids()
        val index = installmentIndex - 1
        return if (index >= 0 && index < bids.size) bids[index] else 0.0
    }

    // Safely parse paid installments (1-based index)
    fun getPaidMonths(): List<Int> {
        if (paidMonthsJson.isBlank() || paidMonthsJson == "[]") return emptyList()
        return paidMonthsJson.trim('[', ']').split(",").mapNotNull { it.trim().toIntOrNull() }
    }

    fun isInstallmentPaid(installmentIndex: Int): Boolean {
        return getPaidMonths().contains(installmentIndex)
    }

    // --- MAIN MECHANICAL CALCULATIONS ---

    // Company commission amount (typically 5% of Chit Value)
    fun getCompanyCommission(): Double {
        return (chitValue * companyCommissionPercent) / 100.0
    }

    // Detailed model of any specific installment
    data class InstallmentDetails(
        val installmentIndex: Int,
        val winningBidDiscount: Double,
        val companyCommission: Double,
        val remainingDiscount: Double,
        val dividendPerMember: Double,
        val finalPayment: Double,
        val prizeMoneyReceived: Double,
        val isPaid: Boolean
    )

    fun getInstallmentDetails(installmentIndex: Int): InstallmentDetails {
        val commission = getCompanyCommission()
        val winningBid = getBidForInstallment(installmentIndex)
        
        val remainingDiscount = if (installmentIndex == 1) {
            0.0
        } else {
            (winningBid - commission).coerceAtLeast(0.0)
        }
        
        val dividend = if (installmentIndex == 1) {
            0.0
        } else {
            remainingDiscount / membersCount.coerceAtLeast(1)
        }
        
        val finalPayment = monthlyContribution - dividend
        val prizeMoney = chitValue - winningBid
        val isPaid = isInstallmentPaid(installmentIndex)
        
        return InstallmentDetails(
            installmentIndex = installmentIndex,
            winningBidDiscount = winningBid,
            companyCommission = commission,
            remainingDiscount = remainingDiscount,
            dividendPerMember = dividend,
            finalPayment = finalPayment,
            prizeMoneyReceived = prizeMoney,
            isPaid = isPaid
        )
    }

    // Total final payments paid by user
    fun getTotalPaidByUser(): Double {
        var total = 0.0
        val paidList = getPaidMonths()
        for (m in paidList) {
            total += getInstallmentDetails(m).finalPayment
        }
        return total
    }

    // Dividends earned
    fun getTotalDividendsEarned(): Double {
        var total = 0.0
        for (m in 1..totalMonths) {
            total += getInstallmentDetails(m).dividendPerMember
        }
        return total
    }
}
