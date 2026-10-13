package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.entity.AttachmentEntity
import com.example.data.local.entity.NoteEntity
import com.example.ui.components.BottomFloatingPill
import com.example.ui.components.GlassBox
import com.example.ui.components.GlassCircleMenuButton
import com.example.ui.components.StarburstLogo
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.BodyText
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.homeGradient
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun GoodMorningScreen(
    userName: String,
    featuredNote: NoteEntity?,
    recentImageAttachment: AttachmentEntity?,
    recentPhotoNote: NoteEntity?,
    onLogoClick: () -> Unit,
    onMenuClick: () -> Unit,
    onDailyOverviewClick: () -> Unit,
    onFeaturedNoteClick: (NoteEntity) -> Unit,
    onHighlightsPhotoClick: (NoteEntity?) -> Unit,
    onAddClick: () -> Unit,
    onMicClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cal = remember { Calendar.getInstance() }
    val hour = remember { cal.get(Calendar.HOUR_OF_DAY) }

    val greetingText = remember(hour) {
        when (hour) {
            in 4..11 -> "Good\nMorning"
            in 12..17 -> "Good\nAfternoon"
            else -> "Good\nEvening"
        }
    }

    val dayOfWeek = remember {
        SimpleDateFormat("EEEE", Locale.getDefault()).format(Date())
    }
    val dateString = remember {
        SimpleDateFormat("MMM d", Locale.getDefault()).format(Date())
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(homeGradient())
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // Top Bar: Starburst Logo at left, 4-dot menu button at right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StarburstLogo(
                    size = 46.dp,
                    strokeWidthDp = 3.6.dp,
                    onClick = onLogoClick
                )

                GlassCircleMenuButton(
                    size = 48.dp,
                    onClick = onMenuClick
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // User Name in small thin white caps (e.g. "JOHN")
            if (userName.isNotBlank()) {
                Text(
                    text = userName.uppercase(Locale.getDefault()),
                    fontFamily = OutfitFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.sp,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(2.dp))
            }

            // Huge two-line greeting
            Text(
                text = greetingText,
                fontFamily = OutfitFontFamily,
                fontSize = 54.sp,
                lineHeight = 58.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Layered Composition:
            // 1. Tilted -8° Featured Note Card
            // 2. Overlapping Highlights Photo Card
            // 3. Daily Overview Card at lower left
            // Designed with precise z-index and spacing so cards never clash or cover text
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // Tilted -8° Featured note card at top-left
                TiltedFeaturedCard(
                    note = featuredNote,
                    onClick = {
                        if (featuredNote != null) onFeaturedNoteClick(featuredNote)
                        else onAddClick()
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.66f)
                        .offset(x = 4.dp, y = 6.dp)
                        .rotate(-8f)
                        .zIndex(1f)
                )

                // Overlapping Highlights Photo Card at top-right
                HighlightsPhotoCard(
                    attachment = recentImageAttachment,
                    photoNote = recentPhotoNote,
                    onClick = { onHighlightsPhotoClick(recentPhotoNote) },
                    modifier = Modifier
                        .fillMaxWidth(0.52f)
                        .align(Alignment.TopEnd)
                        .offset(x = 6.dp, y = 32.dp)
                        .rotate(4f)
                        .zIndex(3f)
                )

                // Frosted Daily Overview card positioned comfortably below Featured card
                DailyOverviewCard(
                    dayOfWeek = dayOfWeek,
                    dateString = dateString,
                    onClick = onDailyOverviewClick,
                    modifier = Modifier
                        .fillMaxWidth(0.56f)
                        .align(Alignment.BottomStart)
                        .offset(x = 10.dp, y = (-78).dp)
                        .zIndex(2f)
                )
            }
        }

        // Bottom Floating Pill: Add and Mic - positioned with clear bottom padding
        BottomFloatingPill(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
                .zIndex(10f),
            onAddClick = onAddClick,
            onMicClick = onMicClick
        )
    }
}

@Composable
private fun TiltedFeaturedCard(
    note: NoteEntity?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateText = remember(note?.updatedAt) {
        if (note != null) SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(note.updatedAt))
        else "1 Jun 2026"
    }

    Box(
        modifier = modifier
            .shadow(8.dp, RoundedCornerShape(28.dp), ambientColor = Color(0x1F000000))
            .clip(RoundedCornerShape(28.dp))
            .background(Color(0xF2FFFFFF))
            .border(1.dp, Color.White, RoundedCornerShape(28.dp))
            .clickable(onClick = onClick)
            .padding(18.dp)
            .testTag("featured_note_card")
    ) {
        Column {
            Text(
                text = note?.title?.ifBlank { "Thought of the day" } ?: "Thought of the day",
                fontFamily = OutfitFontFamily,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = AccentBlue,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = dateText,
                fontFamily = OutfitFontFamily,
                fontSize = 11.sp,
                color = Color(0xFF6B7A90)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Body text - sharp and crisp
            Text(
                text = note?.plainText?.ifBlank {
                    "Every day is a fresh start—embrace it with hope and courage. Let each moment inspire you to grow and shine brighter than before."
                } ?: "Tap to set or create a featured note that inspires your daily reflections.",
                fontFamily = OutfitFontFamily,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = BodyText,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun HighlightsPhotoCard(
    attachment: AttachmentEntity?,
    photoNote: NoteEntity?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateText = remember(photoNote?.updatedAt) {
        if (photoNote != null) SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(photoNote.updatedAt))
        else "25 May 2026"
    }

    Box(
        modifier = modifier
            .aspectRatio(0.80f)
            .shadow(12.dp, RoundedCornerShape(28.dp), ambientColor = Color(0x33000000))
            .clip(RoundedCornerShape(28.dp))
            .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(28.dp))
            .clickable(onClick = onClick)
            .testTag("highlights_card")
    ) {
        if (attachment != null) {
            AsyncImage(
                model = File(attachment.uri),
                contentDescription = "Highlights",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Elegant aesthetic photo backdrop
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF554754), Color(0xFF282330))
                        )
                    )
            )
        }

        // Dark gradient overlay for crisp text readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.55f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.45f)
                        )
                    )
                )
        )

        // Top-left text: Highlights and date
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Text(
                text = "Highlights",
                fontFamily = OutfitFontFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = dateText,
                fontFamily = OutfitFontFamily,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
private fun DailyOverviewCard(
    dayOfWeek: String,
    dateString: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(10.dp, RoundedCornerShape(28.dp), ambientColor = Color(0x1F000000))
            .clip(RoundedCornerShape(28.dp))
            .background(Color(0xEBFFFFFF))
            .border(1.dp, Color.White, RoundedCornerShape(28.dp))
            .clickable(onClick = onClick)
            .padding(18.dp)
            .testTag("daily_overview_card")
    ) {
        Column {
            Text(
                text = dayOfWeek,
                fontFamily = OutfitFontFamily,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B1B1B)
            )
            Text(
                text = dateString,
                fontFamily = OutfitFontFamily,
                fontSize = 17.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF5A6678)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = stringResource(R.string.daily_overview),
                fontFamily = OutfitFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF7A8799)
            )
        }
    }
}
