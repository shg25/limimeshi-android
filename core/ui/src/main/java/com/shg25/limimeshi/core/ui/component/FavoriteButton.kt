package com.shg25.limimeshi.core.ui.component

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * お気に入りボタンコンポーネント
 *
 * @param isFavorite お気に入り登録済みかどうか
 * @param isLoading 操作中かどうか
 * @param enabled ボタンが有効かどうか（未ログインユーザーはfalse）
 * @param onClick クリック時のコールバック
 * @param modifier Modifier
 */
@Composable
fun FavoriteButton(
    isFavorite: Boolean,
    isLoading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val description = when {
        isLoading -> "お気に入り処理中"
        isFavorite -> "お気に入り解除"
        else -> "お気に入り登録"
    }

    IconButton(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier.semantics {
            contentDescription = description
        }
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            }
            isFavorite -> {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            else -> {
                Icon(
                    imageVector = Icons.Outlined.FavoriteBorder,
                    contentDescription = null,
                    tint = if (enabled) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    }
                )
            }
        }
    }
}
