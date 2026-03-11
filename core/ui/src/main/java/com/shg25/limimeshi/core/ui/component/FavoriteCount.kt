package com.shg25.limimeshi.core.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

/**
 * お気に入り登録数の表示コンポーネント
 *
 * @param count お気に入り登録数
 * @param modifier Modifier
 */
@Composable
fun FavoriteCount(
    count: Int,
    modifier: Modifier = Modifier
) {
    if (count <= 0) return

    Text(
        text = "♥ ${count}人がお気に入り登録",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
private fun FavoriteCountPreview() {
    FavoriteCount(count = 42)
}
