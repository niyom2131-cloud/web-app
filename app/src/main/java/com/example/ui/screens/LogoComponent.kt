package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A beautiful, highly-detailed vector-rendered illustration of the Bell 212 helicopter
 * based on the attached image, crafted purely in Jetpack Compose Canvas.
 * It features the white upper fuselage, the yellow/orange swoosh stripe, and the deep green belly and tail,
 * with silver skids, main rotor assembly, and black cockpit glass windows.
 */
@Composable
fun AviationUnitLogo(
    modifier: Modifier = Modifier,
    size: Dp = 140.dp,
    showText: Boolean = true
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.width(size)
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = this.size.width
                val h = this.size.height

                // 1. Draw a clean, modern circular badge background
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFFFFFF), Color(0xFFF1F5F9), Color(0xFFE2E8F0)),
                        center = Offset(w * 0.5f, h * 0.5f),
                        radius = w * 0.48f
                    )
                )
                drawCircle(
                    color = Color(0xFF198754).copy(alpha = 0.15f), // Outer green glow
                    radius = w * 0.48f,
                    style = Stroke(width = w * 0.03f)
                )
                drawCircle(
                    color = Color(0xFFF59E0B).copy(alpha = 0.4f), // Golden yellow accent ring
                    radius = w * 0.45f,
                    style = Stroke(width = w * 0.012f)
                )

                // 2. Main Rotor Mast, Cowling, and Accessories (drawn behind fuselage)
                // Rotor mast column
                drawLine(
                    color = Color(0xFF334155),
                    start = Offset(w * 0.48f, h * 0.40f),
                    end = Offset(w * 0.48f, h * 0.25f),
                    strokeWidth = w * 0.02f
                )
                // Rotor hub/control linkages
                drawCircle(
                    color = Color(0xFF475569),
                    radius = w * 0.03f,
                    center = Offset(w * 0.48f, h * 0.25f)
                )
                // Left rotor blade stretching far left-up
                drawLine(
                    color = Color(0xFF334155),
                    start = Offset(w * 0.48f, h * 0.25f),
                    end = Offset(w * 0.04f, h * 0.24f),
                    strokeWidth = w * 0.012f,
                    cap = StrokeCap.Round
                )
                // Right rotor blade stretching far right-down
                drawLine(
                    color = Color(0xFF334155),
                    start = Offset(w * 0.48f, h * 0.25f),
                    end = Offset(w * 0.96f, h * 0.18f),
                    strokeWidth = w * 0.012f,
                    cap = StrokeCap.Round
                )

                // 3. Helicopter Body Base Layer (White/Light-Grey base)
                val bodyPath = Path().apply {
                    // Start at tail tip (mid-left)
                    moveTo(w * 0.15f, h * 0.32f)
                    // Tail boom top line to cabin
                    lineTo(w * 0.36f, h * 0.40f)
                    // Cabin roof / doghouse cowl
                    quadraticBezierTo(w * 0.40f, h * 0.36f, w * 0.55f, h * 0.36f)
                    // Cabin windscreen drop
                    quadraticBezierTo(w * 0.68f, h * 0.40f, w * 0.72f, h * 0.48f)
                    // Nose curve (front-right)
                    quadraticBezierTo(w * 0.76f, h * 0.54f, w * 0.72f, h * 0.58f)
                    // Bottom cabin belly
                    quadraticBezierTo(w * 0.65f, h * 0.62f, w * 0.50f, h * 0.62f)
                    // Belly transition to tail boom bottom
                    quadraticBezierTo(w * 0.38f, h * 0.54f, w * 0.30f, h * 0.48f)
                    // Tail boom bottom back to tail tip
                    lineTo(w * 0.15f, h * 0.38f)
                    close()
                }
                // Fill base with pristine white
                drawPath(path = bodyPath, color = Color(0xFFFAFAFA))

                // 4. Green Belly and Tail Boom Paint Scheme (Livery)
                val greenBellyPath = Path().apply {
                    // Tail tip bottom
                    moveTo(w * 0.15f, h * 0.38f)
                    // Go slightly up on tail boom
                    lineTo(w * 0.15f, h * 0.35f)
                    // Sweep along the lower tail boom and under cabin belly
                    quadraticBezierTo(w * 0.32f, h * 0.45f, w * 0.42f, h * 0.51f)
                    quadraticBezierTo(w * 0.54f, h * 0.59f, w * 0.70f, h * 0.57f)
                    // Round nose-front bottom corner
                    lineTo(w * 0.68f, h * 0.60f)
                    // Under cabin belly back to tail
                    quadraticBezierTo(w * 0.50f, h * 0.62f, w * 0.30f, h * 0.48f)
                    close()
                }
                drawPath(path = greenBellyPath, color = Color(0xFF0F5132)) // Forest Green livery belly

                // 5. Yellow/Orange Swoop Stripe (Livery Accent)
                val yellowStripePath = Path().apply {
                    // Start at tail boom
                    moveTo(w * 0.15f, h * 0.35f)
                    quadraticBezierTo(w * 0.32f, h * 0.44f, w * 0.42f, h * 0.50f)
                    quadraticBezierTo(w * 0.54f, h * 0.58f, w * 0.71f, h * 0.55f)
                    // Slightly down and back to make it a stripe
                    lineTo(w * 0.70f, h * 0.57f)
                    quadraticBezierTo(w * 0.54f, h * 0.59f, w * 0.42f, h * 0.51f)
                    quadraticBezierTo(w * 0.32f, h * 0.45f, w * 0.15f, h * 0.36f)
                    close()
                }
                drawPath(path = yellowStripePath, color = Color(0xFFFBBF24)) // Bright yellow-gold stripe

                // Outer border outline to make it crisp and comic/vector styled
                drawPath(path = bodyPath, color = Color(0xFF334155), style = Stroke(width = w * 0.015f))

                // 6. Cockpit Glass and Cabin Windows (Glossy Jet Black)
                // Front windshield curves
                val windshieldPath = Path().apply {
                    moveTo(w * 0.60f, h * 0.42f)
                    lineTo(w * 0.67f, h * 0.42f)
                    quadraticBezierTo(w * 0.70f, h * 0.45f, w * 0.70f, h * 0.49f)
                    lineTo(w * 0.63f, h * 0.50f)
                    close()
                }
                drawPath(path = windshieldPath, color = Color(0xFF0F172A))
                // Glass gloss effect
                drawLine(
                    color = Color.White.copy(alpha = 0.4f),
                    start = Offset(w * 0.64f, h * 0.43f),
                    end = Offset(w * 0.68f, h * 0.47f),
                    strokeWidth = w * 0.01f,
                    cap = StrokeCap.Round
                )

                // Cabin sliding door windows
                val cabinWindow1 = Path().apply {
                    moveTo(w * 0.52f, h * 0.42f)
                    lineTo(w * 0.58f, h * 0.42f)
                    lineTo(w * 0.58f, h * 0.50f)
                    lineTo(w * 0.52f, h * 0.50f)
                    close()
                }
                val cabinWindow2 = Path().apply {
                    moveTo(w * 0.43f, h * 0.42f)
                    lineTo(w * 0.49f, h * 0.42f)
                    lineTo(w * 0.49f, h * 0.50f)
                    lineTo(w * 0.43f, h * 0.50f)
                    close()
                }
                drawPath(path = cabinWindow1, color = Color(0xFF0F172A))
                drawPath(path = cabinWindow2, color = Color(0xFF0F172A))

                // 7. Tail Fins and Tail Rotor (upper left)
                // Tail stabilizer vertical fin
                val tailFinPath = Path().apply {
                    moveTo(w * 0.15f, h * 0.32f)
                    lineTo(w * 0.18f, h * 0.18f) // Tip pointing up-right
                    lineTo(w * 0.22f, h * 0.18f)
                    lineTo(w * 0.18f, h * 0.34f)
                    close()
                }
                drawPath(path = tailFinPath, color = Color(0xFF0F5132))
                drawPath(path = tailFinPath, color = Color(0xFF334155), style = Stroke(width = w * 0.012f))

                // Tail rotor blades lines
                drawLine(
                    color = Color(0xFF475569),
                    start = Offset(w * 0.14f, h * 0.23f),
                    end = Offset(w * 0.22f, h * 0.15f),
                    strokeWidth = w * 0.015f,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = Color(0xFF475569),
                    start = Offset(w * 0.22f, h * 0.23f),
                    end = Offset(w * 0.14f, h * 0.15f),
                    strokeWidth = w * 0.015f,
                    cap = StrokeCap.Round
                )

                // 8. Metallic Landing Skids (bottom)
                // Front structural support leg
                drawLine(
                    color = Color(0xFF64748B),
                    start = Offset(w * 0.59f, h * 0.59f),
                    end = Offset(w * 0.56f, h * 0.69f),
                    strokeWidth = w * 0.018f,
                    cap = StrokeCap.Round
                )
                // Rear structural support leg
                drawLine(
                    color = Color(0xFF64748B),
                    start = Offset(w * 0.45f, h * 0.56f),
                    end = Offset(w * 0.42f, h * 0.68f),
                    strokeWidth = w * 0.018f,
                    cap = StrokeCap.Round
                )
                // Horizontal landing skid bar with curve at nose-end
                val skidBar = Path().apply {
                    moveTo(w * 0.33f, h * 0.68f)
                    lineTo(w * 0.65f, h * 0.69f)
                    quadraticBezierTo(w * 0.72f, h * 0.69f, w * 0.74f, h * 0.63f) // upward curving skid tip
                }
                drawPath(
                    path = skidBar,
                    color = Color(0xFF475569),
                    style = Stroke(width = w * 0.024f, cap = StrokeCap.Round)
                )
            }
        }
        
        if (showText) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "ร้อย.ซบร.บ.ทบ.สท.",
                fontSize = (size.value * 0.08f).sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Miniature compact version of the unit logo designed for AppBar/TopBar/Headers.
 * Renders the same white-green-yellow Bell 212 helicopter in a micro-scale.
 */
@Composable
fun AviationUnitLogoMini(
    modifier: Modifier = Modifier,
    size: Dp = 42.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .background(Color(0xFF0F172A), RoundedCornerShape(10.dp))
            .border(1.5.dp, Color(0xFFFBBF24), RoundedCornerShape(10.dp))
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height

            // 1. Helicopter Fuselage Shape (Micro-scale)
            val body = Path().apply {
                moveTo(w * 0.15f, h * 0.35f) // tail tip
                lineTo(w * 0.38f, h * 0.45f) // boom to cabin
                quadraticBezierTo(w * 0.45f, h * 0.38f, w * 0.60f, h * 0.38f) // cabin top
                quadraticBezierTo(w * 0.78f, h * 0.42f, w * 0.82f, h * 0.52f) // nose front
                quadraticBezierTo(w * 0.75f, h * 0.65f, w * 0.55f, h * 0.65f) // cabin bottom
                quadraticBezierTo(w * 0.42f, h * 0.58f, w * 0.32f, h * 0.50f) // bottom to boom
                close()
            }
            // Base white
            drawPath(path = body, color = Color.White)

            // Green bottom belly
            val belly = Path().apply {
                moveTo(w * 0.15f, h * 0.40f)
                quadraticBezierTo(w * 0.38f, h * 0.50f, w * 0.48f, h * 0.55f)
                quadraticBezierTo(w * 0.58f, h * 0.65f, w * 0.78f, h * 0.60f)
                lineTo(w * 0.75f, h * 0.64f)
                quadraticBezierTo(w * 0.55f, h * 0.65f, w * 0.32f, h * 0.52f)
                close()
            }
            drawPath(path = belly, color = Color(0xFF198754))

            // Yellow Stripe line
            val stripe = Path().apply {
                moveTo(w * 0.15f, h * 0.38f)
                quadraticBezierTo(w * 0.38f, h * 0.48f, w * 0.48f, h * 0.53f)
                quadraticBezierTo(w * 0.58f, h * 0.62f, w * 0.80f, h * 0.57f)
                lineTo(w * 0.78f, h * 0.60f)
                quadraticBezierTo(w * 0.58f, h * 0.64f, w * 0.48f, h * 0.55f)
                quadraticBezierTo(w * 0.38f, h * 0.50f, w * 0.15f, h * 0.40f)
                close()
            }
            drawPath(path = stripe, color = Color(0xFFFFC107))

            // Cockpit Windshield (Black dot)
            drawCircle(
                color = Color(0xFF0F172A),
                radius = w * 0.08f,
                center = Offset(w * 0.72f, h * 0.46f)
            )

            // Main rotor mast & blades (Thin grey lines)
            drawLine(
                color = Color(0xFF94A3B8),
                start = Offset(w * 0.52f, h * 0.38f),
                end = Offset(w * 0.52f, h * 0.22f),
                strokeWidth = w * 0.03f
            )
            // Long horizontal blade
            drawLine(
                color = Color(0xFFCBD5E1),
                start = Offset(w * 0.08f, h * 0.24f),
                end = Offset(w * 0.92f, h * 0.20f),
                strokeWidth = w * 0.025f
            )

            // Skids (grey)
            drawLine(
                color = Color(0xFF94A3B8),
                start = Offset(w * 0.32f, h * 0.72f),
                end = Offset(w * 0.72f, h * 0.73f),
                strokeWidth = w * 0.035f
            )
        }
    }
}
