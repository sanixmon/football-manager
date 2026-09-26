package com.footballmanager.app.ui.theme

import androidx.compose.ui.graphics.Color

// ── Football Manager Authentic Dark Theme Palette ─────────────────────────
// Deep stadium dark base (NOT generic grey)
val FmDarkBg = Color(0xFF0C0F14)
val FmSurface = Color(0xFF141820)
val FmSurfaceAlt = Color(0xFF1A202A)
val FmCardBg = Color(0xFF141820)
val FmHeaderBg = Color(0xFF11151C)
val FmSurfaceHover = Color(0xFF1F2633)
val FmSurfaceSelected = Color(0xFF222C3D)
val FmBorder = Color(0xFF262F3D)
val FmBorderSubtle = Color(0xFF1B222C)
val FmTopBarBg = Color(0xFF0F131A)
val FmFooterBg = Color(0xFF090C10)

// ── Brand & Management Accents ───────────────────────────────────────────
val FmAccentBlue = Color(0xFF2563EB)
val FmAccentCyan = Color(0xFF0EA5E9)
val FmContinueGreen = Color(0xFF10B981)
val FmPrimaryGreen = Color(0xFF10B981)
val FmContinueGreenPressed = Color(0xFF059669)
val FmPurpleAccent = Color(0xFF6366F1)

// ── Typography & Contrast ────────────────────────────────────────────────
val FmTextPrimary = Color(0xFFF1F5F9)
val FmTextSecondary = Color(0xFF94A3B8)
val FmTextMuted = Color(0xFF64748B)
val FmTextHighContrast = Color(0xFFFFFFFF)

// ── Semantic Match & Condition Colors ─────────────────────────────────────
val FmRatingHigh = Color(0xFF10B981)
val FmRatingMed = Color(0xFFF59E0B)
val FmRatingLow = Color(0xFFEF4444)
val FmFormWin = Color(0xFF10B981)
val FmFormDraw = Color(0xFFF59E0B)
val FmFormLoss = Color(0xFFEF4444)

// ── Positional Colors (Football Standard) ─────────────────────────────────
val FmPosGk = Color(0xFFF59E0B)  // Gold / Amber
val FmPosDef = Color(0xFF3B82F6) // Blue
val FmPosMid = Color(0xFF10B981) // Green
val FmPosAtt = Color(0xFFEF4444) // Red

// ── Legacy Compatibility Aliases ──────────────────────────────────────────
val StadiumEmerald = FmContinueGreen
val ElectricBlue = FmAccentBlue
val DeepNavy = FmDarkBg
val SurfaceSlate = FmSurface
val BorderSlate = FmBorder
val TextLight = FmTextPrimary
val TextMuted = FmTextSecondary
val StatusGreen = FmRatingHigh
val StatusAmber = FmRatingMed
val StatusCoral = FmRatingLow
val TurfStripeDark = Color(0xFF0F3E22)
val TurfStripeLight = Color(0xFF134E2B)
