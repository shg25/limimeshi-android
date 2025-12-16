package com.shg25.limimeshi.core.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.shg25.limimeshi.core.database.dao.FavoriteDao
import com.shg25.limimeshi.core.database.entity.FavoriteEntity
import com.shg25.limimeshi.core.model.Favorite
import com.shg25.limimeshi.core.network.datasource.FirestoreFavoritesDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant

/**
 * FavoritesRepositoryの単体テスト
 *
 * テストケース:
 * - お気に入り登録（ログイン時/未ログイン時）
 * - お気に入り解除（ログイン時/未ログイン時）
 * - お気に入りトグル
 * - Firestoreからの同期
 * - ローカルキャッシュのクリア
 */
@DisplayName("FavoritesRepository")
class FavoritesRepositoryTest {

    private lateinit var firestoreFavoritesDataSource: FirestoreFavoritesDataSource
    private lateinit var favoriteDao: FavoriteDao
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var repository: FavoritesRepository

    private val testUserId = "test-user-id"
    private val testChainId = "test-chain-id"

    @BeforeEach
    fun setup() {
        firestoreFavoritesDataSource = mockk(relaxed = true)
        favoriteDao = mockk(relaxed = true)
        firebaseAuth = mockk(relaxed = true)
        firebaseUser = mockk(relaxed = true)

        every { firebaseUser.uid } returns testUserId
        every { firebaseAuth.currentUser } returns firebaseUser

        repository = FavoritesRepository(
            firestoreFavoritesDataSource = firestoreFavoritesDataSource,
            favoriteDao = favoriteDao,
            firebaseAuth = firebaseAuth
        )
    }

    @Nested
    @DisplayName("addFavorite")
    inner class AddFavorite {

        @Test
        @DisplayName("ログイン時、Firestoreとローカルキャッシュに登録される")
        fun whenLoggedIn_addsToFirestoreAndLocalCache() = runTest {
            // When
            repository.addFavorite(testChainId)

            // Then
            coVerify { firestoreFavoritesDataSource.addFavorite(testUserId, testChainId) }
            coVerify { favoriteDao.insert(any()) }
        }

        @Test
        @DisplayName("未ログイン時、IllegalStateExceptionがスローされる")
        fun whenNotLoggedIn_throwsIllegalStateException() = runTest {
            // Given
            every { firebaseAuth.currentUser } returns null

            // When & Then
            assertThrows(IllegalStateException::class.java) {
                kotlinx.coroutines.runBlocking {
                    repository.addFavorite(testChainId)
                }
            }
        }

        @Test
        @DisplayName("正しいFavoriteEntityが作成される")
        fun createsCorrectFavoriteEntity() = runTest {
            // Given
            val entitySlot = slot<FavoriteEntity>()
            coEvery { favoriteDao.insert(capture(entitySlot)) } returns Unit

            // When
            repository.addFavorite(testChainId)

            // Then
            val capturedEntity = entitySlot.captured
            assertEquals(testUserId, capturedEntity.userId)
            assertEquals(testChainId, capturedEntity.chainId)
        }
    }

    @Nested
    @DisplayName("removeFavorite")
    inner class RemoveFavorite {

        @Test
        @DisplayName("ログイン時、Firestoreとローカルキャッシュから削除される")
        fun whenLoggedIn_removesFromFirestoreAndLocalCache() = runTest {
            // When
            repository.removeFavorite(testChainId)

            // Then
            coVerify { firestoreFavoritesDataSource.removeFavorite(testUserId, testChainId) }
            coVerify { favoriteDao.delete(testUserId, testChainId) }
        }

        @Test
        @DisplayName("未ログイン時、IllegalStateExceptionがスローされる")
        fun whenNotLoggedIn_throwsIllegalStateException() = runTest {
            // Given
            every { firebaseAuth.currentUser } returns null

            // When & Then
            assertThrows(IllegalStateException::class.java) {
                kotlinx.coroutines.runBlocking {
                    repository.removeFavorite(testChainId)
                }
            }
        }
    }

    @Nested
    @DisplayName("toggleFavorite")
    inner class ToggleFavorite {

        @Test
        @DisplayName("お気に入り登録済みの場合、removeFavoriteが呼ばれる")
        fun whenCurrentlyFavorite_callsRemoveFavorite() = runTest {
            // When
            repository.toggleFavorite(testChainId, currentlyFavorite = true)

            // Then
            coVerify { firestoreFavoritesDataSource.removeFavorite(testUserId, testChainId) }
            coVerify { favoriteDao.delete(testUserId, testChainId) }
        }

        @Test
        @DisplayName("お気に入り未登録の場合、addFavoriteが呼ばれる")
        fun whenNotCurrentlyFavorite_callsAddFavorite() = runTest {
            // When
            repository.toggleFavorite(testChainId, currentlyFavorite = false)

            // Then
            coVerify { firestoreFavoritesDataSource.addFavorite(testUserId, testChainId) }
            coVerify { favoriteDao.insert(any()) }
        }
    }

    @Nested
    @DisplayName("syncFromFirestore")
    inner class SyncFromFirestore {

        @Test
        @DisplayName("Firestoreからお気に入りを取得し、Roomに保存する")
        fun fetchesFavoritesAndSavesToRoom() = runTest {
            // Given
            val favorites = listOf(
                Favorite(chainId = "chain-1", createdAt = Instant.now()),
                Favorite(chainId = "chain-2", createdAt = Instant.now())
            )
            coEvery { firestoreFavoritesDataSource.getFavorites(testUserId) } returns favorites

            // When
            repository.syncFromFirestore()

            // Then
            coVerify { firestoreFavoritesDataSource.getFavorites(testUserId) }
            coVerify { favoriteDao.replaceAllByUserId(testUserId, any()) }
        }

        @Test
        @DisplayName("未ログイン時、何もしない")
        fun whenNotLoggedIn_doesNothing() = runTest {
            // Given
            every { firebaseAuth.currentUser } returns null

            // When
            repository.syncFromFirestore()

            // Then
            coVerify(exactly = 0) { firestoreFavoritesDataSource.getFavorites(any()) }
            coVerify(exactly = 0) { favoriteDao.replaceAllByUserId(any(), any()) }
        }
    }

    @Nested
    @DisplayName("clearLocalCache")
    inner class ClearLocalCache {

        @Test
        @DisplayName("ログイン時、ユーザーのローカルキャッシュを削除する")
        fun whenLoggedIn_deletesUserCache() = runTest {
            // When
            repository.clearLocalCache()

            // Then
            coVerify { favoriteDao.deleteAllByUserId(testUserId) }
        }

        @Test
        @DisplayName("未ログイン時、何もしない")
        fun whenNotLoggedIn_doesNothing() = runTest {
            // Given
            every { firebaseAuth.currentUser } returns null

            // When
            repository.clearLocalCache()

            // Then
            coVerify(exactly = 0) { favoriteDao.deleteAllByUserId(any()) }
        }
    }

    @Nested
    @DisplayName("currentUserId")
    inner class CurrentUserId {

        @Test
        @DisplayName("ログイン時、ユーザーIDを返す")
        fun whenLoggedIn_returnsUserId() {
            // When
            val result = repository.currentUserId

            // Then
            assertEquals(testUserId, result)
        }

        @Test
        @DisplayName("未ログイン時、nullを返す")
        fun whenNotLoggedIn_returnsNull() {
            // Given
            every { firebaseAuth.currentUser } returns null

            // When
            val result = repository.currentUserId

            // Then
            assertEquals(null, result)
        }
    }
}
