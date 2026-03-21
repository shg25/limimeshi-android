package com.shg25.limimeshi.core.domain

import com.shg25.limimeshi.core.data.repository.AuthRepository
import com.shg25.limimeshi.core.model.AuthUser
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): AuthUser? = authRepository.currentUser
}
